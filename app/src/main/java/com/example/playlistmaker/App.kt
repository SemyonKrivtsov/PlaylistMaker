package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {

    var darkTheme = false
    private val sharedPreferences by lazy { getSharedPreferences(SETTINGS, MODE_PRIVATE) }

    override fun onCreate() {
        super.onCreate()
        darkTheme = sharedPreferences.getBoolean(DARK_THEME, false)
        applyTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        applyTheme(darkThemeEnabled)
        saveTheme(darkThemeEnabled)
    }

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }

    private fun saveTheme(darkThemeEnabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(DARK_THEME, darkThemeEnabled)
            .apply()
    }

    companion object {
        private const val SETTINGS = "settings"
        private const val DARK_THEME = "dark_theme"
    }
}
