package com.example.consumoai.presentation.home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}
