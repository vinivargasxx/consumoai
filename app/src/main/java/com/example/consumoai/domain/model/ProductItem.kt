package com.example.consumoai.domain.model

data class ProductItem(
    val id: Long = 0L,
    val receiptId: Long = 0L,
    val itemNumber: Int? = null,
    val name: String,
    val price: Double,
    val category: ProductCategory = ProductCategory.OTHER
)