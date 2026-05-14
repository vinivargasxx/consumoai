package com.example.consumoai.domain.model

data class ImportReceiptsResult(
    val importedCount: Int,
    val skippedCount: Int,
    val failedCount: Int
)

