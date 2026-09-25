package com.example.playlistmaker.ui.player

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.view_model.PlayerViewModel
import com.example.playlistmaker.utils.TimeFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val track: Track by lazy {
        BundleCompat.getParcelable(requireArguments(), ARG_TRACK, Track::class.java)!!
    }
    private val viewModel by viewModel<PlayerViewModel> {
        parametersOf(track)
    }

    private val playlistAdapter = PlaylistRowAdapter { playlist ->
        viewModel.onPlaylistClicked(playlist)
    }

    private var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(
            inflater,
            container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }

        bindTrack(track)

        viewModel.observePlayerState().observe(viewLifecycleOwner) { state ->
            render(state)
        }

        binding.playButton.setOnClickListener {
            viewModel.onPlayButtonClicked()
        }

        binding.likeButton.setOnClickListener {
            viewModel.onFavouriteClicked()
        }

        viewModel.observeFavourite().observe(viewLifecycleOwner) { isFavourite ->
            renderFavourite(isFavourite)
        }

        setUpBottomSheet()

        viewModel.observePlaylists().observe(viewLifecycleOwner) { playlists ->
            playlistAdapter.submitList(playlists)
        }

        viewModel.observeAddTrackResult().observe(viewLifecycleOwner) { result ->
            renderAddTrackResult(result)
        }

        viewModel.onViewCreated()
    }

    private fun setUpBottomSheet() {
        binding.playlistsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = playlistAdapter

        val behavior = BottomSheetBehavior.from(binding.playlistsBottomSheet).apply {
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        bottomSheetBehavior = behavior

        val callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                binding.overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = (slideOffset + 1f) / 2f
            }
        }
        bottomSheetCallback = callback
        behavior.addBottomSheetCallback(callback)

        binding.addToFavouriteButton.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.overlay.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.newPlaylistButton.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(R.id.action_playerFragment_to_newPlaylistFragment)
        }
    }

    private fun renderAddTrackResult(result: AddTrackResult) {
        val message = when (result) {
            is AddTrackResult.Added -> {
                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                getString(R.string.added_to_playlist, result.playlistTitle)
            }

            is AddTrackResult.AlreadyAdded ->
                getString(R.string.already_in_playlist, result.playlistTitle)
        }
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetCallback?.let { bottomSheetBehavior?.removeBottomSheetCallback(it) }
        bottomSheetCallback = null
        bottomSheetBehavior = null
        binding.playlistsRecyclerView.adapter = null
        _binding = null
    }

    private fun bindTrack(track: Track) {
        binding.trackName.text = track.trackName
        binding.artistName.text = track.artistName
        binding.durationValue.text = TimeFormatter.formatMillis(track.trackTimeMillis)
        binding.genreValue.text = track.primaryGenreName
        binding.countryValue.text = track.country

        if (track.collectionName.isNullOrEmpty()) {
            binding.albumValue.visibility = View.GONE
            binding.albumLabel.visibility = View.GONE
        } else {
            binding.albumValue.text = track.collectionName
            binding.albumValue.visibility = View.VISIBLE
            binding.albumLabel.visibility = View.VISIBLE
        }

        if (!track.releaseDate.isNullOrEmpty()) {
            binding.yearValue.text = track.releaseDate.substring(0, 4)
        }

        Glide.with(this)
            .load(track.getCoverArtwork())
            .placeholder(R.drawable.ic_track_placeholder)
            .centerCrop()
            .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.track_player_image_corner_radius)))
            .into(binding.trackImage)
    }

    private fun render(state: PlayerState) {
        binding.playbackTime.text = state.progress

        binding.playButton.setImageResource(
            if (state is PlayerState.Playing) {
                R.drawable.ic_pause_100
            } else {
                R.drawable.ic_play_100
            }
        )
        binding.playButton.isEnabled = state !is PlayerState.Default
    }

    private fun renderFavourite(isFavourite: Boolean) {
        binding.likeButton.setImageResource(
            if (isFavourite) R.drawable.ic_like_filled_25 else R.drawable.ic_like_25
        )
        binding.likeButton.imageTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (isFavourite) R.color.like_active else R.color.white
            )
        )
    }

    companion object {
        const val ARG_TRACK = "track"
    }
}
