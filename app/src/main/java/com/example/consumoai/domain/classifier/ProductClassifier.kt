package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ProductItem

interface ProductClassifier {
    fun classify(item: ProductItem): ProductItem
    fun classifyAll(items: List<ProductItem>): List<ProductItem>
}

