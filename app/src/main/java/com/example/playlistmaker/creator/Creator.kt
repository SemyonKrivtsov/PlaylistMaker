package com.example.playlistmaker.creator

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.R
import com.example.playlistmaker.data.search.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.search.TracksRepositoryImpl
import com.example.playlistmaker.data.search.network.RetrofitNetworkClient
import com.example.playlistmaker.data.settings.SettingsRepositoryImpl
import com.example.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.example.playlistmaker.data.storage.PrefsStorageClient
import com.example.playlistmaker.domain.search.SearchHistoryInteractor
import com.example.playlistmaker.domain.search.SearchHistoryRepository
import com.example.playlistmaker.domain.search.TracksInteractor
import com.example.playlistmaker.domain.search.TracksRepository
import com.example.playlistmaker.domain.search.impl.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.search.impl.TracksInteractorImpl
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.example.playlistmaker.domain.settings.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.sharing.ExternalNavigator
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.impl.SharingInteractorImpl
import com.example.playlistmaker.domain.sharing.model.EmailData
import com.example.playlistmaker.ui.search.view_model.SearchViewModel
import com.example.playlistmaker.ui.settings.view_model.SettingsViewModel
import com.google.gson.reflect.TypeToken

object Creator {

    private const val SEARCH_HISTORY_PREFS = "search_history"
    private const val TRACK_HISTORY_KEY = "tracks_history_list"

    private lateinit var application: Application

    fun initApplication(application: Application) {
        this.application = application
    }

    private fun getTracksRepository(): TracksRepository {
        return TracksRepositoryImpl(RetrofitNetworkClient())
    }

    private fun provideTracksInteractor(): TracksInteractor {
        return TracksInteractorImpl(getTracksRepository())
    }

    private fun getSearchHistoryRepository(): SearchHistoryRepository {
        return SearchHistoryRepositoryImpl(
            PrefsStorageClient<ArrayList<Track>>(
                context = application,
                prefsName = SEARCH_HISTORY_PREFS,
                dataKey = TRACK_HISTORY_KEY,
                type = object : TypeToken<ArrayList<Track>>() {}.type,
            )
        )
    }

    private fun provideSearchHistoryInteractor(): SearchHistoryInteractor {
        return SearchHistoryInteractorImpl(getSearchHistoryRepository())
    }

    private fun getSettingsRepository(): SettingsRepository {
        return SettingsRepositoryImpl(application)
    }

    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository())
    }

    private fun getExternalNavigator(): ExternalNavigator {
        return ExternalNavigatorImpl(application)
    }

    private fun provideSharingInteractor(): SharingInteractor {
        return SharingInteractorImpl(
            externalNavigator = getExternalNavigator(),
            shareAppLink = application.getString(R.string.course_url),
            termsLink = application.getString(R.string.practicum_offer_url),
            supportEmailData = EmailData(
                email = application.getString(R.string.email),
                subject = application.getString(R.string.subject),
                message = application.getString(R.string.text_message),
            ),
        )
    }

    fun provideSearchViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SearchViewModel(provideTracksInteractor(), provideSearchHistoryInteractor())
        }
    }

    fun provideSettingsViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            SettingsViewModel(provideSharingInteractor(), provideSettingsInteractor())
        }
    }
}
