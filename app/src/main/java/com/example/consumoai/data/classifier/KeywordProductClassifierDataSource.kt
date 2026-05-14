package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ProductClassifier
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import java.text.Normalizer
import java.util.Locale

class KeywordProductClassifierDataSource : ProductClassifier {

    // ========== EXPANDED KEYWORDS LISTS ==========

    private val beveragesKeywords = listOf(
        // Soft drinks
        "COCA", "COCA COLA", "REFRIGERANTE", "REFRI", "GUARANA", "FANTA", "SPRITE", "PEPSI",
        // Juices and nectars
        "SUCO", "NECTAR", "NATURALE", "DEL VALLE",
        // Water
        "AGUA", "AGUA MINERAL",
        // Tea and mate
        "CHA", "MATE",
        // Energy drinks
        "ENERGETICO", "MONSTER",
        // Alcoholic beverages
        "CERVEJA", "CHOPP", "IPA", "PALE ALE", "LAGER", "PILSEN", "PILSNER",
        "BADEN BADEN", "BLUE MOON", "KAISERDOM", "TUPINIQUIM", "ROLETA RUSSA",
        "VINHO", "CONCHA Y TORO", "AURORA"
    )

    private val industrializedKeywords = listOf(
        // Chocolates
        "CHOCOLATE", "CHOC", "TRENTO", "LACTA", "KITKAT", "KINDER", "OREO",
        // Biscuits and cookies
        "BISCOITO", "BISC", "BOLACHA", "COOKIE", "WAFER",
        // Sweet snacks
        "TORTIN", "MOUSSE", "ANA MARIA", "BAUDUCCO", "ISABELA",
        "AVELA", "AVELAS", "CACAU",
        // Savory snacks
        "SALGADINHO", "SALG", "SNACK",
        "DORITOS", "FANDANGOS", "CHEETOS", "SALTLETTS", "BREZEL",
        // Frozen/convenience
        "CONGELADO", "LASANHA", "PIZZA", "WRAP", "TORTILHA",
        "NUGGET", "BATATA PALHA",
        // Sauces and condiments
        "TEMPERO", "SAZON", "MAIONESE", "KETCHUP", "CATCHUP", "MOSTARDA",
        // Breakfast items
        "ACHOCOLATADO", "CEREAL", "GRANOLA", "PIPOCA", "PIPOCA MIC",
        // Noodles
        "NISSIN", "MAC NISSIN", "LAMEN", "MIOJO",
        // Sweets
        "BARRA", "DOCE", "BALA", "BALAS", "HALLS", "GOMA", "SORVETE", "PICOLE",
        // Bakery
        "BOLO", "BOLO MARMORE", "ROSCA", "ROSCA POLV"
    )

    private val basicFoodKeywords = listOf(
        // Grains and starches
        "ARROZ", "FEIJAO", "MASSA", "MACARRAO", "ESPAGUETE", "FARINHA",
        // Sweeteners and oils
        "ACUCAR", "OLEO", "SAL", "SAL REFINADO", "SAL IODADO",
        // Beverages - milk based
        "CAFE",
        // Dairy products
        "LEITE", "IOGURTE", "IOG", "BATIDO",
        "QUEIJO", "QJO", "MUSSARELA", "MUCARELA", "MOZZARELLA",
        "REQUEIJAO", "MANTEIGA", "MARGARINA",
        // Bread
        "PAO", "CACETINHO", "BISNAGA",
        // Meat and proteins
        "CARNE", "FRANGO", "PEITO", "PATINHO", "COXAO", "COXAO DENTRO",
        "CARNE MOIDA", "MOIDA", "SALSICHA", "LINGUICA",
        "OVO", "OVOS", "LOMBO", "LOMBO COZ", "PRESUNTO", "MORTADELA", "MORT",
        // Fish and seafood
        "ATUM", "SARDINHA",
        // Sauces and pastes
        "MOLHO", "MOLHO TOMATE", "PASSATA", "EXTRATO",
        "TOMATE S PELE", "TOMATE PELADO",
        // Other
        "MEL", "AVEIA", "MILHO VERDE", "AMENDOIM"
    )

    private val produceKeywords = listOf(
        // Fruits
        "BANANA", "MACA", "MAMAO", "LARANJA", "MORANGO", "UVA", "LIMAO", "ABACAXI",
        "MELANCIA", "MELAO", "PERA",
        // Vegetables
        "TOMATE", "BATATA", "CENOURA", "CEBOLA", "ALFACE", "PIMENTAO",
        "VERDURA", "LEGUME",
        // Leafy and fresh greens
        "BROCOLIS", "COUVE", "REPOLHO", "CEBOLINHA", "ALHO",
        // Other vegetables
        "PEPINO", "ABOBRINHA", "BERINJELA", "MANDIOCA", "AIPIM", "INHAME",
        // Granel indicator intentionally excluded from broad matching to avoid false positives
    )

    private val hygieneKeywords = listOf(
        // Toilet paper
        "PAPEL HIGIENICO", "HIGIENICO",
        // Soaps and cleansers
        "SABONETE", "DOVE",
        // Hair products
        "SHAMPOO", "HEAD", "SHOULDERS", "HEAD SHOULDERS", "CLEAR", "CONDICIONADOR",
        // Dental care
        "CREME DENTAL", "PASTA DENTAL", "COLGATE",
        "ESCOVA DENTAL", "FIO DENTAL",
        // Deodorant
        "DESODORANTE", "REXONA",
        // Feminine products
        "ABSORVENTE", "FRALDA", "HUGGIES",
        // Personal hygiene misc
        "ALGODAO", "COTONETE", "BARBEAR", "GILLETTE", "CARGA GILLETTE",
        "ENXAGUANTE",
        // Wipes and tissues
        "LENCO UMEDECIDO", "LENCO UMED", "TOALHA UMED", "TOALHA UMEDECIDA",
        // Sexual health
        "PRESERVATIVO", "OLLA"
    )

    private val cleaningKeywords = listOf(
        // Dish detergent
        "DETERGENTE", "LAV LOUCA", "LAVA LOUCA",
        // Disinfectants
        "DESINFETANTE", "PINHO SOL", "KALIPTO",
        // Bleach and sanitizers
        "AGUA SANITARIA", "SANITARIA", "CLORO",
        // Laundry products
        "LAVA ROUPAS", "OMO",
        // Soaps
        "SABAO", "SABAO PO",
        // Fabric softeners
        "AMACIANTE", "COMFORT", "SPLENDO",
        // Whiteners
        "ALVEJANTE",
        // General cleaners
        "LIMPADOR", "LIMP PISO", "AJAX", "DESTAC",
        "MULTIUSO", "VEJA", "YPE",
        // Cleaning tools
        "ESPONJA", "ESFREBOM", "BOMBRIL",
        // Misc
        "SAPONACEO", "DESENGORDURANTE", "LIMPEZA",
        // Trash and paper
        "SACO LIXO", "LIXO",
        "PAPEL TOALHA", "TOALHA PAPEL", "FILTRO PAPEL"
    )

    override fun classify(item: ProductItem): ProductItem {
        val normalized = normalizeName(item.name)


        val category = when {
            isAlcoholicBeverage(normalized) -> ProductCategory.BEVERAGES
            isEnergyDrink(normalized) -> ProductCategory.BEVERAGES
            isPersonalHygiene(normalized) -> ProductCategory.HYGIENE
            isHouseCleaning(normalized) -> ProductCategory.CLEANING
            isTomatoProcessed(normalized) -> ProductCategory.BASIC_FOOD
            isFreshProduce(normalized) -> ProductCategory.PRODUCE
            isMeat(normalized) -> ProductCategory.BASIC_FOOD
            isDairy(normalized) -> ProductCategory.BASIC_FOOD
            isFrozenOrConvenienceFood(normalized) -> ProductCategory.INDUSTRIALIZED
            isSnackOrSweet(normalized) -> ProductCategory.INDUSTRIALIZED

            matchesAny(normalized, hygieneKeywords) -> ProductCategory.HYGIENE
            matchesAny(normalized, cleaningKeywords) -> ProductCategory.CLEANING
            matchesAny(normalized, beveragesKeywords) -> ProductCategory.BEVERAGES
            matchesAny(normalized, industrializedKeywords) -> ProductCategory.INDUSTRIALIZED
            matchesAny(normalized, basicFoodKeywords) -> ProductCategory.BASIC_FOOD
            matchesAnyProduce(normalized) -> ProductCategory.PRODUCE
            else -> ProductCategory.OTHER
        }

        return item.copy(category = category)
    }

    override fun classifyAll(items: List<ProductItem>): List<ProductItem> {
        return items.map(::classify)
    }

    private fun normalizeName(name: String): String {
        val uppercase = name.uppercase(Locale.ROOT)
        val noAccents = Normalizer.normalize(uppercase, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")

        return noAccents
            .replace(Regex("[^A-Z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun matchesAny(normalized: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> matchesKeyword(normalized, keyword) }
    }

    private fun matchesKeyword(normalized: String, keyword: String): Boolean {
        return when {
            keyword.length <= 3 -> hasWholeWord(normalized, keyword)
            else -> normalized.contains(keyword)
        }
    }

    private fun hasWholeWord(normalized: String, token: String): Boolean {
        return Regex("(^| )${Regex.escape(token)}( |$)").containsMatchIn(normalized)
    }

    private fun isSafeShortTokenMatch(
        normalized: String,
        token: String,
        requiredContext: List<String>
    ): Boolean {
        if (!hasWholeWord(normalized, token)) return false
        return requiredContext.isEmpty() || requiredContext.any { normalized.contains(it) }
    }

    private fun matchesAnyProduce(normalized: String): Boolean {
        if (containsBeverageContext(normalized)) return false

        return produceKeywords.any { keyword ->
            when (keyword) {
                "MACA", "UVA", "TOMATE", "ALHO" -> hasWholeWord(normalized, keyword)
                else -> normalized.contains(keyword)
            }
        }
    }

    private fun containsBeverageContext(normalized: String): Boolean {
        return normalized.contains("SUCO") ||
            normalized.contains("NECTAR") ||
            normalized.contains("NATURALE") ||
            normalized.contains("DEL VALLE") ||
            normalized.contains("REFRIGERANTE") ||
            normalized.contains("REFRI") ||
            normalized.contains("COCA") ||
            normalized.contains("FANTA") ||
            normalized.contains("SPRITE") ||
            normalized.contains("PEPSI") ||
            normalized.contains("MONSTER") ||
            normalized.contains("ENERGETICO") ||
            normalized.contains("CHOPP") ||
            normalized.contains("VINHO") ||
            normalized.contains("CERVEJA")
    }

    private fun containsSnackOrFrozenContext(normalized: String): Boolean {
        return normalized.contains("PIZZA") ||
            normalized.contains("LASANHA") ||
            normalized.contains("NISSIN") ||
            normalized.contains("LAMEN") ||
            normalized.contains("MIOJO") ||
            normalized.contains("SALG") ||
            normalized.contains("SALGADINHO") ||
            normalized.contains("DORITOS") ||
            normalized.contains("FANDANGOS") ||
            normalized.contains("CHEETOS") ||
            normalized.contains("SNACK") ||
            normalized.contains("WRAP") ||
            normalized.contains("TORTILHA")
    }

    // ========== SPECIAL RULE FUNCTIONS ==========

    /**
     * Detects alcoholic beverages.
     */
    private fun isAlcoholicBeverage(normalized: String): Boolean {
        return matchesAny(
            normalized,
            listOf(
                "CHOPP", "IPA", "PALE ALE", "LAGER", "PILSEN", "PILSNER",
                "BLUE MOON", "BADEN BADEN", "KAISERDOM", "TUPINIQUIM", "ROLETA RUSSA",
                "VINHO", "AURORA", "CONCHA Y TORO"
            )
        ) ||
            isSafeShortTokenMatch(normalized, "VH", listOf("AURORA", "C SAUV", "CARM", "RESERVADO", "VINHO")) ||
            isSafeShortTokenMatch(normalized, "ALE", listOf("PALE", "IPA", "CERVEJA", "CHOPP")) ||
            normalized.contains("C SAUV") ||
            normalized.contains("CARM") ||
            normalized.contains("RESERVADO")
    }

    /**
     * Detects energy drinks.
     */
    private fun isEnergyDrink(normalized: String): Boolean {
        return normalized.contains("MONSTER") ||
            normalized.contains("ENERGETICO") ||
            isSafeShortTokenMatch(normalized, "ENERG", listOf("MONSTER", "RED BULL", "ENERGETICO"))
    }

    /**
     * Detects personal hygiene products.
     */
    private fun isPersonalHygiene(normalized: String): Boolean {
        if (normalized.contains("P TOALHA") || (normalized.contains("TOALHA PAPEL") && !normalized.contains("UMED"))) {
            return false
        }

        return matchesAny(
            normalized,
            listOf(
                "SHAMPOO", "HEAD", "CLEAR", "DOVE", "REXONA", "GILLETTE", "COLGATE",
                "OLLA", "LENCO UMED", "TOALHA UMED", "HUGGIES", "PAPEL HIGIENICO", "HIGIENICO"
            )
        ) ||
            isSafeShortTokenMatch(normalized, "CR D", listOf("COLGATE", "DENTAL")) ||
            isSafeShortTokenMatch(normalized, "SAB", listOf("DOVE", "SABONETE")) ||
            isSafeShortTokenMatch(normalized, "SH", listOf("HEAD", "CLEAR", "SHAMPOO")) ||
            isSafeShortTokenMatch(normalized, "DES", listOf("REXONA", "DOVE", "DESODORANTE", "CLINICAL")) ||
            isSafeShortTokenMatch(normalized, "P H", listOf("NEVE", "HIGIEN")) ||
            normalized.contains("PRESERV")
    }

    /**
     * Detects house cleaning products.
     */
    private fun isHouseCleaning(normalized: String): Boolean {
        return matchesAny(
            normalized,
            listOf(
                "LAV LOUCA", "LAVA LOUCA", "OMO",
                "COMFORT", "SPLENDO", "AJAX", "PINHO SOL", "KALIPTO",
                "ESFREBOM", "SACO LIXO", "PAPEL TOALHA", "TOALHA PAPEL", "FILTRO PAPEL"
            )
        ) ||
            normalized.contains("DET LQ") ||
            normalized.contains("DESINF") ||
            normalized.contains("L ROUP") ||
            normalized.contains("ROUP PO") ||
            normalized.contains("P TOALHA") ||
            isSafeShortTokenMatch(normalized, "AMAC", listOf("COMFORT", "AMACIANTE")) ||
            isSafeShortTokenMatch(normalized, "LIMP", listOf("AJAX", "PISO", "LIMPADOR", "LIMPEZA"))
    }

    /**
     * Detects processed tomato products.
     */
    private fun isTomatoProcessed(normalized: String): Boolean {
        val hasTomato = normalized.contains("TOMATE")
        val isProcessed = normalized.contains("PASSATA") ||
            normalized.contains("MOLHO") ||
            normalized.contains("MOL") ||
            normalized.contains("S PELE") ||
            normalized.contains("PELADO") ||
            normalized.contains("EXTRATO")

        return hasTomato && isProcessed
    }

    /**
     * Detects fresh produce.
     */
    private fun isFreshProduce(normalized: String): Boolean {
        if (containsBeverageContext(normalized)) return false

        val produceMarkers = listOf("PIMENTAO", "CEBOLINHA", "MELANCIA", "BANANA", "MORANGO", "LARANJA")
        return matchesAny(normalized, produceMarkers) ||
            hasWholeWord(normalized, "ALHO") ||
            hasWholeWord(normalized, "MACA") ||
            hasWholeWord(normalized, "UVA") ||
            (normalized.contains("TOMATE") && !isTomatoProcessed(normalized))
    }

    /**
     * Detects meat and protein products.
     */
    private fun isMeat(normalized: String): Boolean {
        // Reject seasoning context
        if (
            normalized.contains("SAZON") ||
            normalized.contains("TEMPERO") ||
            normalized.contains("TEMP") ||
            containsSnackOrFrozenContext(normalized)
        ) {
            return false
        }

        return matchesAny(
            normalized,
            listOf(
                "PATINHO", "COXAO", "CARNE", "FRANGO", "LOMBO", "MORT", "MORTADELA",
                "PRESUNTO", "SALSICHA", "LINGUICA"
            )
        )
    }

    /**
     * Detects dairy products.
     */
    private fun isDairy(normalized: String): Boolean {
        if (containsSnackOrFrozenContext(normalized)) return false

        return matchesAny(
            normalized,
            listOf(
                "LEITE", "IOGURTE", "IOG", "QUEIJO", "QJO", "MUSSARELA",
                "MUCARELA", "MOZZARELLA", "REQUEIJAO", "MANTEIGA", "MARGARINA"
            )
        )
    }

    /**
     * Detects frozen and convenience foods.
     */
    private fun isFrozenOrConvenienceFood(normalized: String): Boolean {
        return matchesAny(
            normalized,
            listOf("PIZZA", "LASANHA", "NISSIN", "MAC NISSIN", "PIPOCA MIC", "WRAP", "TORTILHA")
        )
    }

    /**
     * Detects snacks and sweets.
     */
    private fun isSnackOrSweet(normalized: String): Boolean {
        return matchesAny(
            normalized,
            listOf(
                "SALG", "DORITOS", "FANDANGOS", "CHEETOS", "TRENTO",
                "BALA", "HALLS", "CHOC", "BOLO", "ROSCA"
            )
        )
    }

}
