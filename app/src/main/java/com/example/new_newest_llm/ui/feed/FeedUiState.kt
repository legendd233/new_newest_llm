package com.example.new_newest_llm.ui.feed

sealed class FeedUiState {
    data object Loading : FeedUiState()
    data class Success(val items: List<com.example.new_newest_llm.data.model.FeedItem>) : FeedUiState()
    data class Error(val errorCode: String) : FeedUiState()
    data object Empty : FeedUiState()
}
