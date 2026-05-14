package com.example.quiz.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz.data.model.Article
import com.example.quiz.data.model.NewsCountry
import com.example.quiz.data.model.NewsLanguage
import com.example.quiz.data.model.SUPPORTED_COUNTRIES
import com.example.quiz.data.model.SUPPORTED_LANGUAGES
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
 * NewsUiState — everything the Home screen needs to render itself.
 *
 * @param articles         Current list of articles shown in the feed.
 * @param isLoading        True while a network call is in progress → shows spinner.
 * @param errorMessage     Non-null when the last request failed → shows error UI.
 * @param searchQuery      Current text in the search bar.
 * @param selectedCountry  Country chosen in the country dropdown.
 * @param selectedLanguage Language chosen in the language dropdown.
 */
data class NewsUiState(
    val articles:         List<Article>  = emptyList(),
    val isLoading:        Boolean        = false,
    val errorMessage:     String?        = null,
    val searchQuery:      String         = "",
    val selectedCountry:  NewsCountry    = SUPPORTED_COUNTRIES[0],   // "All Countries"
    val selectedLanguage: NewsLanguage   = SUPPORTED_LANGUAGES[5]    // "English" (index 5)
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
class NewsViewModel : ViewModel() {

    private val repository = NewsRepository()

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    // Separate flow for search so we can debounce keystrokes
    private val _searchQuery = MutableStateFlow("")

    init {
        fetchTopHeadlines()

        viewModelScope.launch {
            _searchQuery
                .drop(1)                  // skip initial empty value
                .debounce(500L)           // wait 500ms after last keystroke
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isBlank()) fetchTopHeadlines() else performSearch(query)
                }
        }
    }

    // ── Private fetch helpers ─────────────────────────────────────────────────

    private fun fetchTopHeadlines() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.getTopHeadlines(
                country = _uiState.value.selectedCountry.code,
                lang    = _uiState.value.selectedLanguage.code
            )

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

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = repository.searchNews(
                query   = query,
                country = _uiState.value.selectedCountry.code,
                lang    = _uiState.value.selectedLanguage.code
            )

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

    // ── Public actions — called by UI ─────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.value     = _uiState.value.copy(searchQuery = query)
        _searchQuery.value = query
    }

    fun onCountrySelected(country: NewsCountry) {
        _uiState.value     = _uiState.value.copy(
            selectedCountry = country,
            searchQuery     = "",
            articles        = emptyList()
        )
        _searchQuery.value = ""
        fetchTopHeadlines()
    }

    /** Called when user picks a language from the language dropdown. */
    fun onLanguageSelected(language: NewsLanguage) {
        _uiState.value     = _uiState.value.copy(
            selectedLanguage = language,
            searchQuery      = "",
            articles         = emptyList()
        )
        _searchQuery.value = ""
        fetchTopHeadlines()
    }

    fun refresh() {
        val currentQuery = _uiState.value.searchQuery
        if (currentQuery.isBlank()) fetchTopHeadlines() else performSearch(currentQuery)
    }

    fun getArticleByIndex(index: Int): Article? = _uiState.value.articles.getOrNull(index)

    private fun friendlyError(e: Throwable?): String = when {
        e == null                                            -> "Unknown error"
        e.message?.contains("Unable to resolve host") == true -> "No internet connection"
        e.message?.contains("401") == true                   -> "Invalid API key"
        e.message?.contains("429") == true                   -> "Too many requests — try later"
        else                                                 -> "Failed to load news: ${e.message}"
    }
}
