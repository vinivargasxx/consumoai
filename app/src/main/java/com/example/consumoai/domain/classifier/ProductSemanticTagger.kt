package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductSemanticTag

interface ProductSemanticTagger {
    fun tagsFor(item: ProductItem): Set<ProductSemanticTag>
}

