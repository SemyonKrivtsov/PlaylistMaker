package com.example.playlistmaker.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.api.SearchHistoryInteractor
import com.example.playlistmaker.domain.api.TracksInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.ui.track_recycler_view.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar

class SearchActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyContainer: ConstraintLayout
    private lateinit var errorImageView: ImageView
    private lateinit var errorTextView: TextView
    private lateinit var reloadButton: MaterialButton
    private lateinit var progressBar: ProgressBar

    private var searchValue: String = EMPTY_STRING
    private val tracks: MutableList<Track> = mutableListOf()
    private val historyTracks: MutableList<Track> = mutableListOf()

    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable { searchRequest() }
    private var isClickAllowed = true

    private val tracksInteractor: TracksInteractor = Creator.provideTracksInteractor()
    private val searchHistoryInteractor: SearchHistoryInteractor =
        Creator.provideSearchHistoryInteractor()

    private val trackAdapter = TrackAdapter(tracks) {
        if (clickDebounce()) {
            searchHistoryInteractor.add(it)
            showPlayer(it)
        }
    }

    private val historyAdapter = TrackAdapter(historyTracks) {
        if (clickDebounce()) {
            searchHistoryInteractor.add(it)
            updateTrackHistory()
            showPlayer(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val clearButton = findViewById<ImageView>(R.id.clearIcon)
        historyContainer = findViewById<ConstraintLayout>(R.id.history)
        historyRecyclerView = findViewById<RecyclerView>(R.id.historyRecyclerView)
        val clearHistoryButton = findViewById<MaterialButton>(R.id.clearHistory)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        clearHistoryButton.setOnClickListener {
            searchHistoryInteractor.clear()
            historyContainer.isVisible = false
        }

        clearButton.setOnClickListener {
            inputEditText.setText(EMPTY_STRING)
            val inputMethodManager =
                getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(clearButton.windowToken, 0)
            hideErrorLayout()
        }

        inputEditText.doOnTextChanged { text, start, before, count ->
            clearButton.isVisible = !text.isNullOrEmpty()
            searchValue = text.toString()
            historyContainer.isVisible = updateTrackHistory()
            if (!text.isNullOrEmpty()) {
                searchDebounce()
            }
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchRequest()
                true
            } else {
                false
            }
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            historyContainer.isVisible = updateTrackHistory()
        }

        recyclerView = findViewById(R.id.recyclerView)
        errorImageView = findViewById(R.id.errorImage)
        errorTextView = findViewById(R.id.errorMessage)
        reloadButton = findViewById(R.id.refreshButton)

        recyclerView.adapter = trackAdapter
        historyRecyclerView.adapter = historyAdapter
        hideErrorLayout()

        reloadButton.setOnClickListener {
            searchRequest()
        }
        updateTrackHistory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SAVED_QUERY, searchValue)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        val restoredQuery = savedInstanceState.getString(SAVED_QUERY)

        if (!restoredQuery.isNullOrEmpty()) {
            searchValue = savedInstanceState.getString(SAVED_QUERY, EMPTY_STRING)
            inputEditText.setText(searchValue)
        }
    }

    private fun showTracks(newTracks: List<Track>) {
        hideErrorLayout()
        tracks.addAll(newTracks)
        recyclerView.isVisible = true
        trackAdapter.notifyDataSetChanged()
    }

    private fun hideTracks() {
        tracks.clear()
        recyclerView.isVisible = false
        trackAdapter.notifyDataSetChanged()
    }

    private fun hideErrorLayout() {
        hideTracks()
        errorImageView.isVisible = false
        errorTextView.isVisible = false
        reloadButton.isVisible = false
    }

    private fun showNotFoundError() {
        hideTracks()
        errorImageView.setImageResource(R.drawable.ic_not_found_120)
        errorTextView.setText(R.string.notFoundTracksMsg)
        errorImageView.isVisible = true
        errorTextView.isVisible = true
        reloadButton.isVisible = false
    }

    private fun showNetworkError() {
        hideTracks()
        errorImageView.setImageResource(R.drawable.ic_network_failed_120)
        errorTextView.setText(R.string.network_failed_msg)
        errorImageView.isVisible = true
        errorTextView.isVisible = true
        reloadButton.isVisible = true
    }

    private fun searchRequest() {
        hideErrorLayout()
        progressBar.isVisible = true
        tracksInteractor.searchTracks(searchValue, object : TracksInteractor.TracksConsumer {
            override fun consume(foundTracks: List<Track>?) {
                runOnUiThread {
                    progressBar.isVisible = false
                    when {
                        foundTracks == null -> showNetworkError()
                        foundTracks.isEmpty() -> {
                            historyContainer.isVisible = false
                            showNotFoundError()
                        }
                        else -> showTracks(foundTracks)
                    }
                }
            }
        })
    }

    private fun canUpdateTrackHistory(): Boolean {
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        return inputEditText.hasFocus() && inputEditText.text.isNullOrEmpty()
    }

    private fun updateTrackHistory(): Boolean {
        val history = searchHistoryInteractor.getHistory()
        val isShowHistory = canUpdateTrackHistory() && history.isNotEmpty()

        if (isShowHistory) {
            historyTracks.clear()
            historyTracks.addAll(history)
            historyAdapter.notifyDataSetChanged()
        }
        return isShowHistory
    }

    private fun showPlayer(track: Track) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(EXTRA_TRACK, track)
        }
        startActivity(intent)
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    private fun clickDebounce() : Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    companion object {
        const val EXTRA_TRACK = "extra_track"
        private const val SAVED_QUERY = "SAVED_QUERY"
        private const val EMPTY_STRING = ""
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
        private const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}