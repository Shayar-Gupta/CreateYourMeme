package com.example.createyourmeme.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.createyourmeme.data.MemeRepository
import com.example.createyourmeme.network.MemeItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemeListViewModel(private val repo: MemeRepository = MemeRepository()) : ViewModel() {
    private val _memes = MutableStateFlow<List<MemeItem>>(emptyList())
    val memes: StateFlow<List<MemeItem>> = _memes.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadMemes()
    }

    fun loadMemes() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _memes.value = repo.fetchMemes()
            } catch (e: Exception) {
                e.printStackTrace()
                _memes.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}
