package com.example.playlistmaker.di

import com.example.playlistmaker.R
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.search.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.search.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.impl.SharingInteractorImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val interactorModule = module {
    single<TracksInteractor> { TracksInteractorImpl(get()) }
    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    single<SettingsInteractor> { SettingsInteractorImpl(get()) }
    single<SharingInteractor> {
        SharingInteractorImpl(
            get(),
            androidContext().getString(R.string.course_url),
            androidContext().getString(R.string.practicum_offer_url),
            get()
        )
    }
}