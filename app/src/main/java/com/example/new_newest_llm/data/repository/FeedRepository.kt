package com.example.new_newest_llm.data.repository

import com.example.new_newest_llm.data.model.ErrorResponse
import com.example.new_newest_llm.data.model.FeedResponse
import com.example.new_newest_llm.data.remote.RetrofitClient
import com.example.new_newest_llm.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitClient.feedApi
    private val gson = Gson()

    suspend fun getFeed(since: Int? = null): Result<FeedResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getFeed(since)
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    val code = parseErrorCode(response.errorBody()?.string())
                    if (response.code() == 401) {
                        tokenManager.clearToken()
                    }
                    Result.Error(code)
                }
            } catch (e: Exception) {
                Result.Error("NETWORK_ERROR")
            }
        }
    }

    private fun parseErrorCode(errorBody: String?): String {
        if (errorBody.isNullOrEmpty()) return "UNKNOWN_ERROR"
        return try {
            val error = gson.fromJson(errorBody, ErrorResponse::class.java)
            error.detail
        } catch (e: Exception) {
            "UNKNOWN_ERROR"
        }
    }
}
