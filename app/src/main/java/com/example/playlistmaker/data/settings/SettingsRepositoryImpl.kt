package com.example.playlistmaker.data.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.example.playlistmaker.domain.settings.model.ThemeSettings

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) :
    SettingsRepository {

    override fun getThemeSettings(): ThemeSettings =
        ThemeSettings(sharedPreferences.getBoolean(DARK_THEME, false))

    override fun updateThemeSetting(settings: ThemeSettings) {
        sharedPreferences.edit {
            putBoolean(DARK_THEME, settings.isDarkThemeEnabled)
        }
        applyTheme(settings.isDarkThemeEnabled)
    }

    override fun applyCurrentTheme() {
        applyTheme(getThemeSettings().isDarkThemeEnabled)
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

    companion object {
        private const val DARK_THEME = "dark_theme"
    }
}
