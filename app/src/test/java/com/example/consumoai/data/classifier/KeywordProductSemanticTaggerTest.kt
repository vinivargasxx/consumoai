package com.example.consumoai.data.classifier

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductSemanticTag
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordProductSemanticTaggerTest {

    private val tagger = KeywordProductSemanticTagger()

    @Test
    fun tags_expectedKeywords() {
        assertHas("CHOPP IPA", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        assertHas("COCA COLA", ProductSemanticTag.SOFT_DRINK)
        assertHas("MONSTER", ProductSemanticTag.ENERGY_DRINK)
        assertHas("DORITOS", ProductSemanticTag.SNACK_OR_SWEET)
        assertHas("PIZZA SADIA", ProductSemanticTag.FROZEN_OR_READY_MEAL)
        assertHas("REQUEIJAO", ProductSemanticTag.DAIRY)
        assertHas("PATINHO", ProductSemanticTag.MEAT_OR_PROTEIN)
        assertHas("MELANCIA", ProductSemanticTag.FRESH_PRODUCE)
        assertHas("OMO", ProductSemanticTag.HOUSEHOLD_CLEANING)
        assertHas("SHAMPOO", ProductSemanticTag.PERSONAL_CARE)
    }

    // === NON_ALCOHOLIC_BEVERAGE propagation ===

    @Test
    fun coca_cola_gets_nonAlcoholicBeverage_and_softDrink() {
        assertHas("COCA COLA", ProductSemanticTag.SOFT_DRINK)
        assertHas("COCA COLA", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        assertNotHas("COCA COLA", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun red_bull_gets_nonAlcoholicBeverage_and_energyDrink() {
        assertHas("RED BULL", ProductSemanticTag.ENERGY_DRINK)
        assertHas("RED BULL", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        assertNotHas("RED BULL", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun suco_gets_nonAlcoholicBeverage_and_juice() {
        assertHas("SUCO UVA", ProductSemanticTag.JUICE)
        assertHas("SUCO UVA", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        assertNotHas("SUCO UVA", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun leite_gets_dairy_not_nonAlcoholicBeverage() {
        assertHas("LEITE INTEGRAL", ProductSemanticTag.DAIRY)
        assertNotHas("LEITE INTEGRAL", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun iogurte_gets_dairy_not_nonAlcoholicBeverage() {
        assertHas("IOGURTE NATURAL", ProductSemanticTag.DAIRY)
        assertNotHas("IOGURTE NATURAL", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun cerveja_gets_alcoholicBeverage_not_nonAlcoholicBeverage() {
        assertHas("CERVEJA LAGER", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        assertNotHas("CERVEJA LAGER", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun vinho_gets_alcoholicBeverage_not_nonAlcoholicBeverage() {
        assertHas("VINHO TINTO", ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        assertNotHas("VINHO TINTO", ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
    }

    @Test
    fun beverages_category_fallback_gives_nonAlcoholicBeverage() {
        // Item sem keyword conhecida, categoria BEVERAGES → fallback = NON_ALCOHOLIC_BEVERAGE
        val item = ProductItem(name = "AGUA MINERAL", price = 3.0, category = ProductCategory.BEVERAGES)
        val tags = tagger.tagsFor(item)
        assertTrue(
            "Fallback de BEVERAGES deve gerar NON_ALCOHOLIC_BEVERAGE, mas gerou $tags",
            tags.contains(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        )
        assertFalse(
            "Fallback de BEVERAGES não deve gerar ALCOHOLIC_BEVERAGE, mas gerou $tags",
            tags.contains(ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        )
    }

    private fun assertHas(name: String, tag: ProductSemanticTag) {
        val tags = tagger.tagsFor(ProductItem(name = name, price = 1.0, category = ProductCategory.OTHER))
        assertTrue("Expected $tag for '$name' but got $tags", tags.contains(tag))
    }

    private fun assertNotHas(name: String, tag: ProductSemanticTag) {
        val tags = tagger.tagsFor(ProductItem(name = name, price = 1.0, category = ProductCategory.OTHER))
        assertFalse("Did NOT expect $tag for '$name' but got $tags", tags.contains(tag))
    }
}

