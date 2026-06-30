package com.example.postalon.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.postalon.ui.viewmodel.ExploreViewModel
import com.example.postalon.ui.widgets.PostItem
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(
    viewModel: ExploreViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.posts.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var lastCount by remember { mutableStateOf(0) }


    LaunchedEffect(posts.size) {
        if (posts.isNotEmpty() && posts.size > lastCount) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
        lastCount = posts.size
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Explore") }) }
    ) { padding ->
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("There is no post yet!")
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding
            ) {
                items(posts, key = { it.id }) { post ->
                    PostItem(
                        post = post,
                        onLike = { sel -> viewModel.like(post.id, sel) },
                        onDislike = { sel -> viewModel.like(post.id, sel) }
                    )
                }
            }
        }
    }
}
