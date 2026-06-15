package com.example.playlistmaker.data

import android.media.MediaPlayer
import com.example.playlistmaker.domain.api.PlayerRepository

class PlayerRepositoryImpl : PlayerRepository {

    private val mediaPlayer = MediaPlayer()

    override fun prepare(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        mediaPlayer.setOnPreparedListener { onPrepared() }
        mediaPlayer.setOnCompletionListener { onCompletion() }
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
    }

    override fun start() = mediaPlayer.start()

    override fun pause() = mediaPlayer.pause()

    override fun release() = mediaPlayer.release()

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition
}
