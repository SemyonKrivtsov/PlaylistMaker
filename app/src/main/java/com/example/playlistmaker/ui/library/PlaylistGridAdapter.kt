package com.example.playlistmaker.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.playlistmaker.databinding.PlaylistGridItemBinding
import com.example.playlistmaker.domain.library.model.Playlist

class PlaylistGridAdapter : ListAdapter<Playlist, PlaylistGridViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistGridViewHolder {
        val binding = PlaylistGridItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PlaylistGridViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistGridViewHolder, position: Int) {
        holder.bind(getItem(position))
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
