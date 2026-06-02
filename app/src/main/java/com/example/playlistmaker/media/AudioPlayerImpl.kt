package com.example.playlistmaker.media

import android.media.MediaPlayer

class AudioPlayerImpl : AudioPlayer {

    private val mediaPlayer = MediaPlayer()

    override fun prepare(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener { onPrepared() }
        mediaPlayer.setOnCompletionListener { onCompletion() }
    }

    override fun start() = mediaPlayer.start()

    override fun pause() = mediaPlayer.pause()

    override fun release() = mediaPlayer.release()

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition
}
