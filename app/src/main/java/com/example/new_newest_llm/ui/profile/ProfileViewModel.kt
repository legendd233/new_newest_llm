package com.example.new_newest_llm.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.AuthRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val errorMessage: String? = null
)

class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableLiveData(ProfileUiState())
    val uiState: LiveData<ProfileUiState> = _uiState

    fun loadUser() {
        _uiState.value = ProfileUiState(isLoading = true)
        viewModelScope.launch {
            when (val result = repository.validateToken()) {
                is Result.Success -> {
                    _uiState.value = ProfileUiState(username = result.data.username)
                }
                is Result.Error -> {
                    _uiState.value = ProfileUiState(errorMessage = "err_token_revoked")
                }
            }
        }
    }
}
