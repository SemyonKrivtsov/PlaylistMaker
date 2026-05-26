package com.example.playlistmaker.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
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
import com.example.playlistmaker.R
import com.example.playlistmaker.helpers.SearchHistory
import com.example.playlistmaker.model.Track
import com.example.playlistmaker.networking.SearchResponse
import com.example.playlistmaker.networking.TracksApiService
import com.example.playlistmaker.ui.track_recycler_view.TrackAdapter
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import kotlin.collections.orEmpty

class SearchActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyContainer: ConstraintLayout
    private lateinit var errorImageView: ImageView
    private lateinit var errorTextView: TextView
    private lateinit var reloadButton: MaterialButton
    private lateinit var tracksApiService: TracksApiService

    private var searchValue: String = EMPTY_STRING
    private val tracks: MutableList<Track> = mutableListOf()
    private val historyTracks: MutableList<Track> = mutableListOf()

    private val searchHistory by lazy {
        SearchHistory(getSharedPreferences(SEARCH_HISTORY, MODE_PRIVATE))
    }

    private val trackAdapter = TrackAdapter(tracks) {
        searchHistory.add(it)
        showPlayer(it)
    }

    private val historyAdapter = TrackAdapter(historyTracks) {
        searchHistory.add(it)
        updateTrackHistory()
        showPlayer(it)
    }
    private val retrofit = Retrofit.Builder()
        .baseUrl(ITUNES_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

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

        clearHistoryButton.setOnClickListener {
            searchHistory.clear()
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

        tracksApiService = retrofit.create<TracksApiService>()
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
        tracksApiService.search(searchValue).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val foundTracks = response.body()?.results.orEmpty()
                    if (foundTracks.isEmpty()) {
                        historyContainer.isVisible = false
                        showNotFoundError()
                        Log.d("EmptyTrackResult", "Response body: ${response.body()}")
                    } else {
                        showTracks(foundTracks)
                    }
                } else {
                    showNetworkError()
                    val errorJson = response.errorBody()?.string()
                    Log.e("NetworkError", "Error: $errorJson")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                showNetworkError()
                Log.e("NetworkError", "Error (onFailure): ${t.message}")
            }
        })
    }

    private fun canUpdateTrackHistory(): Boolean {
        val inputEditText = findViewById<EditText>(R.id.inputEditText)
        return inputEditText.hasFocus() && inputEditText.text.isNullOrEmpty()
    }

    private fun updateTrackHistory(): Boolean {
        val history = searchHistory.getHistory()
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

    companion object {
        const val EXTRA_TRACK = "extra_track"
        private const val SAVED_QUERY = "SAVED_QUERY"
        private const val EMPTY_STRING = ""
        private const val SEARCH_HISTORY = "search_history"
        private const val ITUNES_BASE_URL = "https://itunes.apple.com"
    }
}