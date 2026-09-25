package com.example.playlistmaker.ui.library

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistGridItemBinding
import com.example.playlistmaker.domain.library.model.Playlist
import java.io.File

class PlaylistGridViewHolder(private val binding: PlaylistGridItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(playlist: Playlist) {
        binding.playlistTitle.text = playlist.title
        binding.playlistTracksCount.text = itemView.resources.getQuantityString(
            R.plurals.tracks_count,
            playlist.trackIds.size,
            playlist.trackIds.size
        )

        Glide.with(itemView)
            .load(playlist.coverPath?.let { File(it) })
            .placeholder(R.drawable.ic_track_placeholder)
            .error(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(
                RoundedCorners(
                    itemView.resources.getDimensionPixelSize(R.dimen.playlist_item_corner_radius)
                )
            )
            .into(binding.playlistCover)
    }
}
