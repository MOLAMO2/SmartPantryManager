package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.db.DatabaseHelper;
import com.example.smartpantrymanager.model.Ingredient;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.util.IngredientMatcher;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen 3: Suggested Recipes (Section 2.2 & 2.3).
 * Tab 1 ("Suggested") shows ONLY recipes that pass the strict-matching rule.
 * Tab 2 ("Almost There") is the optional bonus feature (Section 8): recipes
 * missing exactly one ingredient, kept clearly separate from the strict list.
 */
public class SuggestedRecipesActivity extends BaseActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private TabLayout tabLayout;

    private List<Recipe> allRecipes;
    private List<Ingredient> pantry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler_recipes);
        emptyView = findViewById(R.id.text_empty_recipes);
        tabLayout = findViewById(R.id.tab_layout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                renderList(tab.getPosition() == 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-run matching every time the pantry may have changed, so adding
        // or removing one ingredient immediately updates the suggestions
        // (this is exactly what the video demo in Section 5.1 must show).
        allRecipes = dbHelper.getAllRecipes();
        pantry = dbHelper.getAllIngredients();
        renderList(tabLayout.getSelectedTabPosition() == 0);
    }

    /**
     * @param strictTab true = the "Suggested" tab (exact strict matches only);
     *                  false = the "Almost There" bonus tab (missing exactly 1).
     */
    private void renderList(boolean strictTab) {
        List<Recipe> results = new ArrayList<>();
        Map<Long, Integer> missingCounts = new HashMap<>();

        for (Recipe recipe : allRecipes) {
            int missing = IngredientMatcher.countMissingIngredients(recipe, pantry);
            if (strictTab && missing == 0) {
                results.add(recipe);
                missingCounts.put(recipe.getId(), 0);
            } else if (!strictTab && missing == 1) {
                results.add(recipe);
                missingCounts.put(recipe.getId(), 1);
            }
        }
        Collections.sort(results, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        if (results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(strictTab ? R.string.empty_suggestions : R.string.empty_almost);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }

        RecipeAdapter adapter = new RecipeAdapter(results, missingCounts, recipe -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra(EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_suggested;
    }
}
