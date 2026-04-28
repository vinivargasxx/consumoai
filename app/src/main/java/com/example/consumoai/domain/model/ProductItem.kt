package com.example.consumoai.domain.model

data class ProductItem(
    val name: String,
    val price: Double,
    val category: ProductCategory = ProductCategory.OTHER
)