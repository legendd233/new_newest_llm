package com.example.new_newest_llm.ui.common

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FavoriteSyncViewModel : ViewModel() {

    private val _changes = MutableLiveData<Map<Int, Boolean>>(emptyMap())
    val changes: LiveData<Map<Int, Boolean>> = _changes

    fun publish(itemId: Int, isFavorited: Boolean) {
        val current = _changes.value ?: emptyMap()
        if (current[itemId] == isFavorited) return
        _changes.value = current + (itemId to isFavorited)
    }
}
