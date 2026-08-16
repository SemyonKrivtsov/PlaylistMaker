package com.example.playlistmaker.ui.library

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class LibraryViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> FavouriteTracksFragment.newInstance()
        else -> PlaylistsFragment.newInstance()
    }

    override fun getItemCount(): Int = 2
}