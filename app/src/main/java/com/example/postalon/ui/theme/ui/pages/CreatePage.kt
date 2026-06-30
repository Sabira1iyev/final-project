package com.example.postalon.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.postalon.di.AppContainer
import com.example.postalon.di.AppViewModelFactory
import com.example.postalon.ui.viewmodel.CreateUiState
import com.example.postalon.ui.viewmodel.CreateViewModel
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePage(appContainer: AppContainer, modifier: Modifier = Modifier) {
    val viewModel: CreateViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val imageLink by viewModel.imageLink.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Create") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                if (imageLink != null) {
                    SubcomposeAsyncImage(
                        model = imageLink,
                        contentDescription = "Selected image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(350.dp),
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        },
                        error = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.BrokenImage,
                                    contentDescription = null,
                                    modifier = Modifier.height(60.dp)
                                )
                            }
                        }
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(350.dp)
                            .background(Color(0xFFFF9800)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState == CreateUiState.LoadingImage) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.height(70.dp)
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.height(30.dp))

            Button(
                onClick = { viewModel.getImage() },
                enabled = uiState != CreateUiState.LoadingImage && uiState != CreateUiState.Publishing,
                modifier = Modifier.padding(horizontal = 20.dp).
                fillMaxWidth()
            ) {
                Text(
                    text = "Get Image",
                    modifier = Modifier.padding(12.dp)
                )
            }

            Box(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.publish() },
                enabled = imageLink != null && uiState != CreateUiState.Publishing,
                modifier = Modifier.padding(horizontal = 20.dp).
                fillMaxWidth()
            ) {
                Text(
                    text = if (uiState == CreateUiState.Publishing) "Publishing..." else "Publish",
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (uiState == CreateUiState.ImageError) {
                Box(modifier = Modifier.height(12.dp))
                Text(
                    text = "Error loading image, please try again.",
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}
