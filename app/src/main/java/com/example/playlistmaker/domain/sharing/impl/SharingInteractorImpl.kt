package com.example.playlistmaker.domain.sharing.impl

import com.example.playlistmaker.domain.sharing.ExternalNavigator
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.model.EmailData

class SharingInteractorImpl(
    private val externalNavigator: ExternalNavigator,
    private val shareAppLink: String,
    private val termsLink: String,
    private val supportEmailData: EmailData,
) : SharingInteractor {

    override fun shareApp() {
        externalNavigator.shareLink(shareAppLink)
    }

    override fun openTerms(errorMessage: String) {
        externalNavigator.openLink(termsLink, errorMessage)
    }

    override fun openSupport(errorMessage: String) {
        externalNavigator.openEmail(supportEmailData, errorMessage)
    }
}
