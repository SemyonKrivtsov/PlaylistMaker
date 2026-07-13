package com.example.playlistmaker.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.example.playlistmaker.domain.settings.model.ThemeSettings

class SettingsRepositoryImpl(private val sharedPreferences: SharedPreferences) : SettingsRepository {

    override fun getThemeSettings(): ThemeSettings =
        ThemeSettings(sharedPreferences.getBoolean(DARK_THEME, false))

    override fun updateThemeSetting(settings: ThemeSettings) {
        sharedPreferences.edit {
            putBoolean(DARK_THEME, settings.isDarkThemeEnabled)
        }
    }

    companion object {
        private const val DARK_THEME = "dark_theme"
    }
}
