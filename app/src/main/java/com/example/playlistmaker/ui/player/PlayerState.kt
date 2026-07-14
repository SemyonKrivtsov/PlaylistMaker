package com.example.playlistmaker.ui.player

sealed class PlayerState {

    abstract val progress: String

    data class Playing(override val progress: String) : PlayerState()

    data class Paused(override val progress: String) : PlayerState()

    data object Prepared : PlayerState() {
        override val progress: String = DEFAULT_PROGRESS
    }

    data object Default : PlayerState() {
        override val progress: String = DEFAULT_PROGRESS
    }

    private companion object {
        const val DEFAULT_PROGRESS = "00:00"
    }
}
