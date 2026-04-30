package com.example.new_newest_llm.data.model

import com.google.gson.annotations.SerializedName

// Request bodies

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    @SerializedName("security_answer")
    val securityAnswer: String,
    val password: String
)

data class ResetPasswordRequest(
    val username: String,
    @SerializedName("security_answer")
    val securityAnswer: String,
    @SerializedName("new_password")
    val newPassword: String
)

// Response bodies

data class TokenResponse(
    val token: String
)

data class SecurityQuestionResponse(
    val question: QuestionLocalized
)

data class QuestionLocalized(
    val zh: String,
    val en: String
)

data class MeResponse(
    val id: Int,
    val username: String
)

data class OkResponse(
    val ok: Boolean
)

// Error response
data class ErrorResponse(
    val detail: String
)
