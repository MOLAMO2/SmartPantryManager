package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RequiredIngredient;

/**
 * Screen 4: Recipe Detail (Section 2.2).
 * Receives the recipe id via Intent extra from SuggestedRecipesActivity and
 * displays the full ingredient list and preparation method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);

        long recipeId = getIntent().getLongExtra(SuggestedRecipesActivity.EXTRA_RECIPE_ID, -1);
        Recipe recipe = dbHelper.getRecipeById(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        TextView title = findViewById(R.id.text_recipe_title);
        TextView ingredientsText = findViewById(R.id.text_recipe_ingredients);
        TextView stepsText = findViewById(R.id.text_recipe_steps);

        title.setText(recipe.getName());
        setTitle(recipe.getName());

        StringBuilder ingredientsList = new StringBuilder();
        for (RequiredIngredient requirement : recipe.getRequiredIngredients()) {
            ingredientsList.append("• ")
                    .append(formatQuantity(requirement.getQuantity()))
                    .append(" ")
                    .append(requirement.getUnit())
                    .append(" ")
                    .append(capitalize(requirement.getName()))
                    .append("\n");
        }
        ingredientsText.setText(ingredientsList.toString().trim());
        stepsText.setText(recipe.getSteps());
    }

    private String formatQuantity(double quantity) {
        if (quantity == Math.floor(quantity)) {
            return String.valueOf((long) quantity);
        }
        return String.valueOf(quantity);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
