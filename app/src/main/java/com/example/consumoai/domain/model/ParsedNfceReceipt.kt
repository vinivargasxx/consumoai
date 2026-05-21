package com.example.consumoai.domain.model

import java.time.LocalDate

data class ParsedNfceReceipt(
    val items: List<ProductItem>,
    val issueDate: LocalDate?
)

