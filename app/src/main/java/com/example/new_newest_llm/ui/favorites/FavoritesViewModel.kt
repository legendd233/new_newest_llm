package com.example.new_newest_llm.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.model.FeedItem
import com.example.new_newest_llm.data.repository.FavoriteRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: LiveData<FavoritesUiState> = _uiState

    private val _navigateToDetail = MutableLiveData<FeedItem?>(null)
    val navigateToDetail: LiveData<FeedItem?> = _navigateToDetail

    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private val _toastError = MutableLiveData<String?>(null)
    val toastError: LiveData<String?> = _toastError

    private val _favoriteChanged = MutableLiveData<Pair<Int, Boolean>?>(null)
    val favoriteChanged: LiveData<Pair<Int, Boolean>?> = _favoriteChanged

    private var currentItems: List<FeedItem> = emptyList()

    fun loadFavorites() {
        _uiState.value = FavoritesUiState.Loading
        viewModelScope.launch {
            when (val result = favoriteRepository.getFavorites()) {
                is Result.Success -> {
                    val items = result.data.items
                    currentItems = items
                    _uiState.value = if (items.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(items)
                    }
                }
                is Result.Error -> {
                    if (isAuthError(result.code)) {
                        _navigateToLogin.value = true
                    } else {
                        _uiState.value = FavoritesUiState.Error(result.code)
                    }
                }
            }
        }
    }

    fun onItemClicked(item: FeedItem) {
        _navigateToDetail.value = item
    }

    fun onNavigationToDetailHandled() {
        _navigateToDetail.value = null
    }

    fun onNavigationToLoginHandled() {
        _navigateToLogin.value = false
    }

    fun onToastShown() {
        _toastError.value = null
    }

    fun onFavoriteChangePublished() {
        _favoriteChanged.value = null
    }

    fun unfavorite(item: FeedItem) {
        if (!item.isFavorited) return
        val previous = currentItems
        val updated = currentItems.filterNot { it.id == item.id }
        currentItems = updated
        _uiState.value = if (updated.isEmpty()) {
            FavoritesUiState.Empty
        } else {
            FavoritesUiState.Success(updated)
        }

        viewModelScope.launch {
            when (val result = favoriteRepository.removeFavorite(item.id)) {
                is Result.Success -> {
                    _favoriteChanged.value = item.id to false
                }
                is Result.Error -> {
                    if (isAuthError(result.code)) {
                        _navigateToLogin.value = true
                    } else {
                        currentItems = previous
                        _uiState.value = FavoritesUiState.Success(previous)
                        _toastError.value = "err_favorite_failed"
                    }
                }
            }
        }
    }

    fun applyExternalChange(itemId: Int, isFavorited: Boolean) {
        if (isFavorited) {
            return
        }
        val updated = currentItems.filterNot { it.id == itemId }
        if (updated.size == currentItems.size) return
        currentItems = updated
        _uiState.value = if (updated.isEmpty()) {
            FavoritesUiState.Empty
        } else {
            FavoritesUiState.Success(updated)
        }
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
