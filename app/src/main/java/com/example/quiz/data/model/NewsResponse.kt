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
 * NewsCountry — a country filter option for gnews.io.
 * [code] uses ISO 3166-1 alpha-2 (e.g. "pk", "us"). Empty = no filter.
 */
data class NewsCountry(val displayName: String, val code: String)

val SUPPORTED_COUNTRIES = listOf(
    NewsCountry("All Countries",   ""),
    NewsCountry("Pakistan",        "pk"),
    NewsCountry("United States",   "us"),
    NewsCountry("United Kingdom",  "gb"),
    NewsCountry("India",           "in"),
    NewsCountry("Saudi Arabia",    "sa"),
    NewsCountry("UAE",             "ae")
)

// ── Language options ──────────────────────────────────────────────────────────

/**
 * NewsLanguage — a language filter option for gnews.io.
 * [code] uses ISO 639-1 two-letter language codes (e.g. "en", "ur").
 * Empty code = no language filter (API default).
 */
data class NewsLanguage(val displayName: String, val code: String)

/**
 * SUPPORTED_LANGUAGES — full list of languages gnews.io supports,
 * plus Punjabi added per user request.
 */
val SUPPORTED_LANGUAGES = listOf(
    NewsLanguage("All Languages", ""),
    NewsLanguage("Arabic",        "ar"),
    NewsLanguage("Bengali",       "bn"),
    NewsLanguage("Chinese",       "zh"),
    NewsLanguage("Dutch",         "nl"),
    NewsLanguage("English",       "en"),
    NewsLanguage("French",        "fr"),
    NewsLanguage("German",        "de"),
    NewsLanguage("Greek",         "el"),
    NewsLanguage("Hebrew",        "he"),
    NewsLanguage("Hindi",         "hi"),
    NewsLanguage("Indonesian",    "id"),
    NewsLanguage("Italian",       "it"),
    NewsLanguage("Japanese",      "ja"),
    NewsLanguage("Malayalam",     "ml"),
    NewsLanguage("Marathi",       "mr"),
    NewsLanguage("Norwegian",     "no"),
    NewsLanguage("Portuguese",    "pt"),
    NewsLanguage("Punjabi",       "pa"),   // added per user request
    NewsLanguage("Romanian",      "ro"),
    NewsLanguage("Russian",       "ru"),
    NewsLanguage("Spanish",       "es"),
    NewsLanguage("Swedish",       "sv"),
    NewsLanguage("Tamil",         "ta"),
    NewsLanguage("Telugu",        "te"),
    NewsLanguage("Turkish",       "tr"),
    NewsLanguage("Ukrainian",     "uk"),
    NewsLanguage("Urdu",          "ur")
)
