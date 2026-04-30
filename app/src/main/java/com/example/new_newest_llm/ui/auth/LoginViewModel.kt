package com.example.new_newest_llm.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(username: String, password: String): String? {
        val validationError = validate(username, password)
        if (validationError != null) return validationError

        _uiState.value = LoginUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.login(username, password)) {
                is Result.Success -> {
                    _uiState.value = LoginUiState(loginSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = LoginUiState(errorMessage = mapErrorCode(result.code))
                }
            }
        }
        return null
    }

    private fun validate(username: String, password: String): String? {
        if (username.isBlank()) return "err_username_empty"
        if (username.length > 64) return "err_username_too_long"
        if (password.isEmpty()) return "err_password_empty"
        if (password.length < 8) return "err_password_too_short"
        if (password.length > 64) return "err_password_too_long"
        return null
    }

    companion object {
        fun mapErrorCode(code: String): String {
            return when (code) {
                "INVALID_CREDENTIALS" -> "err_invalid_credentials"
                "NETWORK_ERROR" -> "err_network"
                else -> "err_network"
            }
        }
    }
}
