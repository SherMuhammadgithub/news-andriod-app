package com.example.quiz.data.api

import com.example.quiz.data.model.NewsResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ── API Constants ─────────────────────────────────────────────────────────────

private const val BASE_URL = "https://gnews.io/api/v4/"
const val NEWS_API_KEY    = "205c750d376b7f27146ea164a90ae52e"

// ── Retrofit Interface ────────────────────────────────────────────────────────

/**
 * NewsApiService — defines the HTTP endpoints for gnews.io.
 *
 * Retrofit reads this interface at compile-time and generates the actual
 * HTTP call implementation automatically. You never implement this interface
 * yourself — Retrofit does it.
 *
 * Each function maps to one API endpoint:
 *   @GET("path")      → GET https://gnews.io/api/v4/path
 *   @Query("param")   → appends ?param=value to the URL
 *   suspend fun       → can be called from a coroutine (runs on IO thread)
 */
interface NewsApiService {

    /**
     * Fetch top headlines.
     *
     * Full URL example:
     * GET https://gnews.io/api/v4/top-headlines
     *        ?category=general&lang=en&country=us&max=10&apikey=KEY
     *
     * @param country  ISO 3166-1 alpha-2 code (e.g. "us", "pk"). Empty = all countries.
     * @param lang     Language code (default "en").
     * @param max      Max articles to return (default 10, max 100 on free plan).
     * @param apiKey   Your gnews.io API key.
     */
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country")  country:  String = "",
        @Query("lang")     lang:     String = "en",
        @Query("max")      max:      Int    = 10,
        @Query("apikey")   apiKey:   String = NEWS_API_KEY
    ): NewsResponse

    /**
     * Search articles by keyword.
     *
     * Full URL example:
     * GET https://gnews.io/api/v4/search
     *        ?q=cricket&lang=en&country=pk&max=10&apikey=KEY
     *
     * @param query    The search keyword(s).
     * @param country  ISO country code to scope results. Empty = worldwide.
     * @param lang     Language code.
     * @param max      Max results.
     * @param apiKey   Your gnews.io API key.
     */
    @GET("search")
    suspend fun searchNews(
        @Query("q")        query:    String,
        @Query("country")  country:  String = "",
        @Query("lang")     lang:     String = "en",
        @Query("max")      max:      Int    = 10,
        @Query("apikey")   apiKey:   String = NEWS_API_KEY
    ): NewsResponse
}

// ── Retrofit Singleton ────────────────────────────────────────────────────────

/**
 * RetrofitInstance — a singleton that creates and holds the Retrofit client.
 *
 * Why singleton? Creating a Retrofit instance is expensive (parses annotations,
 * builds OkHttp client). We create it once and reuse it everywhere.
 *
 * [by lazy] means it is created only when first accessed, not at app startup.
 */
object RetrofitInstance {

    // OkHttpClient adds a logging interceptor so we can see full request/response
    // in Logcat (filter by tag "OkHttp"). Only logs in DEBUG builds.
    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // BODY level logs: URL, headers, and full JSON response body
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    /**
     * [api] is the ready-to-use service. Call it like:
     *   RetrofitInstance.api.getTopHeadlines(country = "pk")
     */
    val api: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            // GsonConverterFactory automatically converts JSON → Kotlin data classes
            // using the @SerializedName annotations in NewsResponse.kt
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(NewsApiService::class.java)
    }
}
