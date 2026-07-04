package com.example.playlistmaker.ui.settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.ui.settings.view_model.SettingsViewModel

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModels {
        Creator.provideSettingsViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.settings) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.shareButton.setOnClickListener {
            viewModel.shareApp()
        }

        binding.supportButton.setOnClickListener {
            viewModel.openSupport()
        }

        binding.offerButton.setOnClickListener {
            viewModel.openTerms()
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }

        viewModel.observeThemeSettings().observe(this) { themeSettings ->
            if (binding.themeSwitcher.isChecked != themeSettings.isDarkThemeEnabled) {
                binding.themeSwitcher.isChecked = themeSettings.isDarkThemeEnabled
            }
        }
    }
}
