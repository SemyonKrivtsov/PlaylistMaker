package com.example.playlistmaker.ui.player

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.playlistmaker.databinding.PlaylistRowItemBinding
import com.example.playlistmaker.domain.library.model.Playlist

class PlaylistRowAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : ListAdapter<Playlist, PlaylistRowViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistRowViewHolder {
        val binding = PlaylistRowItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistRowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistRowViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
        holder.itemView.setOnClickListener { onPlaylistClick(playlist) }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Playlist>() {
            override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean =
                oldItem == newItem
        }
    }
}
