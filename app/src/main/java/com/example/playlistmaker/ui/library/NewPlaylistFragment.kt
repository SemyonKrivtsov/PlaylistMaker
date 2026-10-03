package com.example.playlistmaker.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentNewPlaylistBinding
import com.example.playlistmaker.ui.library.view_model.NewPlaylistViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlaylistFragment : Fragment() {

    private var _binding: FragmentNewPlaylistBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModel<NewPlaylistViewModel>()

    private val pickCoverLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onCoverSelected(uri.toString())
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { onBackAction() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { onBackAction() }

        binding.playlistName.doOnTextChanged { text, _, _, _ ->
            viewModel.onTitleChanged(text?.toString().orEmpty())
        }
        binding.playlistDescription.doOnTextChanged { text, _, _, _ ->
            viewModel.onDescriptionChanged(text?.toString().orEmpty())
        }

        binding.selectImage.setOnClickListener {
            pickCoverLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.createButton.setOnClickListener { viewModel.createPlaylist() }

        viewModel.observeCreateAvailable().observe(viewLifecycleOwner) { isAvailable ->
            binding.createButton.isEnabled = isAvailable
        }

        viewModel.observeCoverUri().observe(viewLifecycleOwner) { uri ->
            renderCover(uri)
        }

        viewModel.observePlaylistCreated().observe(viewLifecycleOwner) { name ->
            Toast.makeText(
                requireContext(),
                getString(R.string.dialog_created, name),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigateUp()
        }

        viewModel.observeCreateError().observe(viewLifecycleOwner) {
            Toast.makeText(
                requireContext(),
                R.string.create_playlist_error,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderCover(uri: String?) {
        if (uri == null) {
            binding.coverImage.isVisible = false
            binding.coverPlaceholder.isVisible = true
            binding.selectImage.setBackgroundResource(R.drawable.cover_placeholder_border)
            return
        }

        binding.coverPlaceholder.isVisible = false
        binding.coverImage.isVisible = true
        binding.selectImage.background = null

        Glide.with(this)
            .load(uri.toUri())
            .centerCrop()
            .transform(
                RoundedCorners(resources.getDimensionPixelSize(R.dimen.playlist_cover_corner_radius))
            )
            .into(binding.coverImage)
    }

    private fun onBackAction() {
        if (viewModel.hasUnsavedData()) {
            showExitDialog()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun showExitDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.PlaylistAlertDialogTheme)
            .setTitle(R.string.dialog_finish_creating_title)
            .setMessage(R.string.dialog_finish_creating_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.finish) { _, _ -> findNavController().navigateUp() }
            .show()
    }
}
