package com.example.playlistmaker.ui.playlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.domain.library.model.Playlist
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.playlist.view_model.PlaylistViewModel
import com.example.playlistmaker.ui.search.TrackAdapter
import com.example.playlistmaker.utils.TrackCountFormatter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class PlaylistFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!

    private val args: PlaylistFragmentArgs by navArgs()
    private val viewModel by viewModel<PlaylistViewModel> { parametersOf(args.playlistId) }

    private val tracks = mutableListOf<Track>()
    private val trackAdapter = TrackAdapter(
        tracks = tracks,
        onTrackClick = { onTrackClick(it) },
        onTrackLongClick = { showDeleteTrackDialog(it) }
    )

    private var menuBehavior: BottomSheetBehavior<LinearLayout>? = null
    private var menuCallback: BottomSheetBehavior.BottomSheetCallback? = null
    private val hideMenuOnBack = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            menuBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.tracksRecyclerView.adapter = trackAdapter

        setUpTracksBottomSheet()
        setUpMenuBottomSheet()

        binding.shareButton.setOnClickListener { viewModel.onShareClicked() }
        binding.menuButton.setOnClickListener {
            menuBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }

        viewModel.observeState().observe(viewLifecycleOwner) { render(it) }
        viewModel.loadPlaylist()

        viewModel.observeNothingToShare().observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                R.string.playlist_nothing_to_share,
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.observePlaylistDeleted().observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menuCallback?.let { menuBehavior?.removeBottomSheetCallback(it) }
        menuCallback = null
        menuBehavior = null
        binding.tracksRecyclerView.adapter = null
        _binding = null
    }

    // Список треков начинается под кнопками «Поделиться» и «Меню» и не скрывается
    private fun setUpTracksBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.tracksBottomSheet)
        binding.root.doOnLayout { root ->
            val margin = resources.getDimensionPixelSize(R.dimen.playlist_tracks_sheet_margin)
            behavior.peekHeight = (root.height - binding.shareButton.bottom - margin)
                .coerceAtLeast(margin * MIN_PEEK_MARGINS)
        }
    }

    private fun setUpMenuBottomSheet() {
        // Флаг живёт дольше view: если меню не успело скрыться до ухода с экрана, он остался бы включён
        hideMenuOnBack.isEnabled = false
        val behavior = BottomSheetBehavior.from(binding.menuBottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
        }
        menuBehavior = behavior

        val callback = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                val isMenuShown = newState != BottomSheetBehavior.STATE_HIDDEN
                binding.overlay.isVisible = isMenuShown
                hideMenuOnBack.isEnabled = isMenuShown
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = (slideOffset + 1f).coerceIn(0f, 1f)
            }
        }
        menuCallback = callback
        behavior.addBottomSheetCallback(callback)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, hideMenuOnBack)

        binding.overlay.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.menuShare.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            viewModel.onShareClicked()
        }

        binding.menuEdit.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            findNavController().navigate(
                PlaylistFragmentDirections.actionPlaylistFragmentToEditPlaylistFragment(args.playlistId)
            )
        }

        binding.menuDelete.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            showDeletePlaylistDialog()
        }
    }

    private fun render(state: PlaylistScreenState) {
        val playlist = state.playlist

        binding.title.text = playlist.title
        binding.description.text = playlist.description
        binding.description.isVisible = !playlist.description.isNullOrBlank()

        val trackCount = TrackCountFormatter.format(resources, state.tracks.size)
        binding.info.text = getString(
            R.string.playlist_info,
            resources.getQuantityString(
                R.plurals.minutes_count,
                state.durationMinutes,
                state.durationMinutes
            ),
            trackCount
        )

        Glide.with(this)
            .load(playlist.coverPath?.let { File(it) })
            .placeholder(R.drawable.ic_playlist_placeholder)
            .error(R.drawable.ic_playlist_placeholder)
            .fallback(R.drawable.ic_playlist_placeholder)
            .centerCrop()
            .into(binding.cover)

        renderMenuHeader(playlist, trackCount)
        renderTracks(state.tracks)
    }

    private fun renderMenuHeader(playlist: Playlist, trackCount: String) {
        val header = binding.menuPlaylistRow
        header.playlistTitle.text = playlist.title
        header.playlistTracksCount.text = trackCount

        Glide.with(this)
            .load(playlist.coverPath?.let { File(it) })
            .placeholder(R.drawable.ic_track_placeholder)
            .error(R.drawable.ic_track_placeholder)
            .fallback(R.drawable.ic_track_placeholder)
            .transform(
                CenterCrop(),
                RoundedCorners(resources.getDimensionPixelSize(R.dimen.track_image_corner_radius))
            )
            .into(header.playlistCover)
    }

    private fun renderTracks(newTracks: List<Track>) {
        tracks.clear()
        tracks.addAll(newTracks)
        trackAdapter.notifyDataSetChanged()
        binding.tracksRecyclerView.isVisible = newTracks.isNotEmpty()
        binding.emptyTracksMessage.isVisible = newTracks.isEmpty()
    }

    private fun onTrackClick(track: Track) {
        if (viewModel.clickDebounce()) {
            findNavController().navigate(
                PlaylistFragmentDirections.actionPlaylistFragmentToPlayerFragment(track)
            )
        }
    }

    private fun showDeleteTrackDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistAlertDialogTheme)
            .setMessage(R.string.dialog_delete_track)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.deleteTrack(track) }
            .show()
    }

    private fun showDeletePlaylistDialog() {
        val title = viewModel.observeState().value?.playlist?.title.orEmpty()
        MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistAlertDialogTheme)
            .setMessage(getString(R.string.dialog_delete_playlist, title))
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> viewModel.deletePlaylist() }
            .show()
    }

    companion object {
        private const val MIN_PEEK_MARGINS = 4
    }
}
