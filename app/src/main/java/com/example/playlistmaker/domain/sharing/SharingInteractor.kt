package com.example.playlistmaker.domain.sharing

interface SharingInteractor {
    fun shareApp()
    fun shareText(text: String)
    fun openTerms(errorMessage: String)
    fun openSupport(errorMessage: String)
}
