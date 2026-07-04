package com.example.playlistmaker.data.search.dto

class TracksSearchResponse(
    val resultCount: Int,
    val results: List<TrackDto>
) : Response()
