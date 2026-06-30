package com.example.postalon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postalon.data.FirebaseService
import com.example.postalon.data.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CreateUiState {
    object Idle : CreateUiState()
    object LoadingImage : CreateUiState()
    object Publishing : CreateUiState()
    object ImageError : CreateUiState()
}


class CreateViewModel(
    private val imageRepository: ImageRepository,
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _imageLink = MutableStateFlow<String?>(null)
    val imageLink: StateFlow<String?> = _imageLink.asStateFlow()

    private val _uiState = MutableStateFlow<CreateUiState>(CreateUiState.Idle)
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    fun getImage() {
        viewModelScope.launch {
            _uiState.value = CreateUiState.LoadingImage
            val link = imageRepository.getRandomImageLink()
            _imageLink.value = link
            _uiState.value = if (link != null) CreateUiState.Idle else CreateUiState.ImageError
        }
    }

    fun publish() {
        val link = _imageLink.value ?: return

        viewModelScope.launch {
            _uiState.value = CreateUiState.Publishing
            val success = firebaseService.createPost(link)
            _imageLink.value = if (success) null else link
            _uiState.value = CreateUiState.Idle
        }
    }
}
