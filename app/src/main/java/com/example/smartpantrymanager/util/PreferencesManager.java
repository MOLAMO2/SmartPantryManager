package com.example.smartpantrymanager.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple wrapper around SharedPreferences for the Settings screen
 * (Section 2.2: "A settings or profile screen").
 */
public class PreferencesManager {

    private static final String PREFS_NAME = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts_enabled";
    private static final String KEY_UNIT_SYSTEM = "unit_system"; // "metric" or "imperial"

    private final SharedPreferences prefs;

    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isExpiryAlertsEnabled() {
        return prefs.getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public void setExpiryAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_EXPIRY_ALERTS, enabled).apply();
    }

    public String getUnitSystem() {
        return prefs.getString(KEY_UNIT_SYSTEM, "metric");
    }

    public void setUnitSystem(String system) {
        prefs.edit().putString(KEY_UNIT_SYSTEM, system).apply();
    }
}
