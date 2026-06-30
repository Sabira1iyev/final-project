package com.example.postalon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postalon.data.FirebaseService
import com.example.postalon.data.model.PostModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val firebaseService: FirebaseService
) : ViewModel() {


    val posts: StateFlow<List<PostModel>> = firebaseService
        .getLivePostsFlow(limit = 20)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun like(postId: String, selected: Int?) {
        viewModelScope.launch {
            firebaseService.likeQuestions(postId, selected)
        }
    }
}
