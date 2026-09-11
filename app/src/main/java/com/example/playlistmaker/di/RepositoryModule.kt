package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.data.library.FavouriteTracksRepositoryImpl
import com.example.playlistmaker.data.search.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.search.TracksRepositoryImpl
import com.example.playlistmaker.data.settings.SettingsRepositoryImpl
import com.example.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.example.playlistmaker.domain.library.FavouriteTracksRepository
import com.example.playlistmaker.domain.search.SearchHistoryRepository
import com.example.playlistmaker.domain.search.TracksRepository
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.example.playlistmaker.domain.sharing.ExternalNavigator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {

    single<TracksRepository> { TracksRepositoryImpl(get(), get()) }

    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get(), get()) }

    single<SettingsRepository> {
        val sharedPreferences =
            androidContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
        SettingsRepositoryImpl(sharedPreferences)
    }

    single<ExternalNavigator> { ExternalNavigatorImpl(get()) }
    single<FavouriteTracksRepository> { FavouriteTracksRepositoryImpl(get(), get()) }
}