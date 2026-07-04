package com.example.playlistmaker.ui.player

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.view_model.PlayerViewModel
import com.example.playlistmaker.utils.TimeFormatter
import com.google.gson.Gson

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val track = intent.getStringExtra(EXTRA_TRACK)?.let {
            Gson().fromJson(it, Track::class.java)
        }
        track?.let { bindTrack(it) }

        val url = track?.previewUrl.orEmpty()
        viewModel = ViewModelProvider(this, PlayerViewModel.getFactory(url))
            .get(PlayerViewModel::class.java)

        viewModel.observePlayerState().observe(this) { state ->
            binding.playButton.isEnabled = state != PlayerViewModel.STATE_DEFAULT
            binding.playButton.setImageResource(
                if (state == PlayerViewModel.STATE_PLAYING) {
                    R.drawable.ic_pause_100
                } else {
                    R.drawable.ic_play_100
                }
            )
        }

        viewModel.observeProgressTime().observe(this) {
            binding.playbackTime.text = it
        }

        binding.playButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun bindTrack(track: Track) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.durationValue.text = TimeFormatter.formatMillis(track.trackTimeMillis)
        binding.genreValue.text = track.primaryGenreName
        binding.countryValue.text = track.country

        if (track.collectionName.isNullOrEmpty()) {
            binding.albumValue.visibility = View.GONE
            binding.albumLabel.visibility = View.GONE
        } else {
            binding.albumValue.text = track.collectionName
            binding.albumValue.visibility = View.VISIBLE
            binding.albumLabel.visibility = View.VISIBLE
        }

        if (!track.releaseDate.isNullOrEmpty()) {
            binding.yearValue.text = track.releaseDate.substring(0, 4)
        }

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.track_player_image_corner_radius)))
            .into(binding.trackImage)
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
    }
}
