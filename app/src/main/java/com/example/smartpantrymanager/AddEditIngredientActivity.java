package com.example.smartpantrymanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;

/**
 * Screen 2: Add/Edit Ingredient (Section 2.2 & 3.1).
 * A single Activity handles both "add" and "edit" depending on whether an
 * ingredient id was passed in via Intent extras from PantryListActivity.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    private static final String[] UNITS = {"piece", "g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb"};

    private DatabaseHelper dbHelper;
    private TextInputLayout layoutName, layoutQuantity;
    private TextInputEditText editName, editQuantity;
    private Spinner spinnerUnit;
    private TextView textExpiryValue;
    private Button btnPickDate, btnSave;

    private long editingId = -1; // -1 means "adding new"
    private String selectedExpiryDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        layoutName = findViewById(R.id.layout_name);
        layoutQuantity = findViewById(R.id.layout_quantity);
        editName = findViewById(R.id.edit_name);
        editQuantity = findViewById(R.id.edit_quantity);
        spinnerUnit = findViewById(R.id.spinner_unit);
        textExpiryValue = findViewById(R.id.text_expiry_value);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSave = findViewById(R.id.btn_save);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveIngredient());

        // Read the Intent extra to decide add vs edit mode (Section 3.1).
        editingId = getIntent().getLongExtra(PantryListActivity.EXTRA_INGREDIENT_ID, -1);
        if (editingId != -1) {
            setTitle(R.string.title_add_edit);
            populateFieldsForEdit(editingId);
        }
    }

    private void populateFieldsForEdit(long id) {
        Ingredient ingredient = dbHelper.getIngredientById(id);
        if (ingredient == null) return;

        editName.setText(ingredient.getName());
        editQuantity.setText(formatQuantity(ingredient.getQuantity()));

        int unitIndex = indexOf(UNITS, ingredient.getUnit());
        if (unitIndex >= 0) spinnerUnit.setSelection(unitIndex);

        if (ingredient.getExpiryDate() != null && !ingredient.getExpiryDate().isEmpty()) {
            selectedExpiryDate = ingredient.getExpiryDate();
            textExpiryValue.setText(selectedExpiryDate);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedExpiryDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            textExpiryValue.setText(selectedExpiryDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    /**
     * Input validation on the data entry form, as required by Section 3.1.
     */
    private boolean validateInputs() {
        boolean valid = true;
        String name = editName.getText() == null ? "" : editName.getText().toString().trim();
        String qtyText = editQuantity.getText() == null ? "" : editQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            layoutName.setError(getString(R.string.error_name_required));
            valid = false;
        } else {
            layoutName.setError(null);
        }

        try {
            double qty = Double.parseDouble(qtyText);
            if (qty <= 0 || qty > 10000) {
                layoutQuantity.setError(getString(R.string.error_quantity_invalid));
                valid = false;
            } else {
                layoutQuantity.setError(null);
            }
        } catch (NumberFormatException e) {
            layoutQuantity.setError(getString(R.string.error_quantity_invalid));
            valid = false;
        }

        return valid;
    }

    private void saveIngredient() {
        if (!validateInputs()) return;

        String name = editName.getText().toString().trim();
        double quantity = Double.parseDouble(editQuantity.getText().toString().trim());
        String unit = (String) spinnerUnit.getSelectedItem();

        Ingredient ingredient = new Ingredient(editingId, name, quantity, unit, selectedExpiryDate);

        if (editingId == -1) {
            dbHelper.addIngredient(ingredient);
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateIngredient(ingredient);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    private int indexOf(String[] array, String value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(value)) return i;
        }
        return -1;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
