package com.example.playlistmaker.data.search.network

import com.example.playlistmaker.data.search.NetworkClient
import com.example.playlistmaker.data.search.dto.Response
import com.example.playlistmaker.data.search.dto.TracksSearchRequest
import kotlinx.coroutines.CancellationException

class RetrofitNetworkClient(private val iTunesService: ITunesApiService) : NetworkClient {

    override suspend fun doRequest(dto: Any): Response {
        if (dto !is TracksSearchRequest) return Response().apply {
            resultCode = 400
        }

        return try {
            iTunesService.search(dto.expression).apply {
                resultCode = 200
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Response().apply { resultCode = -1 }
        }
    }
}
