
package com.pes.travelexplorer.data.local

import android.content.Context

class PreferencesManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "travel_explorer_preferences",
        Context.MODE_PRIVATE
    )

    // Save currency
    fun saveCurrency(currency: String) {
        preferences.edit()
            .putString("preferred_currency", currency)
            .apply()
    }

    // Get saved currency
    fun getCurrency(): String {
        return preferences.getString("preferred_currency", "INR") ?: "INR"
    }

    // Save dark mode
    fun saveDarkMode(enabled: Boolean) {
        preferences.edit()
            .putBoolean("dark_mode", enabled)
            .apply()
    }

    // Get dark mode
    fun isDarkModeEnabled(): Boolean {
        return preferences.getBoolean("dark_mode", false)
    }
}
