package com.example.playlistmaker.data.sharing

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.example.playlistmaker.domain.sharing.ExternalNavigator
import com.example.playlistmaker.domain.sharing.model.EmailData

class ExternalNavigatorImpl(private val context: Context) : ExternalNavigator {

    override fun shareLink(link: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, link)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override fun openLink(link: String, errorMessage: String) {
        val viewIntent = Intent(Intent.ACTION_VIEW, link.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (viewIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(viewIntent)
        } else {
            showError(errorMessage)
        }
    }

    override fun openEmail(emailData: EmailData, errorMessage: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailData.email))
            putExtra(Intent.EXTRA_SUBJECT, emailData.subject)
            putExtra(Intent.EXTRA_TEXT, emailData.message)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (emailIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(emailIntent)
        } else {
            showError(errorMessage)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
