package com.example.playlistmaker.ui.player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.ui.player.PlayerState
import com.example.playlistmaker.utils.TimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicService : Service(), PlayerControl {

    private val binder = MusicServiceBinder()

    private var mediaPlayer: MediaPlayer? = null
    private var trackUrl: String? = null
    private var artistName: String = ""
    private var trackName: String = ""

    private val playerState = MutableStateFlow<PlayerState>(PlayerState.Default)

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    inner class MusicServiceBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        mediaPlayer = MediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder {
        trackUrl = intent?.getStringExtra(EXTRA_TRACK_URL)
        artistName = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        trackName = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        initMediaPlayer()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        releasePlayer()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        releasePlayer()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun getPlayerState(): StateFlow<PlayerState> = playerState.asStateFlow()

    override fun startPlayer() {
        mediaPlayer?.start()
        startTimer()
    }

    override fun pausePlayer() {
        timerJob?.cancel()
        mediaPlayer?.pause()
        playerState.value = PlayerState.Paused(currentPosition())
    }

    override fun showNotification() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
    }

    override fun hideNotification() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    private fun initMediaPlayer() {
        val url = trackUrl
        if (url.isNullOrBlank()) return
        val player = mediaPlayer ?: return
        player.setOnPreparedListener {
            playerState.value = PlayerState.Prepared
        }
        player.setOnCompletionListener {
            timerJob?.cancel()
            playerState.value = PlayerState.Prepared
        }
        player.setOnErrorListener { _, _, _ ->
            timerJob?.cancel()
            playerState.value = PlayerState.Default
            true
        }
        try {
            player.setDataSource(url)
            player.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to prepare track: $url", e)
            playerState.value = PlayerState.Default
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (mediaPlayer?.isPlaying == true) {
                playerState.value = PlayerState.Playing(currentPosition())
                delay(TIMER_DELAY)
            }
        }
    }

    private fun currentPosition(): String =
        TimeFormatter.formatMillis(mediaPlayer?.currentPosition?.toLong() ?: 0L)

    private fun releasePlayer() {
        timerJob?.cancel()
        hideNotification()
        mediaPlayer?.apply {
            setOnPreparedListener(null)
            setOnCompletionListener(null)
            setOnErrorListener(null)
            release()
        }
        mediaPlayer = null
        playerState.value = PlayerState.Default
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.music_service_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.music_service_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.music_notification_title))
            .setContentText(getString(R.string.music_notification_text, artistName, trackName))
            .setSmallIcon(R.drawable.ic_play_100)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

    companion object {
        const val EXTRA_TRACK_URL = "extra_track_url"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        const val EXTRA_TRACK_NAME = "extra_track_name"

        private const val TAG = "MusicService"
        private const val CHANNEL_ID = "music_service_channel"
        private const val NOTIFICATION_ID = 100
        private const val TIMER_DELAY = 300L
    }
}
