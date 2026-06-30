package com.example.postalon.ui.pages

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postalon.di.AppContainer
import com.example.postalon.di.AppViewModelFactory
import com.example.postalon.ui.viewmodel.ExploreViewModel


@Composable
fun HomePage(appContainer: AppContainer) {
    var selectedIndex by remember { mutableStateOf(0) }
    
    val factory = AppViewModelFactory(appContainer)
    val exploreViewModel: ExploreViewModel = viewModel(factory = factory)

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedIndex == 0,
                    onClick = { selectedIndex = 0 },
                    icon = { Icon(Icons.Filled.Public, contentDescription = "Explore") },
                    label = null
                )
                NavigationBarItem(
                    selected = selectedIndex == 1,
                    onClick = { selectedIndex = 1 },
                    icon = { Icon(Icons.Filled.AddAPhoto, contentDescription = "Create") },
                    label = null
                )
            }
        }
    ) { padding ->
        when (selectedIndex) {
            0 -> ExplorePage(
                viewModel = exploreViewModel,
                modifier = Modifier.padding(padding)
            )
            else -> CreatePage(
                appContainer = appContainer,
                modifier = Modifier.padding(padding)
            )
        }
    }
}
