package com.example.playlistmaker.ui.library

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistGridItemBinding
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.utils.TrackCountFormatter
import java.io.File

class PlaylistGridViewHolder(private val binding: PlaylistGridItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(playlist: Playlist) {
        binding.playlistTitle.text = playlist.title
        binding.playlistTracksCount.text =
            TrackCountFormatter.format(itemView.resources, playlist.trackIds.size)

        Glide.with(itemView)
            .load(playlist.coverPath?.let { File(it) })
            .placeholder(R.drawable.ic_playlist_placeholder)
            .error(R.drawable.ic_playlist_placeholder)
            .fallback(R.drawable.ic_playlist_placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners(
                    itemView.resources.getDimensionPixelSize(R.dimen.playlist_item_corner_radius)
                )
            )
            .into(binding.playlistCover)
    }
}
