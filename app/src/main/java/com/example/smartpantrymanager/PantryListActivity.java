package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * Screen 1: Pantry List (Section 2.2).
 * Shows all pantry items in a RecyclerView bound to SQLite, and lets the
 * user add, edit, or delete ingredients (full CRUD - Section 3.2).
 */
public class PantryListActivity extends BaseActivity implements PantryAdapter.Listener {

    public static final String EXTRA_INGREDIENT_ID = "extra_ingredient_id";

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private PantryAdapter adapter;
    private List<Ingredient> pantryItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_pantry);
        emptyView = findViewById(R.id.text_empty_pantry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fab_add_ingredient);
        fab.setOnClickListener(v -> {
            // Intent navigation with no extras = "add" mode.
            Intent intent = new Intent(PantryListActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time we return to this screen so edits/deletes/adds
        // made elsewhere (or after process death) are always reflected, and
        // to prove data persists after the app is reopened (Section 3.2).
        loadPantry();
    }

    private void loadPantry() {
        pantryItems = dbHelper.getAllIngredients();
        if (pantryItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
        adapter = new PantryAdapter(pantryItems, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onEditClicked(Ingredient ingredient) {
        // Pass the ingredient's id via the Intent so the next screen knows
        // it is in "edit" mode (Section 3.1: "correct use of Intents ... to
        // pass data").
        Intent intent = new Intent(this, AddEditIngredientActivity.class);
        intent.putExtra(EXTRA_INGREDIENT_ID, ingredient.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClicked(Ingredient ingredient) {
        dbHelper.deleteIngredient(ingredient.getId());
        loadPantry();
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_pantry;
    }
}
