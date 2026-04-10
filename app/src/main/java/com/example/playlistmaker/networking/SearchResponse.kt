package com.example.playlistmaker.networking

import com.example.playlistmaker.model.Track

data class SearchResponse(val resultCount: Int, val results: List<Track>)