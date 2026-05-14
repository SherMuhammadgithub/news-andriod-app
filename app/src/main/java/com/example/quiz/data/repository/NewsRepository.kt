package com.example.quiz.data.repository

import com.example.quiz.data.api.RetrofitInstance
import com.example.quiz.data.model.Article

class NewsRepository {

    private val api = RetrofitInstance.api

    suspend fun getTopHeadlines(country: String = "", lang: String = "en"): Result<List<Article>> {
        return try {
            val response = api.getTopHeadlines(country = country, lang = lang)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchNews(query: String, country: String = "", lang: String = "en"): Result<List<Article>> {
        return try {
            val response = api.searchNews(query = query, country = country, lang = lang)
            Result.success(response.articles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
