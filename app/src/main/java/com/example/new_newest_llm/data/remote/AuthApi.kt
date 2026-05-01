package com.example.new_newest_llm.data.remote

import com.example.new_newest_llm.data.model.LoginRequest
import com.example.new_newest_llm.data.model.MeResponse
import com.example.new_newest_llm.data.model.OkResponse
import com.example.new_newest_llm.data.model.RegisterRequest
import com.example.new_newest_llm.data.model.ResetPasswordRequest
import com.example.new_newest_llm.data.model.SecurityQuestionResponse
import com.example.new_newest_llm.data.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("register")
    suspend fun register(@Body body: RegisterRequest): Response<TokenResponse>

    @POST("login")
    suspend fun login(@Body body: LoginRequest): Response<TokenResponse>

    @GET("security-question")
    suspend fun getSecurityQuestion(): Response<SecurityQuestionResponse>

    @POST("reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): Response<OkResponse>

    @GET("me")
    suspend fun me(): Response<MeResponse>
}
