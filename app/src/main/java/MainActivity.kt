package com.example.postalon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postalon.di.AppContainer
import com.example.postalon.di.AppViewModelFactory
import com.example.postalon.ui.pages.HomePage
import com.example.postalon.ui.viewmodel.AuthState
import com.example.postalon.ui.viewmodel.AuthViewModel
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        appContainer = AppContainer()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    PostalonApp(appContainer = appContainer)
                }
            }
        }
    }
}


@Composable
fun PostalonApp(appContainer: AppContainer) {
    val authViewModel: AuthViewModel = viewModel(factory = AppViewModelFactory(appContainer))
    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> LoadingScreen()
        is AuthState.Ready -> HomePage(appContainer = appContainer)
        is AuthState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = state.message)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
