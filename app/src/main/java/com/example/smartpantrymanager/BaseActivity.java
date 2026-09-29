package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Base class for the three main tab screens (Pantry, Suggested Recipes,
 * Settings). Wires up the shared BottomNavigationView so navigating between
 * them uses an Intent, as required by Section 3.1.
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Subclasses return which bottom-nav item corresponds to themselves. */
    protected abstract int getSelectedNavItemId();

    protected void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(getSelectedNavItemId());
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == getSelectedNavItemId()) {
                return true; // already here
            }
            Intent intent = null;
            if (id == R.id.nav_pantry) {
                intent = new Intent(this, PantryListActivity.class);
            } else if (id == R.id.nav_suggested) {
                intent = new Intent(this, SuggestedRecipesActivity.class);
            } else if (id == R.id.nav_settings) {
                intent = new Intent(this, SettingsActivity.class);
            }
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
            return true;
        });
    }
}
