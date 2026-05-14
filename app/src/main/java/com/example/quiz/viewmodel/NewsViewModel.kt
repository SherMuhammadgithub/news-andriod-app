package com.example.quiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.Article
import com.example.quiz.data.model.SUPPORTED_COUNTRIES
import com.example.quiz.data.model.NewsCountry
import com.example.quiz.data.repository.NewsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

// ── UI State ──────────────────────────────────────────────────────────────────

/**
 * NewsUiState — all the data the Home screen needs to render itself.
 *
 * Keeping everything in one state object means the UI only needs to
 * observe a single StateFlow instead of many separate ones.
 *
 * @param articles       The list of articles currently displayed.
 * @param isLoading      True while a network request is in progress → shows spinner.
 * @param errorMessage   Non-null when the last request failed → shows error banner.
 * @param searchQuery    The current text in the search bar.
 * @param selectedCountry The country currently chosen in the dropdown filter.
 */
data class NewsUiState(
    val articles:        List<Article>  = emptyList(),
    val isLoading:       Boolean        = false,
    val errorMessage:    String?        = null,
    val searchQuery:     String         = "",
    val selectedCountry: NewsCountry    = SUPPORTED_COUNTRIES[0] // default = "All Countries"
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

/**
 * NewsViewModel — manages all business logic for the news screens.
 *
 * MVVM flow:
 *   NewsHomeScreen (observes uiState) ← NewsViewModel → NewsRepository → Retrofit → gnews.io
 *
 * Key responsibilities:
 *   1. Fetch top headlines on first load
 *   2. Re-fetch when the user changes country
 *   3. Trigger search when the user types (with debounce to avoid too many API calls)
 *   4. Expose a refresh action for the refresh button
 *   5. Expose article lookup by index for the Detail screen
 */
@OptIn(FlowPreview::class)
class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    // ── State ─────────────────────────────────────────────────────────────────

    // Private mutable — only this ViewModel can change it
    private val _uiState = MutableStateFlow(NewsUiState())

    // Public read-only — screens observe this
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // Separate flow just for the search query so we can debounce it.
    // Debounce = wait until the user stops typing for 500ms before firing the API call.
    // Without debounce, every keystroke would fire a separate network request.
    private val _searchQuery = MutableStateFlow("")

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        // Load top headlines immediately when the ViewModel is created (app opens)
        fetchTopHeadlines()

        // Watch the search query for changes, debounced to avoid rapid API calls.
        // drop(1) skips the initial empty string so we don't search on startup.
        viewModelScope.launch {
            _searchQuery
                .drop(1)                         // skip the initial "" value
                .debounce(500L)                  // wait 500ms after last keystroke
                .distinctUntilChanged()           // skip if value hasn't actually changed
                .collect { query ->
                    if (query.isBlank()) {
                        // User cleared search → go back to top headlines
                        fetchTopHeadlines()
                    } else {
                        // User typed something → search for it
                        performSearch(query)
                    }
                }
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Fetch top headlines for the currently selected country.
     * Called on init and when the user selects a different country.
     */
    private fun fetchTopHeadlines() {
        viewModelScope.launch {
            // Show spinner, clear any previous error
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val countryCode = _uiState.value.selectedCountry.code
            val result = repository.getTopHeadlines(country = countryCode)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    articles     = result.getOrDefault(emptyList()),
                    isLoading    = false,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = friendlyError(result.exceptionOrNull())
                )
            }
        }
    }

    /**
     * Search articles by keyword using the currently selected country as scope.
     * Called automatically by the debounced search query observer.
     */
    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val countryCode = _uiState.value.selectedCountry.code
            val result = repository.searchNews(query = query, country = countryCode)

            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    articles     = result.getOrDefault(emptyList()),
                    isLoading    = false,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = friendlyError(result.exceptionOrNull())
                )
            }
        }
    }

    // ── Public actions — called by UI screens ──────────────────────────────────

    /**
     * Called when the user types in the search bar.
     * Updates both the visible text in the field and the debounced search trigger.
     */
    fun onSearchQueryChange(query: String) {
        _uiState.value  = _uiState.value.copy(searchQuery = query)
        _searchQuery.value = query   // triggers the debounced collector in init{}
    }

    /**
     * Called when the user picks a country from the dropdown.
     * Clears the search query and re-fetches headlines for the new country.
     */
    fun onCountrySelected(country: NewsCountry) {
        _uiState.value  = _uiState.value.copy(
            selectedCountry = country,
            searchQuery     = "",    // reset search when country changes
            articles        = emptyList()
        )
        _searchQuery.value = ""      // reset the debounced flow too
        fetchTopHeadlines()
    }

    /**
     * Called by the refresh button (pull-to-refresh or toolbar button).
     * Re-runs whichever mode is active: search or top-headlines.
     */
    fun refresh() {
        val currentQuery = _uiState.value.searchQuery
        if (currentQuery.isBlank()) {
            fetchTopHeadlines()
        } else {
            performSearch(currentQuery)
        }
    }

    /**
     * Return the article at [index] from the current list.
     * Used by the Detail screen — we pass the index via navigation args
     * and look up the full Article object here.
     *
     * Returns null if the index is out of bounds (safety guard).
     */
    fun getArticleByIndex(index: Int): Article? {
        return _uiState.value.articles.getOrNull(index)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Convert a raw exception into a short, user-friendly message.
     * Users should never see a raw Java exception class name.
     */
    private fun friendlyError(e: Throwable?): String {
        return when {
            e == null                                       -> "Unknown error"
            e.message?.contains("Unable to resolve host")
                    == true                                 -> "No internet connection"
            e.message?.contains("401") == true             -> "Invalid API key"
            e.message?.contains("429") == true             -> "Too many requests — try later"
            else                                            -> "Failed to load news: ${e.message}"
        }
    }
}
