package com.example.playlistmaker.ui.settings.view_model

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.model.ThemeSettings
import com.example.playlistmaker.domain.sharing.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val themeSettingsLiveData = MutableLiveData(settingsInteractor.getThemeSettings())
    fun observeThemeSettings(): LiveData<ThemeSettings> = themeSettingsLiveData

    fun switchTheme(darkThemeEnabled: Boolean) {
        val settings = ThemeSettings(darkThemeEnabled)
        settingsInteractor.updateThemeSetting(settings)
        applyTheme(darkThemeEnabled)
        themeSettingsLiveData.value = settings
    }

    fun shareApp() = sharingInteractor.shareApp()

    fun openTerms(errorMessage: String) = sharingInteractor.openTerms(errorMessage)

    fun openSupport(errorMessage: String) = sharingInteractor.openSupport(errorMessage)

    private fun applyTheme(darkThemeEnabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
    }
}
