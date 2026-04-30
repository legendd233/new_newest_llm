package com.example.new_newest_llm.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.new_newest_llm.data.repository.FavoriteRepository
import com.example.new_newest_llm.data.repository.Result
import kotlinx.coroutines.launch

class DetailViewModel(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private var itemId: Int = -1

    private val _isFavorited = MutableLiveData(false)
    val isFavorited: LiveData<Boolean> = _isFavorited

    private val _favoriteChanged = MutableLiveData<Pair<Int, Boolean>?>(null)
    val favoriteChanged: LiveData<Pair<Int, Boolean>?> = _favoriteChanged

    private val _toastError = MutableLiveData<String?>(null)
    val toastError: LiveData<String?> = _toastError

    private val _navigateToLogin = MutableLiveData(false)
    val navigateToLogin: LiveData<Boolean> = _navigateToLogin

    private var initialized = false

    fun init(itemId: Int, isFavorited: Boolean) {
        if (initialized) return
        initialized = true
        this.itemId = itemId
        _isFavorited.value = isFavorited
    }

    fun toggleFavorite() {
        if (itemId < 0) return
        val newState = !(_isFavorited.value ?: false)
        _isFavorited.value = newState

        viewModelScope.launch {
            val result = if (newState) {
                favoriteRepository.addFavorite(itemId)
            } else {
                favoriteRepository.removeFavorite(itemId)
            }
            when (result) {
                is Result.Success -> {
                    _favoriteChanged.value = itemId to newState
                }
                is Result.Error -> {
                    if (isAuthError(result.code)) {
                        _navigateToLogin.value = true
                    } else {
                        _isFavorited.value = !newState
                        _toastError.value = "err_favorite_failed"
                    }
                }
            }
        }
    }

    fun onFavoriteChangePublished() {
        _favoriteChanged.value = null
    }

    fun onToastShown() {
        _toastError.value = null
    }

    fun onNavigationToLoginHandled() {
        _navigateToLogin.value = false
    }

    private fun isAuthError(code: String): Boolean {
        return code == "INVALID_TOKEN" ||
                code == "MISSING_TOKEN" ||
                code == "TOKEN_REVOKED" ||
                code == "USER_NOT_FOUND"
    }
}
