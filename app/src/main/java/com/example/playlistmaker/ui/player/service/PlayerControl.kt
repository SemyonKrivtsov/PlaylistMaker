package com.example.playlistmaker.ui.player.service

import com.example.playlistmaker.ui.player.PlayerState
import kotlinx.coroutines.flow.StateFlow

interface PlayerControl {
    fun getPlayerState(): StateFlow<PlayerState>
    fun startPlayer()
    fun pausePlayer()
    fun showNotification()
    fun hideNotification()
}
