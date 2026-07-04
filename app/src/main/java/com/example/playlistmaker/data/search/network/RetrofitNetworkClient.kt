package com.example.playlistmaker.data.search.network

import com.example.playlistmaker.data.search.NetworkClient
import com.example.playlistmaker.data.search.dto.Response
import com.example.playlistmaker.data.search.dto.TracksSearchRequest
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitNetworkClient : NetworkClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val iTunesService = retrofit.create(ITunesApiService::class.java)

    override fun doRequest(dto: Any): Response {
        return if (dto is TracksSearchRequest) {
            try {
                val resp = iTunesService.search(dto.expression).execute()
                val body = resp.body() ?: Response()
                body.apply { resultCode = resp.code() }
            } catch (e: Throwable) {
                Response().apply { resultCode = -1 }
            }
        } else {
            Response().apply { resultCode = 400 }
        }
    }

    companion object {
        private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    }
}
