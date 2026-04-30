package com.example.new_newest_llm.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.FeedRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: FeedRepository) : ViewModel() {

    private val _uiState = MutableLiveData<FeedUiState>(FeedUiState.Loading)
    val uiState: LiveData<FeedUiState> = _uiState

    private val _navigateToDetail = MutableLiveData<com.example.new_newest_llm.data.model.FeedItem?>(null)
    val navigateToDetail: LiveData<com.example.new_newest_llm.data.model.FeedItem?> = _navigateToDetail

    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    fun loadFeed() {
        _uiState.value = FeedUiState.Loading
        viewModelScope.launch {
            when (val result = repository.getFeed()) {
                is Result.Success -> {
                    val items = result.data.items
                    _uiState.value = if (items.isEmpty()) {
                        FeedUiState.Empty
                    } else {
                        FeedUiState.Success(items)
                    }
                }
                is Result.Error -> {
                    if (isAuthError(result.code)) {
                        _navigateToLogin.value = true
                    } else {
                        _uiState.value = FeedUiState.Error(result.code)
                    }
                }
            }
        }
    }

    fun onItemClicked(item: com.example.new_newest_llm.data.model.FeedItem) {
        _navigateToDetail.value = item
    }

    fun onNavigationToDetailHandled() {
        _navigateToDetail.value = null
    }

    fun onNavigationToLoginHandled() {
        _navigateToLogin.value = false
    }

    companion object {
        fun mapErrorCode(code: String): String {
            return when (code) {
                "NETWORK_ERROR" -> "err_network"
                else -> "err_network"
            }
        }

        private fun isAuthError(code: String): Boolean {
            return code == "INVALID_TOKEN" ||
                    code == "MISSING_TOKEN" ||
                    code == "TOKEN_REVOKED" ||
                    code == "USER_NOT_FOUND"
        }
    }
}
