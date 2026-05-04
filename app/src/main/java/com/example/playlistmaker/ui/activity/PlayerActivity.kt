package com.example.playlistmaker.ui.activity

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.ui.activity.SearchActivity.Companion.EXTRA_TRACK
import com.example.playlistmaker.utils.TimeFormatter
import com.google.android.material.appbar.MaterialToolbar

class PlayerActivity : AppCompatActivity() {

    private lateinit var trackImage: ImageView
    private lateinit var trackName: TextView
    private lateinit var artistName: TextView
    private lateinit var durationValue: TextView
    private lateinit var albumValue: TextView
    private lateinit var yearValue: TextView
    private lateinit var genreValue: TextView
    private lateinit var countryValue: TextView
    private lateinit var albumLabel: TextView
    private lateinit var playButton: ImageButton
    private lateinit var playbackTime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        initializeViews()

        val track = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_TRACK, Track::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_TRACK) as? Track
        }

        if (track != null) {
            bindTrack(track)
        }
    }

    private fun initializeViews() {
        trackImage = findViewById(R.id.trackImage)
        trackName = findViewById(R.id.trackName)
        artistName = findViewById(R.id.artistName)
        durationValue = findViewById(R.id.durationValue)
        albumValue = findViewById(R.id.albumValue)
        yearValue = findViewById(R.id.yearValue)
        genreValue = findViewById(R.id.genreValue)
        countryValue = findViewById(R.id.countryValue)
        albumLabel = findViewById(R.id.albumLabel)
        playButton = findViewById(R.id.playButton)
        playbackTime = findViewById(R.id.playbackTime)
    }

    private fun bindTrack(track: Track) {
        trackName.text = track.trackName
        artistName.text = track.artistName
        durationValue.text = TimeFormatter.formatMillis(track.trackTimeMillis)
        genreValue.text = track.primaryGenreName
        countryValue.text = track.country

        if (track.collectionName.isNullOrEmpty()) {
            albumValue.visibility = View.GONE
            albumLabel.visibility = View.GONE
        } else {
            albumValue.text = track.collectionName
            albumValue.visibility = View.VISIBLE
            albumLabel.visibility = View.VISIBLE
        }

        if (!track.releaseDate.isNullOrEmpty()) {
            yearValue.text = track.releaseDate.substring(0, 4)
        }

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.track_player_image_corner_radius)))
            .into(trackImage)
    }
}