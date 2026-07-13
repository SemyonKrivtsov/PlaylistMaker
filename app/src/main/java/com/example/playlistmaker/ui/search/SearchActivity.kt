package com.example.playlistmaker.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.domain.search.model.Track
import com.example.playlistmaker.ui.player.PlayerActivity
import com.example.playlistmaker.ui.search.view_model.SearchViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private val viewModel by viewModel<SearchViewModel>()

    private val tracks: MutableList<Track> = mutableListOf()
    private val historyTracks: MutableList<Track> = mutableListOf()

    private val trackAdapter = TrackAdapter(tracks) { onTrackClick(it) }
    private val historyAdapter = TrackAdapter(historyTracks) { onTrackClick(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.recyclerView.adapter = trackAdapter
        binding.historyRecyclerView.adapter = historyAdapter

        binding.clearHistory.setOnClickListener {
            viewModel.clearHistory()
        }

        binding.clearIcon.setOnClickListener {
            binding.inputEditText.setText("")
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(binding.clearIcon.windowToken, 0)
        }

        binding.inputEditText.doOnTextChanged { text, _, _, _ ->
            binding.clearIcon.isVisible = !text.isNullOrEmpty()
            val query = text?.toString().orEmpty()
            if (query.isEmpty()) {
                viewModel.showHistory()
            } else {
                viewModel.searchDebounce(query)
            }
        }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.searchImmediately(binding.inputEditText.text.toString())
                true
            } else {
                false
            }
        }

        binding.inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && binding.inputEditText.text.isNullOrEmpty()) {
                viewModel.showHistory()
            }
        }

        binding.refreshButton.setOnClickListener {
            viewModel.searchImmediately(binding.inputEditText.text.toString())
        }

        viewModel.observeState().observe(this) { render(it) }
    }

    private fun render(state: SearchState) {
        when (state) {
            is SearchState.Loading -> showLoading()
            is SearchState.Content -> showTracks(state.tracks)
            is SearchState.History -> showHistory(state.tracks)
            is SearchState.NothingFound -> showNotFoundError()
            is SearchState.ConnectionError -> showNetworkError()
            is SearchState.Empty -> showEmpty()
        }
    }

    private fun hideAll() {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.history.isVisible = false
        binding.errorImage.isVisible = false
        binding.errorMessage.isVisible = false
        binding.refreshButton.isVisible = false
    }

    private fun showLoading() {
        hideAll()
        binding.progressBar.isVisible = true
    }

    private fun showTracks(newTracks: List<Track>) {
        hideAll()
        tracks.clear()
        tracks.addAll(newTracks)
        trackAdapter.notifyDataSetChanged()
        binding.recyclerView.isVisible = true
    }

    private fun showHistory(history: List<Track>) {
        hideAll()
        historyTracks.clear()
        historyTracks.addAll(history)
        historyAdapter.notifyDataSetChanged()
        binding.history.isVisible = true
    }

    private fun showNotFoundError() {
        hideAll()
        binding.errorImage.setImageResource(R.drawable.ic_not_found_120)
        binding.errorMessage.setText(R.string.notFoundTracksMsg)
        binding.errorImage.isVisible = true
        binding.errorMessage.isVisible = true
    }

    private fun showNetworkError() {
        hideAll()
        binding.errorImage.setImageResource(R.drawable.ic_network_failed_120)
        binding.errorMessage.setText(R.string.network_failed_msg)
        binding.errorImage.isVisible = true
        binding.errorMessage.isVisible = true
        binding.refreshButton.isVisible = true
    }

    private fun showEmpty() {
        hideAll()
    }

    private fun onTrackClick(track: Track) {
        if (viewModel.clickDebounce()) {
            viewModel.addTrackToHistory(track)
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_TRACK, track)
            }
            startActivity(intent)
        }
    }
}
