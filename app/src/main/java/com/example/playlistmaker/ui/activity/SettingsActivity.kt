package com.example.playlistmaker.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val sharingButton = findViewById<ImageButton>(R.id.shareButton)
        sharingButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain")
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.course_url))
            startActivity(Intent.createChooser(shareIntent, null))
        }

        val supportButton = findViewById<ImageButton>(R.id.supportButton)
        supportButton.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO)
            supportIntent.setData(Uri.parse("mailto:"))
            val email = getString(R.string.email)
            val message = getString(R.string.text_message)
            val subject = getString(R.string.subject)

            supportIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            supportIntent.putExtra(Intent.EXTRA_TEXT, message)
            supportIntent.putExtra(Intent.EXTRA_SUBJECT, subject)

            if (supportIntent.resolveActivity(packageManager) != null) {
                startActivity(supportIntent)
            } else {
                val errorMsg = getString(R.string.error_msg_no_email_client)
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        val offerButton = findViewById<ImageButton>(R.id.offerButton)
        offerButton.setOnClickListener {
            val offerUri = getString(R.string.practicum_offer_url)
            val offerIntent = Intent(Intent.ACTION_VIEW, Uri.parse(offerUri))

            if (offerIntent.resolveActivity(packageManager) != null) {
                startActivity(offerIntent)
            } else {
                val errorMsg = getString(R.string.error_msg_no_browsers)
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        val app = applicationContext as App
        themeSwitcher.isChecked = app.darkTheme
        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            app.switchTheme(checked)
        }
    }
}