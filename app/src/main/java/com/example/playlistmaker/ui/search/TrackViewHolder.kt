package com.example.playlistmaker.ui.search

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.TrackViewBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.utils.TimeFormatter

class TrackViewHolder(private val binding: TrackViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(trackModel: Track) {
        binding.trackTitle.text = trackModel.trackName
        binding.artistName.text = trackModel.artistName
        binding.trackTime.text = TimeFormatter.formatMillis(trackModel.trackTimeMillis)

        Glide.with(itemView.context)
            .load(trackModel.artworkUrl100)
            .placeholder(R.drawable.ic_track_placeholder)
            .error(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(RoundedCorners(itemView.context.resources.getDimensionPixelSize(R.dimen.track_image_corner_radius)))
            .into(binding.trackImage)
    }
}
