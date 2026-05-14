package com.example.quiz.data.model

import com.google.gson.annotations.SerializedName

// ── Top-level API response ────────────────────────────────────────────────────

/**
 * NewsResponse — maps directly to the JSON returned by gnews.io API.
 *
 * Example gnews.io response:
 * {
 *   "totalArticles": 87234,
 *   "articles": [ { ... }, { ... } ]
 * }
 *
 * @SerializedName tells Gson which JSON key maps to which Kotlin field.
 * Without it, field names must exactly match the JSON keys.
 */
data class NewsResponse(
    @SerializedName("totalArticles") val totalArticles: Int = 0,
    @SerializedName("articles")      val articles: List<Article> = emptyList()
)

// ── Article ───────────────────────────────────────────────────────────────────

/**
 * Article — one news item from gnews.io.
 *
 * gnews.io article JSON shape:
 * {
 *   "title":       "Headline text",
 *   "description": "Short summary",
 *   "content":     "First 200–300 chars of article body...",
 *   "url":         "https://original-article-url.com",
 *   "image":       "https://image-url.jpg",
 *   "publishedAt": "2024-01-15T10:30:00Z",
 *   "source": { "name": "BBC News", "url": "https://bbc.com" }
 * }
 */
data class Article(
    @SerializedName("title")       val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("content")     val content: String = "",
    @SerializedName("url")         val url: String = "",
    @SerializedName("image")       val image: String? = null,   // nullable — some articles have no image
    @SerializedName("publishedAt") val publishedAt: String = "",
    @SerializedName("source")      val source: Source = Source()
)

// ── Source ────────────────────────────────────────────────────────────────────

/**
 * Source — the news outlet that published the article.
 *
 * Nested inside each Article object:
 * "source": { "name": "Reuters", "url": "https://reuters.com" }
 */
data class Source(
    @SerializedName("name") val name: String = "",
    @SerializedName("url")  val url: String = ""
)

// ── Country options ───────────────────────────────────────────────────────────

/**
 * NewsCountry — the list of countries supported by gnews.io's country filter.
 *
 * Each entry has:
 *   [displayName] — shown in the UI dropdown (e.g. "Pakistan")
 *   [code]        — sent to the API as the "country" query param (e.g. "pk")
 *
 * gnews.io country codes follow ISO 3166-1 alpha-2 (two-letter country codes).
 */
data class NewsCountry(val displayName: String, val code: String)

/**
 * SUPPORTED_COUNTRIES — the dropdown options for country filtering.
 * Used in NewsHomeScreen and NewsViewModel.
 */
val SUPPORTED_COUNTRIES = listOf(
    NewsCountry("All Countries", ""),     // empty code = no country filter
    NewsCountry("Pakistan",        "pk"),
    NewsCountry("United States",   "us"),
    NewsCountry("United Kingdom",  "gb"),
    NewsCountry("India",           "in"),
    NewsCountry("Saudi Arabia",    "sa"),
    NewsCountry("UAE",             "ae")
)
