package com.example.new_newest_llm.ui.favorites

import com.example.new_newest_llm.data.model.FeedItem

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data class Success(val items: List<FeedItem>) : FavoritesUiState()
    data class Error(val errorCode: String) : FavoritesUiState()
    data object Empty : FavoritesUiState()
}
