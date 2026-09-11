package com.example.playlistmaker.ui.player

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.view_model.PlayerViewModel
import com.example.playlistmaker.utils.TimeFormatter
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
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
