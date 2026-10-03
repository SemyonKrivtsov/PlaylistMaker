package com.example.playlistmaker.ui.player

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.PlaylistRowItemBinding
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.utils.TrackCountFormatter
import java.io.File

class PlaylistRowViewHolder(private val binding: PlaylistRowItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(playlist: Playlist) {
        binding.playlistTitle.text = playlist.title
        binding.playlistTracksCount.text =
            TrackCountFormatter.format(itemView.resources, playlist.trackIds.size)

        Glide.with(itemView)
            .load(playlist.coverPath?.let { File(it) })
            .placeholder(R.drawable.ic_track_placeholder)
            .error(R.drawable.ic_track_placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners(
                    itemView.resources.getDimensionPixelSize(R.dimen.track_image_corner_radius)
                )
            )
            .into(binding.playlistCover)
    }
}
