package com.example.playlistmaker.utils

import android.content.res.Resources
import com.example.playlistmaker.R

object TrackCountFormatter {

    fun format(resources: Resources, count: Int): String {
        val mod10 = count % 10
        val mod100 = count % 100
        val stringRes = when {
            mod10 == 1 && mod100 != 11 -> R.string.tracks_count_one
            mod10 in 2..4 && mod100 !in 12..14 -> R.string.tracks_count_few
            else -> R.string.tracks_count_many
        }
        return resources.getString(stringRes, count)
    }
}
