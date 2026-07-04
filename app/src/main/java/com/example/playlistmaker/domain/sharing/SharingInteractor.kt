package com.example.playlistmaker.domain.sharing

interface SharingInteractor {
    fun shareApp()
    fun openTerms(errorMessage: String)
    fun openSupport(errorMessage: String)
}
