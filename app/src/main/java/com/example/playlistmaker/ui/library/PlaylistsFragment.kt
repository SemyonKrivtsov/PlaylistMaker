package com.example.playlistmaker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistsBinding
import com.example.playlistmaker.ui.library.view_model.PlaylistsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<PlaylistsViewModel>()
    private val playlistAdapter = PlaylistGridAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.playlistsRecyclerView.layoutManager = GridLayoutManager(requireContext(), COLUMNS)
        binding.playlistsRecyclerView.adapter = playlistAdapter

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_newPlaylistFragment)
        }

        viewModel.observeState().observe(viewLifecycleOwner) { render(it) }
        viewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.playlistsRecyclerView.adapter = null
        _binding = null
    }

    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Empty -> {
                playlistAdapter.submitList(emptyList())
                binding.playlistsRecyclerView.isVisible = false
                binding.emptyGroup.isVisible = true
            }

            is PlaylistsState.Content -> {
                playlistAdapter.submitList(state.playlists)
                binding.playlistsRecyclerView.isVisible = true
                binding.emptyGroup.isVisible = false
            }
        }
    }

    companion object {
        private const val COLUMNS = 2

        fun newInstance() = PlaylistsFragment()
    }
}
