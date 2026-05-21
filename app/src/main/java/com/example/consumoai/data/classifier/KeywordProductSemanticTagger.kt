package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ProductSemanticTagger
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductSemanticTag
import java.text.Normalizer
import java.util.Locale

class KeywordProductSemanticTagger : ProductSemanticTagger {

    override fun tagsFor(item: ProductItem): Set<ProductSemanticTag> {
        val normalized = normalize(item.name)
        val tags = mutableSetOf<ProductSemanticTag>()

        // Bebidas alcoólicas: cerveja, chopp, vinho e derivados.
        if (matchesAny(normalized, listOf(
                "CERVEJA", "CERV ", "CERV.", "CHOPP", "IPA", "LAGER", "PILSEN", "PILSNER",
                "PALE ALE", "AMBER ALE", "STOUT", "VINHO", "ESPUMANTE", "ROSE",
                "WHISKY", "WHISKEY", "VODKA", "GIN ", "CACHACA", "CAIPIRINHA",
                "SKOL", "BRAHMA", "ANTARCTICA", "HEINEKEN", "STELLA", "CORONA",
                "BUDWEISER", "GUINNESS", "SPATEN", "BECK",
                "BADEN BADEN", "BLUE MOON", "KAISERDOM", "TUPINIQUIM",
                "AURORA", "CONCHA Y TORO"
            ))
        ) {
            tags += ProductSemanticTag.ALCOHOLIC_BEVERAGE
        }
        // Refrigerantes: colas, guaraná e afins. Excluir leite e derivados.
        if (matchesAny(normalized, listOf("COCA", "GUARANA", "REFRIGERANTE", "REFRI", "FANTA", "SPRITE", "PEPSI", "SUKITA", "DOLLY"))) {
            tags += ProductSemanticTag.SOFT_DRINK
        }
        // Energéticos.
        if (matchesAny(normalized, listOf("MONSTER", "ENERGETICO", "ENERG", "RED BULL", "REDBULL", "FLASH POWER", "TNT ENERGY"))) {
            tags += ProductSemanticTag.ENERGY_DRINK
        }
        // Sucos e néctares. Leite de caixinha começa com "LEITE" e NÃO deve bater aqui.
        if (matchesAny(normalized, listOf("SUCO", "NECTAR", "DEL VALLE", "NATURALE", "SUFRESH", "JOTA BE"))) {
            tags += ProductSemanticTag.JUICE
        }
        // Snacks e doces.
        if (matchesAny(normalized, listOf("DORITOS", "FANDANGOS", "TRENTO", "BALA", "SALG", "CHOC", "HALLS", "PAÇOCA", "PACOCA", "BISCOITO", "BOLACHA"))) {
            tags += ProductSemanticTag.SNACK_OR_SWEET
        }
        // Congelados e refeições prontas.
        if (matchesAny(normalized, listOf("PIZZA", "LASANHA", "NISSIN", "MIOJO", "LAMEN", "MACARRAO INST", "HOT POCKET"))) {
            tags += ProductSemanticTag.FROZEN_OR_READY_MEAL
        }
        // Laticínios: leite, iogurte, queijo, requeijão.
        // Prioridade alta para evitar que "LEITE" seja mapeado como bebida.
        if (matchesAny(normalized, listOf("LEITE", "QUEIJO", "QJO", "REQUEIJAO", "IOGURTE", "IOG", "MANTEIGA", "RICOTA", "CREME DE LEITE"))) {
            tags += ProductSemanticTag.DAIRY
        }
        // Carnes e proteínas.
        if (matchesAny(normalized, listOf("PATINHO", "COXAO", "FRANGO", "CARNE", "PRESUNTO", "MORTADELA", "LOMBO", "FILE", "PEIXE", "ATUM", "SARDINHA", "OVO"))) {
            tags += ProductSemanticTag.MEAT_OR_PROTEIN
        }
        // Hortifruti.
        if (matchesAny(normalized, listOf("MELANCIA", "PIMENTAO", "ALHO", "CEBOLINHA", "TOMATE", "BANANA", "MACA", "MORANGO", "ALFACE", "CENOURA", "BROCOLIS", "LARANJA", "LIMAO"))) {
            tags += ProductSemanticTag.FRESH_PRODUCE
        }
        // Higiene pessoal.
        if (matchesAny(normalized, listOf("SHAMPOO", "DOVE", "REXONA", "COLGATE", "SABONETE", "DESODORANTE", "CONDICIONADOR", "CREME DENTAL", "FRALDA"))) {
            tags += ProductSemanticTag.PERSONAL_CARE
        }
        // Limpeza doméstica.
        if (matchesAny(normalized, listOf("OMO", "AJAX", "DET LQ", "DESINF", "DETERGENTE", "AMACIANTE", "PINHO SOL", "FLASH LIMP", "VEJA", "CLOROX", "DOMESTOS"))) {
            tags += ProductSemanticTag.HOUSEHOLD_CLEANING
        }
        // Petshop.
        if (matchesAny(normalized, listOf("PEDIGREE", "PETHAND", "RACAO", "ALIM CAO", "WHISKAS", "PREMIER PET"))) {
            tags += ProductSemanticTag.PET
        }
        // Utilidades diversas.
        if (matchesAny(normalized, listOf("CANECA", "FITA", "CADERNO", "PULVERIZ", "CAD "))) {
            tags += ProductSemanticTag.UTILITY
        }

        // NON_ALCOHOLIC_BEVERAGE: inferred from SOFT_DRINK, ENERGY_DRINK or JUICE,
        // but only when DAIRY is NOT present (leite/iogurte/requeijão nunca são bebidas não alcoólicas).
        if (!tags.contains(ProductSemanticTag.DAIRY) &&
            (tags.contains(ProductSemanticTag.SOFT_DRINK) ||
                tags.contains(ProductSemanticTag.ENERGY_DRINK) ||
                tags.contains(ProductSemanticTag.JUICE))
        ) {
            tags += ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE
        }

        if (tags.isEmpty()) {
            tags += fallbackTagFromCategory(item.category)
        }

        return tags
    }

    private fun fallbackTagFromCategory(category: ProductCategory): ProductSemanticTag {
        return when (category) {
            ProductCategory.BEVERAGES -> ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE
            ProductCategory.INDUSTRIALIZED -> ProductSemanticTag.SNACK_OR_SWEET
            ProductCategory.BASIC_FOOD -> ProductSemanticTag.MEAT_OR_PROTEIN
            ProductCategory.PRODUCE -> ProductSemanticTag.FRESH_PRODUCE
            ProductCategory.HYGIENE -> ProductSemanticTag.PERSONAL_CARE
            ProductCategory.CLEANING -> ProductSemanticTag.HOUSEHOLD_CLEANING
            ProductCategory.OTHER -> ProductSemanticTag.UNKNOWN
        }
    }

    private fun normalize(value: String): String {
        val uppercase = value.uppercase(Locale.ROOT)
        val noAccents = Normalizer.normalize(uppercase, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
        return noAccents.replace(Regex("[^A-Z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchesAny(normalized: String, keywords: List<String>): Boolean {
        return keywords.any { normalized.contains(it) }
    }
}

