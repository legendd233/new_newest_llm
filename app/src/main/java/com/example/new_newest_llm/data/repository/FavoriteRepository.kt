package com.example.new_newest_llm.data.repository

import com.example.new_newest_llm.data.model.ErrorResponse
import com.example.new_newest_llm.data.model.FeedResponse
import com.example.new_newest_llm.data.model.OkResponse
import com.example.new_newest_llm.data.remote.RetrofitClient
import com.example.new_newest_llm.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitClient.favoriteApi
    private val gson = Gson()

    suspend fun addFavorite(itemId: Int): Result<OkResponse> = withContext(Dispatchers.IO) {
        runCall { api.addFavorite(itemId) }
    }

    suspend fun removeFavorite(itemId: Int): Result<OkResponse> = withContext(Dispatchers.IO) {
        runCall { api.removeFavorite(itemId) }
    }

    suspend fun getFavorites(): Result<FeedResponse> = withContext(Dispatchers.IO) {
        runCall { api.getFavorites() }
    }

    private suspend fun <T> runCall(block: suspend () -> retrofit2.Response<T>): Result<T> {
        return try {
            val response = block()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                if (response.code() == 401) {
                    tokenManager.clearToken()
                }
                Result.Error(parseErrorCode(response.errorBody()?.string()))
            }
        } catch (e: Exception) {
            Result.Error("NETWORK_ERROR")
        }
    }

    private fun parseErrorCode(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "UNKNOWN_ERROR"
        return try {
            gson.fromJson(errorBody, ErrorResponse::class.java).detail
        } catch (e: Exception) {
            "UNKNOWN_ERROR"
        }
    }
}
