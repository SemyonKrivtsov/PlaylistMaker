package com.example.playlistmaker.utils

import java.text.SimpleDateFormat
import java.util.Locale

object TimeFormatter {
    private val trackTimeFormatter by lazy {
        SimpleDateFormat("mm:ss", Locale.getDefault())
    }

    fun formatMillis(millis: Long): String {
        return trackTimeFormatter.format(millis)
    }
}