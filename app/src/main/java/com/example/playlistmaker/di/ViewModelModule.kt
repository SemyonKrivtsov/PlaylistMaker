package com.example.playlistmaker.di

import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.library.view_model.FavouriteTracksViewModel
import com.example.playlistmaker.ui.library.view_model.PlaylistsViewModel
import com.example.playlistmaker.ui.player.view_model.PlayerViewModel
import com.example.playlistmaker.ui.search.view_model.SearchViewModel
import com.example.playlistmaker.ui.settings.view_model.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { (track: Track) -> PlayerViewModel(track, get(), get()) }
    viewModel { FavouriteTracksViewModel(get()) }
    viewModel { PlaylistsViewModel() }
}
