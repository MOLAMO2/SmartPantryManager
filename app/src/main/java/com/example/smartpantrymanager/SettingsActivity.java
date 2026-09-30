package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.example.smartpantrymanager.util.PreferencesManager;

/**
 * Screen 5: Settings (Section 2.2).
 * Lets the user toggle expiring-soon alerts and pick a preferred unit
 * system, persisted via SharedPreferences (PreferencesManager).
 */
public class SettingsActivity extends BaseActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferencesManager = new PreferencesManager(this);

        Switch switchExpiryAlerts = findViewById(R.id.switch_expiry_alerts);
        RadioGroup radioUnits = findViewById(R.id.radio_units);

        switchExpiryAlerts.setChecked(preferencesManager.isExpiryAlertsEnabled());
        radioUnits.check(preferencesManager.getUnitSystem().equals("imperial")
                ? R.id.radio_imperial : R.id.radio_metric);

        switchExpiryAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferencesManager.setExpiryAlertsEnabled(isChecked);
            Toast.makeText(this, isChecked ? "Expiry alerts on" : "Expiry alerts off",
                    Toast.LENGTH_SHORT).show();
        });
        radioUnits.setOnCheckedChangeListener((group, checkedId) -> {
            String system = (checkedId == R.id.radio_imperial) ? "imperial" : "metric";
            preferencesManager.setUnitSystem(system);
        });

        setupBottomNavigation();
    }

    @Override
    protected int getSelectedNavItemId() {
        return R.id.nav_settings;
    }
}
