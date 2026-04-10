package com.example.playlistmaker.ui.track_recycler_view

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.utils.TimeFormatter

class TrackViewHolder(parentView: View) : RecyclerView.ViewHolder(parentView) {
    private val trackTitle: TextView = parentView.findViewById(R.id.trackTitle)
    private val artistName: TextView = parentView.findViewById(R.id.artistName)
    private val trackTime: TextView = parentView.findViewById(R.id.trackTime)
    private val trackImage: ImageView = parentView.findViewById(R.id.trackImage)

    fun bind(trackModel: Track) {
        trackTitle.text = trackModel.trackName
        artistName.text = trackModel.artistName
        trackTime.text = TimeFormatter.formatMillis(trackModel.trackTimeMillis)

        Glide.with(itemView.context)
            .load(trackModel.artworkUrl100)
            .placeholder(R.drawable.ic_track_placeholder)
            .error(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(RoundedCorners(itemView.context.resources.getDimensionPixelSize(R.dimen.track_image_corner_radius)))
            .into(trackImage)
    }
}