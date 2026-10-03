package com.example.playlistmaker.ui.playlist

import android.content.res.Resources
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.utils.TimeFormatter
import com.example.playlistmaker.utils.TrackCountFormatter

class PlaylistShareTextFormatter(private val resources: Resources) {

    fun format(playlist: Playlist, tracks: List<Track>): String = buildString {
        appendLine(playlist.title)
        if (!playlist.description.isNullOrBlank()) {
            appendLine(playlist.description)
        }
        appendLine(TrackCountFormatter.format(resources, tracks.size))
        tracks.forEachIndexed { index, track ->
            appendLine(
                resources.getString(
                    R.string.share_track_line,
                    index + 1,
                    track.artistName,
                    track.trackName,
                    TimeFormatter.formatMillis(track.trackTimeMillis)
                )
            )
        }
    }.trimEnd()
}
