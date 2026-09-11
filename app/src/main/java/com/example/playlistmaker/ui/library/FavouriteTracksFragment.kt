package com.example.playlistmaker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentFavouriteTracksBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.library.view_model.FavouriteTracksViewModel
import com.example.playlistmaker.ui.player.PlayerFragment
import com.example.playlistmaker.ui.search.TrackAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavouriteTracksFragment : Fragment() {

    private var _binding: FragmentFavouriteTracksBinding? = null
    private val binding get() = _binding!!
    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(tracks) { onTrackClick(it) }

    private val viewModel by viewModel<FavouriteTracksViewModel>()

    private fun onTrackClick(track: Track) {
        if (viewModel.clickDebounce()) {
            findNavController().navigate(
                R.id.action_libraryFragment_to_playerFragment,
                bundleOf(PlayerFragment.ARG_TRACK to track)
            )
        }
    }

    private fun render(state: FavouriteTracksState) {
        when (state) {
            is FavouriteTracksState.Empty -> {
                binding.recyclerView.isVisible = false
                binding.errorGroup.isVisible = true
            }

            is FavouriteTracksState.Content -> {
                tracks.clear()
                tracks.addAll(state.tracks)
                trackAdapter.notifyDataSetChanged()
                binding.recyclerView.isVisible = true
                binding.errorGroup.isVisible = false
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = trackAdapter
        viewModel.observeState().observe(viewLifecycleOwner) { render(it) }
    }

    companion object {
        fun newInstance() = FavouriteTracksFragment()
    }
}