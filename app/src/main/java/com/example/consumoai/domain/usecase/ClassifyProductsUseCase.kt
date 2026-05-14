package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ProductClassifier
import com.example.consumoai.domain.model.ProductItem

class ClassifyProductsUseCase(
    private val productClassifier: ProductClassifier
) {
    operator fun invoke(items: List<ProductItem>): List<ProductItem> {
        return productClassifier.classifyAll(items)
    }
}

