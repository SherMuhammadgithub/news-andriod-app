package com.example.quiz.data.repository

import com.example.quiz.data.api.RetrofitInstance
import com.example.quiz.data.model.Article

/**
 * NewsRepository — the single source of truth for news data.
 *
 * MVVM layer:  ViewModel → NewsRepository → RetrofitInstance (API)
 *
 * Why a repository?
 *   Keeps Retrofit-specific code out of the ViewModel.
 *   If you switch to a different API later, only this file changes.
 *   Makes the ViewModel testable without a real network connection.
 *
 * All functions are [suspend] — they run inside a coroutine and
 * automatically execute on a background thread (via viewModelScope).
 */
class NewsRepository {

    // The Retrofit API service — lazily created singleton
    private val api = RetrofitInstance.api

    // ── Fetch Top Headlines ───────────────────────────────────────────────────

    /**
     * Fetch the latest top headlines, optionally filtered by country.
     *
     * @param country ISO 3166-1 alpha-2 code (e.g. "us", "pk").
     *                Pass empty string "" to get worldwide headlines.
     * @return [Result.success] with a list of articles on success,
     *         [Result.failure] with the exception on network/API error.
     *
     * [Result] is Kotlin's built-in wrapper for success/failure.
     * The ViewModel uses it to update UI state without throwing exceptions.
     */
    suspend fun getTopHeadlines(country: String = ""): Result<List<Article>> {
        return try {
            val response = api.getTopHeadlines(country = country)
            // .articles is the list inside NewsResponse; return it on success
            Result.success(response.articles)
        } catch (e: Exception) {
            // Catches: no internet, timeout, 4xx/5xx HTTP errors, JSON parse errors
            Result.failure(e)
        }
    }

    // ── Search News ───────────────────────────────────────────────────────────

    /**
     * Search articles by keyword, optionally scoped to a country.
     *
     * Uses the gnews.io /search endpoint instead of /top-headlines.
     * The API does full-text search across titles and descriptions.
     *
     * @param query   The search term (e.g. "cricket", "economy").
     * @param country ISO country code to scope results. "" = worldwide.
     * @return [Result.success] with matching articles, or [Result.failure] on error.
     */
    suspend fun searchNews(query: String, country: String = ""): Result<List<Article>> {
        return try {
            val response = api.searchNews(query = query, country = country)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
