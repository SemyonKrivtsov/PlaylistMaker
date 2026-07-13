package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import com.example.playlistmaker.R
import com.example.playlistmaker.data.StorageClient
import com.example.playlistmaker.data.search.NetworkClient
import com.example.playlistmaker.data.search.network.ITunesApiService
import com.example.playlistmaker.data.search.network.RetrofitNetworkClient
import com.example.playlistmaker.data.storage.PrefsStorageClient
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.sharing.model.EmailData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit.Builder
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {
    single<ITunesApiService> {
        Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesApiService::class.java)
    }

    single {
        androidContext()
            .getSharedPreferences("search_history", Context.MODE_PRIVATE)
    }

    factory { Gson() }
    factory { MediaPlayer() }
    factory { Handler(Looper.getMainLooper()) }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    single<StorageClient<ArrayList<Track>>> {
        PrefsStorageClient(
            "tracks_history_list",
            object : TypeToken<ArrayList<Track>>() {}.type,
            get(),
            get()
        )
    }

    single {
        EmailData(
            email = androidContext().getString(R.string.email),
            subject = androidContext().getString(R.string.subject),
            message = androidContext().getString(R.string.text_message),
        )
    }
}