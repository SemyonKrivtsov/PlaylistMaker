package com.example.playlistmaker.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.domain.api.SettingsRepository

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun isDarkThemeEnabled(): Boolean =
        sharedPreferences.getBoolean(DARK_THEME, false)

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(DARK_THEME, enabled)
        }
    }

    companion object {
        private const val DARK_THEME = "dark_theme"
    }
}
