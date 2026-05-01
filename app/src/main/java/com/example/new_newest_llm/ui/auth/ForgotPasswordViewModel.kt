package com.example.new_newest_llm.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val isLoading: Boolean = false,
    val questionLoaded: Boolean = false,
    val questionZh: String = "",
    val questionEn: String = "",
    val resetSuccess: Boolean = false,
    val errorMessage: String? = null
)

class ForgotPasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData(ForgotPasswordUiState())
    val uiState: LiveData<ForgotPasswordUiState> = _uiState

    fun loadSecurityQuestion() {
        _uiState.value = ForgotPasswordUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.getSecurityQuestion()) {
                is Result.Success -> {
                    _uiState.value = ForgotPasswordUiState(
                        questionLoaded = true,
                        questionZh = result.data.question.zh,
                        questionEn = result.data.question.en
                    )
                }
                is Result.Error -> {
                    _uiState.value = ForgotPasswordUiState(errorMessage = "err_network")
                }
            }
        }
    }

    fun resetPassword(username: String, securityAnswer: String, newPassword: String): String? {
        val validationError = validate(username, securityAnswer, newPassword)
        if (validationError != null) return validationError

        val currentState = _uiState.value ?: ForgotPasswordUiState()
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.resetPassword(username, securityAnswer, newPassword)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(isLoading = false, resetSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = mapErrorCode(result.code)
                    )
                }
            }
        }
        return null
    }

    private fun validate(username: String, securityAnswer: String, newPassword: String): String? {
        if (username.isBlank()) return "err_username_empty"
        if (username.length > 64) return "err_username_too_long"
        if (securityAnswer.isBlank()) return "err_security_answer_empty"
        if (securityAnswer.length > 64) return "err_security_answer_too_long"
        if (newPassword.isEmpty()) return "err_password_empty"
        if (newPassword.length < 8) return "err_password_too_short"
        if (newPassword.length > 64) return "err_password_too_long"
        return null
    }

    companion object {
        fun mapErrorCode(code: String): String {
            return when (code) {
                "INVALID_CREDENTIALS" -> "err_invalid_credentials"
                "PASSWORD_REUSED" -> "err_password_reused"
                "NETWORK_ERROR" -> "err_network"
                else -> "err_network"
            }
        }
    }
}
