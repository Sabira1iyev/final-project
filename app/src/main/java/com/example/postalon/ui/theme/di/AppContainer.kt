package com.example.postalon.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.postalon.data.FirebaseService
import com.example.postalon.data.ImageRepository
import com.example.postalon.ui.viewmodel.AuthViewModel
import com.example.postalon.ui.viewmodel.CreateViewModel
import com.example.postalon.ui.viewmodel.ExploreViewModel


class AppContainer {
    val firebaseService = FirebaseService()
    val imageRepository = ImageRepository()
}

class AppViewModelFactory(private val appContainer: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java ->
                AuthViewModel(appContainer.firebaseService) as T

            ExploreViewModel::class.java ->
                ExploreViewModel(appContainer.firebaseService) as T

            CreateViewModel::class.java ->
                CreateViewModel(appContainer.imageRepository, appContainer.firebaseService) as T

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
