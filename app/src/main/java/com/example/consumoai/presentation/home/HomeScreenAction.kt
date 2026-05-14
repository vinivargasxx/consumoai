package com.example.consumoai.presentation.home

sealed interface HomeScreenAction {
    data object OnImportSampleNfceUrlsClick : HomeScreenAction
    data object OnAnalyzeStoredReceiptsClick : HomeScreenAction
    data object OnClearReceiptsClick : HomeScreenAction
}
