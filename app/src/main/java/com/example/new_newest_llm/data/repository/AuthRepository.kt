package com.example.new_newest_llm.data.repository

import com.example.new_newest_llm.data.model.ErrorResponse
import com.example.new_newest_llm.data.model.LoginRequest
import com.example.new_newest_llm.data.model.MeResponse
import com.example.new_newest_llm.data.model.OkResponse
import com.example.new_newest_llm.data.model.RegisterRequest
import com.example.new_newest_llm.data.model.ResetPasswordRequest
import com.example.new_newest_llm.data.model.SecurityQuestionResponse
import com.example.new_newest_llm.data.model.TokenResponse
import com.example.new_newest_llm.data.remote.RetrofitClient
import com.example.new_newest_llm.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: String) : Result<Nothing>()
}

class AuthRepository(private val tokenManager: TokenManager) {

    private val api = RetrofitClient.authApi
    private val gson = Gson()

    suspend fun login(username: String, password: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    Result.Success(response.body()!!)
                } else {
                    val code = parseErrorCode(response.errorBody()?.string())
                    Result.Error(code)
                }
            } catch (e: Exception) {
                Result.Error("NETWORK_ERROR")
            }
        }
    }

    suspend fun register(username: String, password: String, securityAnswer: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.register(RegisterRequest(username, securityAnswer, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.token
                    tokenManager.saveToken(token)
                    Result.Success(response.body()!!)
                } else {
                    val code = parseErrorCode(response.errorBody()?.string())
                    Result.Error(code)
                }
            } catch (e: Exception) {
                Result.Error("NETWORK_ERROR")
            }
        }
    }

    suspend fun getSecurityQuestion(): Result<SecurityQuestionResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getSecurityQuestion()
                if (response.isSuccessful && response.body() != null) {
                    Result.Success(response.body()!!)
                } else {
                    val code = parseErrorCode(response.errorBody()?.string())
                    Result.Error(code)
                }
            } catch (e: Exception) {
                Result.Error("NETWORK_ERROR")
            }
        }
    }

    suspend fun resetPassword(username: String, securityAnswer: String, newPassword: String): Result<OkResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.resetPassword(ResetPasswordRequest(username, securityAnswer, newPassword))
                if (response.isSuccessful && response.body() != null) {
                    tokenManager.clearToken()
                    Result.Success(response.body()!!)
                } else {
                    val code = parseErrorCode(response.errorBody()?.string())
                    Result.Error(code)
                }
            } catch (e: Exception) {
                Result.Error("NETWORK_ERROR")
            }
        }
    }

    suspend fun validateToken(): Result<MeResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.me()
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
