package com.example.new_newest_llm.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String? = null
)

class ChangePasswordViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData(ChangePasswordUiState())
    val uiState: LiveData<ChangePasswordUiState> = _uiState

    fun changePassword(
        username: String,
        securityAnswer: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        val error = validate(username, securityAnswer, newPassword, confirmPassword)
        if (error != null) return error

        _uiState.value = ChangePasswordUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.resetPassword(username, securityAnswer, newPassword)) {
                is Result.Success -> {
                    _uiState.value = ChangePasswordUiState(success = true)
                }
                is Result.Error -> {
                    _uiState.value = ChangePasswordUiState(errorMessage = mapErrorCode(result.code))
                }
            }
        }
        return null
    }

    private fun validate(
        username: String,
        securityAnswer: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        if (username.isBlank()) return "err_username_empty"
        if (securityAnswer.isBlank()) return "err_security_answer_empty"
        if (securityAnswer.length > 64) return "err_security_answer_too_long"
        if (newPassword.isEmpty()) return "err_password_empty"
        if (newPassword.length < 8) return "err_password_too_short"
        if (newPassword.length > 64) return "err_password_too_long"
        if (newPassword != confirmPassword) return "err_confirm_password_mismatch"
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
