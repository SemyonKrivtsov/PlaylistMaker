package com.example.playlistmaker.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.databinding.TrackViewBinding
import com.example.playlistmaker.domain.search.model.Track

class TrackAdapter(
    private val tracks: List<Track>,
    private val onTrackLongClick: ((Track) -> Unit)? = null,
    private val onTrackClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = TrackViewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        val holder = TrackViewHolder(binding)
        holder.itemView.setOnClickListener {
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onTrackClick(tracks[position])
            }
        }
        if (onTrackLongClick != null) {
            holder.itemView.setOnLongClickListener {
                val position = holder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTrackLongClick.invoke(tracks[position])
                }
                true
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size
}
