package com.example.new_newest_llm.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

data class RegisterUiState(
    val isLoading: Boolean = false,
    val registerSuccess: Boolean = false,
    val questionZh: String = "",
    val questionEn: String = "",
    val errorMessage: String? = null
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData(RegisterUiState())
    val uiState: LiveData<RegisterUiState> = _uiState

    fun loadSecurityQuestion() {
        viewModelScope.launch {
            when (val result = repository.getSecurityQuestion()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(
                        questionZh = result.data.question.zh,
                        questionEn = result.data.question.en
                    )
                }
                is Result.Error -> {
                    // Question加载失败不影响注册流程，用户可手动输入
                }
            }
        }
    }

    fun register(username: String, password: String, confirmPassword: String, securityAnswer: String): String? {
        val validationError = validate(username, password, confirmPassword, securityAnswer)
        if (validationError != null) return validationError

        _uiState.value = _uiState.value?.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.register(username, password, securityAnswer)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value?.copy(isLoading = false, registerSuccess = true)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value?.copy(
                        isLoading = false,
                        errorMessage = mapErrorCode(result.code)
                    )
                }
            }
        }
        return null
    }

    private fun validate(username: String, password: String, confirmPassword: String, securityAnswer: String): String? {
        if (username.isBlank()) return "err_username_empty"
        if (username.length > 64) return "err_username_too_long"
        if (password.isEmpty()) return "err_password_empty"
        if (password.length < 8) return "err_password_too_short"
        if (password.length > 64) return "err_password_too_long"
        if (password != confirmPassword) return "err_confirm_password_mismatch"
        if (securityAnswer.isBlank()) return "err_security_answer_empty"
        if (securityAnswer.length > 64) return "err_security_answer_too_long"
        return null
    }

    companion object {
        fun mapErrorCode(code: String): String {
            return when (code) {
                "USERNAME_TAKEN" -> "err_username_taken"
                "NETWORK_ERROR" -> "err_network"
                else -> "err_network"
            }
        }
    }
}
