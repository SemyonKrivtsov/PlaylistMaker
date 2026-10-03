package com.example.playlistmaker.domain.sharing

import com.example.playlistmaker.domain.sharing.model.EmailData

interface ExternalNavigator {
    fun shareText(text: String)
    fun openLink(link: String, errorMessage: String)
    fun openEmail(emailData: EmailData, errorMessage: String)
}
