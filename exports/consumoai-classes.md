# ConsumoAI source export

Generated: 2026-05-20 21:28:54  

Include tests: True  

Total files: 87

## FILE: app/src/main/java/com/example/consumoai/ConsumoAiApplication.kt

```kotlin
package com.example.consumoai

import android.app.Application
import com.example.consumoai.core.di.appModule
import com.example.consumoai.core.di.dataModule
import com.example.consumoai.core.di.domainModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ConsumoAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ConsumoAiApplication)
            modules(
                appModule,
                domainModule,
                dataModule
            )
        }
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/core/di/AppModule.kt

```kotlin
package com.example.consumoai.core.di

import com.example.consumoai.presentation.home.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        HomeViewModel(
            importSampleNfceReceiptsUseCase = get(),
            analyzeStoredReceiptsUseCase = get(),
            getStoredReceiptsSummaryUseCase = get(),
            clearReceiptsUseCase = get()
        )
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/core/di/DataModule.kt

```kotlin
package com.example.consumoai.core.di

import androidx.room.Room
import com.example.consumoai.BuildConfig
import com.example.consumoai.data.classifier.ConsumptionModelApi
import com.example.consumoai.data.classifier.KeywordProductClassifierDataSource
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.data.classifier.RemoteConsumptionBehaviorClassifier
import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.data.local.AppDatabase
import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.data.repository.ReceiptRepositoryImpl
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.classifier.ProductClassifier
import com.example.consumoai.domain.classifier.ProductSemanticTagger
import com.example.consumoai.domain.repository.ReceiptRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "consumoai-db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get<AppDatabase>().receiptDao()
    }

    single<ReceiptRepository> {
        ReceiptRepositoryImpl(
            receiptDao = get()
        )
    }

    single {
        NfceHtmlParserDataSource()
    }

    single {
        NfceQrCodeDataSource(
            nfceHtmlParserDataSource = get()
        )
    }

    single<ProductClassifier> {
        KeywordProductClassifierDataSource()
    }

    single<ProductSemanticTagger> {
        KeywordProductSemanticTagger()
    }

    single {
        OkHttpClient.Builder()
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.MODEL_API_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single {
        get<Retrofit>().create(ConsumptionModelApi::class.java)
    }

    single {
        RuleBasedConsumptionBehaviorClassifier()
    }

    single<ConsumptionBehaviorClassifier> {
        RemoteConsumptionBehaviorClassifier(
            api = get(),
            fallbackClassifier = get()
        )
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/core/di/DomainModule.kt

```kotlin
package com.example.consumoai.core.di

import com.example.consumoai.domain.insights.ConsumptionInsightsEngine
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.usecase.AnalyzeReceiptFromQrCodeUrlUseCase
import com.example.consumoai.domain.usecase.AnalyzeStoredReceiptsUseCase
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.BuildConsumptionProfileSummaryUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsV2UseCase
import com.example.consumoai.domain.usecase.ClearReceiptsUseCase
import com.example.consumoai.domain.usecase.ClassifyConsumptionProfileUseCase
import com.example.consumoai.domain.usecase.ClassifyProductsUseCase
import com.example.consumoai.domain.usecase.ConsumptionFeatureSanitizer
import com.example.consumoai.domain.usecase.GetStoredReceiptsSummaryUseCase
import com.example.consumoai.domain.usecase.ImportSampleNfceReceiptsUseCase
import com.example.consumoai.domain.usecase.SaveReceiptUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        AnalyzeReceiptFromQrCodeUrlUseCase(
            nfceQrCodeDataSource = get(),
            classifyProductsUseCase = get()
        )
    }

    factory {
        ClassifyProductsUseCase(
            productClassifier = get()
        )
    }

    factory { CalculateConsumptionMetricsUseCase() }

    factory { BuildConsumptionModelInputUseCase() }

    factory { ConsumptionFeatureSanitizer() }

    factory {
        CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = get(),
            semanticTagger = get()
        )
    }

    factory { BuildConsumptionProfileSummaryUseCase() }


    factory {
        ClassifyConsumptionProfileUseCase(
            consumptionBehaviorClassifier = get()
        )
    }


    factory {
        SaveReceiptUseCase(
            receiptRepository = get()
        )
    }

    factory {
        ImportSampleNfceReceiptsUseCase(
            analyzeReceiptFromQrCodeUrlUseCase = get(),
            saveReceiptUseCase = get(),
            receiptRepository = get()
        )
    }

    single<ConsumptionInsightsEngine> {
        DefaultConsumptionInsightsEngine()
    }

    factory {
        AnalyzeStoredReceiptsUseCase(
            receiptRepository = get(),
            calculateConsumptionMetricsV2UseCase = get(),
            buildConsumptionModelInputUseCase = get(),
            classifyConsumptionProfileUseCase = get(),
            insightsEngine = get(),
            consumptionFeatureSanitizer = get(),
            buildConsumptionProfileSummaryUseCase = get()
        )
    }

    factory {
        GetStoredReceiptsSummaryUseCase(
            receiptRepository = get()
        )
    }

    factory {
        ClearReceiptsUseCase(
            receiptRepository = get()
        )
    }

}
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/ConsumptionModelApi.kt

```kotlin
package com.example.consumoai.data.classifier
import retrofit2.http.Body
import retrofit2.http.POST
interface ConsumptionModelApi {
    @POST("predict")
    suspend fun predict(
        @Body request: ModelPredictionRequestDto
    ): ModelPredictionResponseDto
}
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/KeywordProductClassifierDataSource.kt

```kotlin
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
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/KeywordProductSemanticTagger.kt

```kotlin
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

        if (matchesAny(normalized, listOf("CHOPP", "IPA", "VINHO", "BADEN BADEN", "BLUE MOON", "CONCHA Y TORO", "AURORA"))) {
            tags += ProductSemanticTag.ALCOHOLIC_BEVERAGE
        }
        if (matchesAny(normalized, listOf("COCA", "GUARANA", "REFRIGERANTE", "REFRI", "FANTA", "SPRITE", "PEPSI"))) {
            tags += ProductSemanticTag.SOFT_DRINK
        }
        if (matchesAny(normalized, listOf("MONSTER", "ENERGETICO", "ENERG"))) {
            tags += ProductSemanticTag.ENERGY_DRINK
        }
        if (matchesAny(normalized, listOf("SUCO", "NECTAR", "DEL VALLE", "NATURALE"))) {
            tags += ProductSemanticTag.JUICE
        }
        if (matchesAny(normalized, listOf("DORITOS", "FANDANGOS", "TRENTO", "BALA", "SALG", "CHOC", "HALLS"))) {
            tags += ProductSemanticTag.SNACK_OR_SWEET
        }
        if (matchesAny(normalized, listOf("PIZZA", "LASANHA", "NISSIN", "MIOJO", "LAMEN"))) {
            tags += ProductSemanticTag.FROZEN_OR_READY_MEAL
        }
        if (matchesAny(normalized, listOf("LEITE", "QUEIJO", "QJO", "REQUEIJAO", "IOGURTE", "IOG"))) {
            tags += ProductSemanticTag.DAIRY
        }
        if (matchesAny(normalized, listOf("PATINHO", "COXAO", "FRANGO", "CARNE", "PRESUNTO", "MORTADELA", "LOMBO"))) {
            tags += ProductSemanticTag.MEAT_OR_PROTEIN
        }
        if (matchesAny(normalized, listOf("MELANCIA", "PIMENTAO", "ALHO", "CEBOLINHA", "TOMATE", "BANANA", "MACA", "MORANGO"))) {
            tags += ProductSemanticTag.FRESH_PRODUCE
        }
        if (matchesAny(normalized, listOf("SHAMPOO", "DOVE", "REXONA", "COLGATE", "SABONETE", "DESODORANTE"))) {
            tags += ProductSemanticTag.PERSONAL_CARE
        }
        if (matchesAny(normalized, listOf("OMO", "AJAX", "DET LQ", "DESINF", "DETERGENTE", "AMACIANTE"))) {
            tags += ProductSemanticTag.HOUSEHOLD_CLEANING
        }
        if (matchesAny(normalized, listOf("PEDIGREE", "PETHAND", "RACAO", "ALIM CAO"))) {
            tags += ProductSemanticTag.PET
        }
        if (matchesAny(normalized, listOf("CANECA", "FITA", "CADERNO", "PULVERIZ", "CAD "))) {
            tags += ProductSemanticTag.UTILITY
        }

        if (tags.isEmpty()) {
            tags += fallbackTagFromCategory(item.category)
        }

        return tags
    }

    private fun fallbackTagFromCategory(category: ProductCategory): ProductSemanticTag {
        return when (category) {
            ProductCategory.BEVERAGES -> ProductSemanticTag.JUICE
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

```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/ModelPredictionDtos.kt

```kotlin
package com.example.consumoai.data.classifier

data class ModelPredictionRequestDto(
    val version: String,
    val features: Map<String, Double>
)

// Resposta bruta devolvida pelo serviÃ§o de inferÃªncia.
data class ModelPredictionResponseDto(
    // Nome do perfil principal retornado pelo modelo.
    val main_profile: String,
    // ConfianÃ§a numÃ©rica associada ao perfil principal.
    val confidence: Double,
    // PontuaÃ§Ã£o de cada perfil considerado na inferÃªncia.
    val profile_scores: Map<String, Double>,
    // VersÃ£o do payload processado pelo backend (opcional).
    val version: String? = null,
    // Quantidade de features processadas pelo backend (opcional).
    val feature_count: Int? = null,
    // Identificador do modelo carregado no backend (opcional).
    val model: String? = null,
    // Lista de features efetivamente usadas pelo backend (opcional).
    val used_features: List<String>? = null
)
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/RemoteConsumptionBehaviorClassifier.kt

```kotlin
package com.example.consumoai.data.classifier

import android.util.Log
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FallbackReason
import retrofit2.HttpException

class RemoteConsumptionBehaviorClassifier(
    private val api: ConsumptionModelApi,
    private val fallbackClassifier: RuleBasedConsumptionBehaviorClassifier
) : ConsumptionBehaviorClassifier {

    private companion object {
        const val REQUEST_TAG = "MODEL_REQUEST"
        const val RESPONSE_TAG = "MODEL_RESPONSE"
        const val ERROR_TAG = "MODEL_ERROR"
        const val FALLBACK_TAG = "MODEL_FALLBACK"
    }

    override suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult {
        val startNanos = System.nanoTime()

        if (input.features.isEmpty()) {
            return fallback(
                input = input,
                reason = FallbackReason.EMPTY_FEATURES,
                durationMs = elapsedMillis(startNanos),
                details = "Nenhuma feature disponÃ­vel para enviar ao modelo."
            )
        }

        return try {
            safeLogDebug(
                REQUEST_TAG,
                "version=${input.version} features=${input.features.size}"
            )
            safeLogDebug(
                REQUEST_TAG,
                "feature_names=${input.features.keys.joinToString(",")}"
            )
            val response = api.predict(
                ModelPredictionRequestDto(
                    version = input.version,
                    features = input.features
                )
            )
            val durationMs = elapsedMillis(startNanos)
            safeLogDebug(
                RESPONSE_TAG,
                "version=${response.version ?: input.version} feature_count=${response.feature_count ?: input.features.size} main=${response.main_profile} confidence=${response.confidence} profile_scores=${response.profile_scores} model=${response.model ?: "unknown"} durationMs=$durationMs"
            )

            val mainProfile = response.main_profile.toBehaviorProfile()
            val mappedScores = response.profile_scores
                .mapKeys { (profile, _) -> profile.toBehaviorProfile() }
                .toMutableMap()
                .apply {
                    putIfAbsent(mainProfile, response.confidence)
                }
                .toMap()

            ConsumptionBehaviorResult(
                mainProfile = mainProfile,
                confidence = response.confidence.coerceIn(0.0, 1.0),
                profileScores = mappedScores,
                source = BehaviorClassificationSource.TRAINED_MODEL,
                inferenceDurationMs = durationMs,
                requestedInputVersion = input.version,
                requestedFeatureCount = input.features.size,
                responseVersion = response.version ?: input.version,
                responseFeatureCount = response.feature_count ?: input.features.size,
                backendModelUsed = response.model
            )
        } catch (error: Exception) {
            val durationMs = elapsedMillis(startNanos)
            val reason = when {
                error is HttpException && error.code() == 400 -> FallbackReason.BACKEND_REJECTED_INPUT
                error is IllegalArgumentException -> FallbackReason.INVALID_INPUT
                error.message?.contains("load", ignoreCase = true) == true -> FallbackReason.MODEL_LOAD_ERROR
                else -> FallbackReason.INFERENCE_ERROR
            }
            val httpCode = (error as? HttpException)?.code()
            val errorBody = (error as? HttpException)?.response()?.errorBody()?.string()
            safeLogError(
                "durationMs=$durationMs reason=$reason http_code=${httpCode ?: "n/a"} message=${error.message} error_body=${errorBody ?: "n/a"}\n${error.stackTraceToString()}"
            )
            fallback(
                input = input,
                reason = reason,
                durationMs = durationMs,
                details = error.message ?: "Erro desconhecido na inferÃªncia remota"
            )
        }
    }

    private suspend fun fallback(
        input: ConsumptionModelInput,
        reason: FallbackReason,
        durationMs: Long,
        details: String
    ): ConsumptionBehaviorResult {
        safeLogDebug(
            FALLBACK_TAG,
            "reason=$reason durationMs=$durationMs details=$details feature_count=${input.features.size}"
        )
        return fallbackClassifier.classify(input).copy(
            fallbackReason = reason,
            inferenceDurationMs = durationMs,
            requestedInputVersion = input.version,
            requestedFeatureCount = input.features.size
        )
    }

    private fun elapsedMillis(startNanos: Long): Long {
        return ((System.nanoTime() - startNanos) / 1_000_000L).coerceAtLeast(0L)
    }

    private fun safeLogDebug(tag: String, message: String) {
        runCatching { Log.d(tag, message) }
    }

    private fun safeLogError(message: String) {
        runCatching { Log.e(ERROR_TAG, message) }
    }

    private fun String.toBehaviorProfile(): ConsumptionBehaviorProfile {
        return runCatching { ConsumptionBehaviorProfile.valueOf(this) }
            .getOrDefault(ConsumptionBehaviorProfile.UNDEFINED)
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/RuleBasedConsumptionBehaviorClassifier.kt

```kotlin
package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Fallback local usado apenas quando o backend treinado nÃ£o estÃ¡ disponÃ­vel.
 * O fluxo principal permanece no classificador remoto (XGBoost V2 Top 15).
 */
@Suppress("unused")
class RuleBasedConsumptionBehaviorClassifier : ConsumptionBehaviorClassifier {

    override suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult {
        val profile = classifyProfile(input)
        return ConsumptionBehaviorResult(
            mainProfile = profile,
            confidence = 1.0,
            profileScores = mapOf(profile to 1.0),
            source = BehaviorClassificationSource.RULE_BASED_FALLBACK
        )
    }

    internal fun classifyProfile(input: ConsumptionModelInput): ConsumptionBehaviorProfile {
        val classifiedItemsPercentage = input.feature("classified_items_percentage")
        val categoryStabilityScore = input.feature("category_stability_score")
        val basicProduceCoOccurrence = input.feature("basic_produce_cooccurrence_frequency")
        val ticketVariationCoefficient = input.feature("ticket_variation_coefficient")
        val essentialRoutineScore = input.feature("essential_routine_score")
        val householdRoutineScore = input.feature("household_routine_score")
        val categoryConcentrationIndex = input.feature("category_concentration_index")
        val produceFrequency = input.feature("produce_frequency")
        val hygieneCleaningCoOccurrence = input.feature("hygiene_cleaning_cooccurrence_frequency")
        val otherValuePct = input.feature("other_value_pct")
        val beveragesFrequency = input.feature("beverages_frequency")
        val essentialScore = input.feature("essential_score")
        val categoryDominanceGap = input.feature("category_dominance_gap")
        val essentialCategoriesPercentage = input.feature("essential_categories_percentage")
        val beverageRoutineScore = input.feature("beverage_routine_score")

        return when {
            classifiedItemsPercentage < 0.50 -> ConsumptionBehaviorProfile.UNDEFINED
            categoryConcentrationIndex >= 0.70 && categoryDominanceGap >= 0.30 -> ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED
            beverageRoutineScore >= 0.55 || beveragesFrequency >= 0.60 -> ConsumptionBehaviorProfile.BEVERAGE_RECURRENT
            essentialRoutineScore >= 0.55 && essentialCategoriesPercentage >= 0.50 && essentialScore >= 0.55 -> ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
            householdRoutineScore >= 0.40 || hygieneCleaningCoOccurrence >= 0.35 -> ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE
            produceFrequency <= 0.15 && basicProduceCoOccurrence <= 0.20 -> ConsumptionBehaviorProfile.LOW_FRESH_FOOD
            otherValuePct >= 0.30 && ticketVariationCoefficient >= 0.45 -> ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION
            categoryStabilityScore >= 0.55 && categoryConcentrationIndex < 0.45 -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
            otherValuePct >= 0.20 -> ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED
            else -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
        }
    }

    private fun ConsumptionModelInput.feature(name: String): Double {
        return features[name] ?: 0.0
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/data/datasource/qrcode/NfceQrCodeDataSource.kt

```kotlin
package com.example.consumoai.data.datasource.qrcode

import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.domain.model.ParsedNfceReceipt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NfceQrCodeDataSource(
    private val nfceHtmlParserDataSource: NfceHtmlParserDataSource
) {

    suspend fun extractReceipt(url: String): ParsedNfceReceipt = withContext(Dispatchers.IO) {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android)")
            .timeout(20_000)
            .get()

        nfceHtmlParserDataSource.parse(document)
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/data/local/AppDatabase.kt

```kotlin
package com.example.consumoai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity

@Database(
    entities = [ReceiptEntity::class, ProductItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
}

```

## FILE: app/src/main/java/com/example/consumoai/data/local/dao/ReceiptDao.kt

```kotlin
package com.example.consumoai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems

@Dao
interface ReceiptDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ProductItemEntity>)

    @Transaction
    @Query("SELECT * FROM receipts ORDER BY id DESC")
    suspend fun getAllReceiptsWithItems(): List<ReceiptWithItems>

    @Query("DELETE FROM receipts")
    suspend fun deleteAllReceipts()

    @Query("SELECT EXISTS(SELECT 1 FROM receipts WHERE accessKeyOrUrl = :accessKeyOrUrl LIMIT 1)")
    suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean
}

```

## FILE: app/src/main/java/com/example/consumoai/data/local/entity/ProductItemEntity.kt

```kotlin
package com.example.consumoai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["receiptId"])]
)
data class ProductItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val receiptId: Long,
    val itemNumber: Int?,
    val name: String,
    val price: Double,
    val category: String
)

```

## FILE: app/src/main/java/com/example/consumoai/data/local/entity/ReceiptEntity.kt

```kotlin
package com.example.consumoai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    indices = [Index(value = ["accessKeyOrUrl"], unique = true)]
)
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val accessKeyOrUrl: String? = null,
    val date: String,
    val source: String,
    val totalValue: Double
)

```

## FILE: app/src/main/java/com/example/consumoai/data/local/entity/ReceiptWithItems.kt

```kotlin
package com.example.consumoai.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ReceiptWithItems(
    @Embedded
    val receipt: ReceiptEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "receiptId"
    )
    val items: List<ProductItemEntity>
)

```

## FILE: app/src/main/java/com/example/consumoai/data/mapper/ReceiptMapper.kt

```kotlin
package com.example.consumoai.data.mapper

import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import java.time.LocalDate

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    accessKeyOrUrl = accessKeyOrUrl,
    date = date.toString(),
    source = source.name,
    totalValue = totalValue
)

fun ProductItem.toEntity(receiptId: Long): ProductItemEntity = ProductItemEntity(
    id = id,
    receiptId = receiptId,
    itemNumber = itemNumber,
    name = name,
    price = price,
    category = category.name
)

fun ReceiptWithItems.toDomain(): Receipt = Receipt(
    id = receipt.id,
    accessKeyOrUrl = receipt.accessKeyOrUrl,
    date = LocalDate.parse(receipt.date),
    source = ReceiptSource.valueOf(receipt.source),
    items = items.map { item ->
        ProductItem(
            id = item.id,
            receiptId = item.receiptId,
            itemNumber = item.itemNumber,
            name = item.name,
            price = item.price,
            category = item.category.toCategory()
        )
    }
)

private fun String.toCategory(): ProductCategory {
    return ProductCategory.entries.firstOrNull { it.name == this } ?: ProductCategory.OTHER
}

```

## FILE: app/src/main/java/com/example/consumoai/data/parser/NfceHtmlParserDataSource.kt

```kotlin
package com.example.consumoai.data.parser

import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ParsedNfceReceipt
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

class NfceHtmlParserDataSource {

    private val moneyRegex = Regex("(\\d{1,4}[,.]\\d{2})")
    private val rowRegex = Regex("^(\\d{1,3})\\s+(.+?)\\s+(\\d{1,4}[,.]\\d{2})$")
    private val forbiddenLineTokens = setOf(
        "QTD TOTAL", "VALOR TOTAL", "VALOR PAGO", "FORMA DE PAGAMENTO", "CONSUMIDOR", "CHAVE", "PROTOCOLO"
    )

    fun parse(document: Document): ParsedNfceReceipt {
        val products = parseTabResultTable(document)
            .ifEmpty { parseByRows(document) }
            .ifEmpty {
            parseFallbackFromText(document.body().text())
            }
            .distinctBy { "${it.itemNumber}|${it.name}|${formatPrice(it.price)}" }
            .sortedBy { it.itemNumber ?: Int.MAX_VALUE }

        return ParsedNfceReceipt(
            items = products,
            issueDate = extractIssueDate(document)
        )
    }

    private fun extractIssueDate(document: Document): LocalDate? {
        val bodyText = normalizeSpaces(document.body().text())

        val emissionPatterns = listOf(
            Regex("(?:EMISSAO|DATA DE EMISSAO|DATA EMISSAO)\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4})(?:\\s+(\\d{2}:\\d{2}:\\d{2}))?"),
            Regex("\\b(\\d{2}/\\d{2}/\\d{4})\\s+(\\d{2}:\\d{2}:\\d{2})\\b"),
            Regex("\\b(\\d{2}/\\d{2}/\\d{4})\\b")
        )

        val candidate = emissionPatterns.firstNotNullOfOrNull { regex ->
            regex.find(bodyText)?.groupValues?.drop(1)?.firstOrNull { it.isNotBlank() }
        }

        return parseDate(candidate)
    }

    private fun parseDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null

        val trimmed = value.trim()
        val dateFormats = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
        )

        dateFormats.forEach { formatter ->
            try {
                return LocalDate.parse(trimmed, formatter)
            } catch (_: DateTimeParseException) {
            }
        }

        val dateTimeFormats = listOf(
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("d/M/yyyy H:mm:ss")
        )
        dateTimeFormats.forEach { formatter ->
            try {
                return LocalDateTime.parse(trimmed, formatter).toLocalDate()
            } catch (_: DateTimeParseException) {
            }
        }

        return null
    }

    private fun parseTabResultTable(document: Document): List<ProductItem> {
        val rows = document.select("table#tabResult tr")
        if (rows.isEmpty()) return emptyList()

        return rows.mapNotNull { row ->
            val rawId = row.id().trim()
            val itemNumber = Regex("(\\d{1,3})").find(rawId)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: return@mapNotNull null

            val name = normalizeSpaces(row.selectFirst("span.txtTit")?.text().orEmpty())
                .replace("**", "")
                .trim()
            if (name.isBlank()) return@mapNotNull null

            val priceText = row.selectFirst("span.valor")?.text().orEmpty()
            val price = parsePrice(priceText) ?: return@mapNotNull null

            ProductItem(
                itemNumber = itemNumber,
                name = name,
                price = price
            )
        }
    }

    private fun parseByRows(document: Document): List<ProductItem> {
        val products = mutableListOf<ProductItem>()

        val rows = document.select("tr, li, div")
            .map { it.text().trim() }
            .filter { it.isNotBlank() }

        for (row in rows) {
            val normalized = normalizeSpaces(row)
            if (containsForbiddenToken(normalized)) continue

            val match = rowRegex.find(normalized)
            if (match != null) {
                val itemNumber = match.groupValues[1].toIntOrNull()
                val name = match.groupValues[2].trim()
                val price = parsePrice(match.groupValues[3])
                if (itemNumber != null && !name.isBlank() && price != null) {
                    products.add(ProductItem(itemNumber = itemNumber, name = name, price = price))
                }
                continue
            }

            val itemNumber = Regex("^(\\d{1,3})\\b").find(normalized)?.groupValues?.getOrNull(1)?.toIntOrNull()
                ?: continue
            val prices = moneyRegex.findAll(normalized).map { it.groupValues[1] }.toList()
            if (prices.isEmpty()) continue

            val price = parsePrice(prices.last()) ?: continue
            val name = normalized
                .replaceFirst(Regex("^\\d{1,3}\\s+"), "")
                .replace(prices.last(), "")
                .trim()

            if (name.isNotBlank()) {
                products.add(ProductItem(itemNumber = itemNumber, name = name, price = price))
            }
        }

        return products
    }

    private fun parseFallbackFromText(bodyText: String): List<ProductItem> {
        val lines = bodyText
            .split(Regex("\\s{2,}|\\n"))
            .map { normalizeSpaces(it) }
            .filter { it.isNotBlank() }

        return lines.mapNotNull { line ->
            if (containsForbiddenToken(line)) return@mapNotNull null

            val match = rowRegex.find(line) ?: return@mapNotNull null
            val itemNumber = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val name = match.groupValues[2].trim()
            val price = parsePrice(match.groupValues[3]) ?: return@mapNotNull null

            ProductItem(itemNumber = itemNumber, name = name, price = price)
        }
    }

    private fun parsePrice(value: String): Double? {
        return value
            .replace(',', '.')
            .replace(Regex("[^\\d.]"), "")
            .toDoubleOrNull()
    }

    private fun containsForbiddenToken(line: String): Boolean {
        val upper = line.uppercase(Locale.ROOT)
        return forbiddenLineTokens.any { upper.contains(it) }
    }

    private fun normalizeSpaces(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun formatPrice(price: Double): String = "%.2f".format(Locale.US, price)
}

```

## FILE: app/src/main/java/com/example/consumoai/data/repository/ReceiptRepositoryImpl.kt

```kotlin
package com.example.consumoai.data.repository

import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.mapper.toDomain
import com.example.consumoai.data.mapper.toEntity
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.repository.ReceiptRepository

class ReceiptRepositoryImpl(
    private val receiptDao: ReceiptDao
) : ReceiptRepository {

    override suspend fun saveReceipt(receipt: Receipt) {
        val key = receipt.accessKeyOrUrl
        if (!key.isNullOrBlank() && receiptDao.existsByAccessKeyOrUrl(key)) {
            return
        }

        val receiptId = receiptDao.insertReceipt(receipt.toEntity())
        val items = receipt.items.map { item -> item.toEntity(receiptId) }
        if (items.isNotEmpty()) {
            receiptDao.insertItems(items)
        }
    }

    override suspend fun getAllReceipts(): List<Receipt> {
        return receiptDao.getAllReceiptsWithItems().map { it.toDomain() }
    }

    override suspend fun clearReceipts() {
        receiptDao.deleteAllReceipts()
    }

    override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean {
        return receiptDao.existsByAccessKeyOrUrl(accessKeyOrUrl)
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/classifier/ConsumptionBehaviorClassifier.kt

```kotlin
package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

interface ConsumptionBehaviorClassifier {
	suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/classifier/ProductClassifier.kt

```kotlin
package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ProductItem

interface ProductClassifier {
    fun classify(item: ProductItem): ProductItem
    fun classifyAll(items: List<ProductItem>): List<ProductItem>
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/classifier/ProductSemanticTagger.kt

```kotlin
package com.example.consumoai.domain.classifier

import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductSemanticTag

interface ProductSemanticTagger {
    fun tagsFor(item: ProductItem): Set<ProductSemanticTag>
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/insights/ConsumptionInsightsEngine.kt

```kotlin
package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.ConsumptionBehaviorAnalysis
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionMetricsV2

interface ConsumptionInsightsEngine {
    fun generate(
        metricsV2: ConsumptionMetricsV2,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/insights/DefaultConsumptionInsightsEngine.kt

```kotlin
package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.BehaviorCompositionItem
import com.example.consumoai.domain.model.ConsumptionBehaviorAnalysis
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionInsight
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionMetricsV2
import com.example.consumoai.domain.model.InsightSeverity
import com.example.consumoai.domain.model.InsightType
import com.example.consumoai.domain.model.ProductCategory

class DefaultConsumptionInsightsEngine : ConsumptionInsightsEngine {

    override fun generate(
        metricsV2: ConsumptionMetricsV2,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis {
        val metrics = metricsV2.baseMetrics
        val insights = mutableListOf<ConsumptionInsight>()

        // Generate primary insights based on metrics and model scores
        generateRecurrenceInsights(metrics, insights)
        generateDiversityInsights(metrics, insights)
        generateConcentrationInsights(metrics, insights)
        generateCategoryDominanceInsights(metrics, insights)
        generateFreshFoodInsights(metrics, insights)
        generateBalanceInsights(metrics, insights)
        generateCompositeInsights(metrics, result, insights)
        generateHybridBehaviorInsights(result, insights)
        generateConfidenceInsights(result, insights)

        val sortedInsights = insights.sortedWith(
            compareByDescending<ConsumptionInsight> { it.severity.toPriority() }
                .thenByDescending { it.relatedProfiles.size }
                .thenBy { it.title }
        )

        // Generate behavioral composition from profile scores
        val composition = generateBehavioralComposition(result)

        // Generate text summary
        val summary = generateSummary(result, metrics, composition)

        return ConsumptionBehaviorAnalysis(
            behaviorResult = result,
            insights = sortedInsights,
            summary = summary,
            behavioralComposition = composition
        )
    }

    private fun generateRecurrenceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val beverageFrequency = metrics.frequencyByCategory[ProductCategory.BEVERAGES] ?: 0.0
        
        if (beverageFrequency >= 0.70) {
            insights.add(
                ConsumptionInsight(
                    title = "Bebidas aparecem com alta recorrÃªncia",
                    description = "Bebidas estiveram presentes em grande parte das compras analisadas.",
                    type = InsightType.RECURRENCE,
                    severity = InsightSeverity.HIGH,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT),
                    relatedFeatures = listOf("beverages_frequency")
                )
            )
        }
    }

    private fun generateDiversityInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.categoryDiversityIndex < 0.40) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa diversidade de categorias",
                    description = "As compras analisadas apresentam baixa variedade entre categorias de consumo.",
                    type = InsightType.DIVERSITY,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("category_diversity_index")
                )
            )
        }
    }

    private fun generateConcentrationInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.categoryConcentrationIndex >= 0.60) {
            insights.add(
                ConsumptionInsight(
                    title = "Consumo mais concentrado",
                    description = "Grande parte do valor consumido estÃ¡ concentrado em poucas categorias.",
                    type = InsightType.CONCENTRATION,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("category_concentration_index")
                )
            )
        }
    }

    private fun generateCategoryDominanceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val industrializedPercentage = metrics.valuePercentageByCategory[ProductCategory.INDUSTRIALIZED] ?: 0.0

        if (industrializedPercentage >= 0.35) {
            insights.add(
                ConsumptionInsight(
                    title = "Alta presenÃ§a de industrializados",
                    description = "Produtos industrializados representam parcela relevante das compras analisadas.",
                    type = InsightType.CATEGORY_DOMINANCE,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED),
                    relatedFeatures = listOf("industrialized_value_pct")
                )
            )
        }
    }

    private fun generateFreshFoodInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        val producePercentage = metrics.produceToTotalRatio

        if (producePercentage <= 0.05) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa presenÃ§a de alimentos frescos",
                    description = "As compras apresentam baixa participaÃ§Ã£o de hortifruti e alimentos frescos.",
                    type = InsightType.CONSUMPTION_BALANCE,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.LOW_FRESH_FOOD),
                    relatedFeatures = listOf("produce_value_pct")
                )
            )
        }
    }

    private fun generateBalanceInsights(
        metrics: ConsumptionMetrics,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (metrics.diversityScore >= 0.65 && metrics.categoryConcentrationIndex <= 0.35) {
            insights.add(
                ConsumptionInsight(
                    title = "PadrÃ£o de consumo equilibrado",
                    description = "As categorias de consumo estÃ£o relativamente distribuÃ­das entre as compras.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.LOW,
                    relatedProfiles = listOf(ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED),
                    relatedFeatures = listOf("diversity_score", "category_concentration_index")
                )
            )
        }
    }

    private fun generateCompositeInsights(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        val topProfiles = result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }

        val beveragesStrong = metrics.beveragesToTotalRatio > 0.25
        val diversityStrong = metrics.diversityScore > 0.45
        val essentialsStrong = metrics.essentialScore > 0.50
        val householdStrong = (metrics.valuePercentageByCategory[ProductCategory.HYGIENE] ?: 0.0) +
            (metrics.valuePercentageByCategory[ProductCategory.CLEANING] ?: 0.0) > 0.22

        if (
            beveragesStrong &&
            diversityStrong &&
            essentialsStrong &&
            topProfiles.contains(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT) &&
            topProfiles.contains(ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED)
        ) {
            insights.add(
                ConsumptionInsight(
                    title = "EquilÃ­brio entre itens essenciais e bebidas",
                    description = "Seu consumo demonstra equilÃ­brio entre itens essenciais e bebidas recorrentes.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.MEDIUM,
                    relatedProfiles = listOf(
                        ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                        ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED,
                        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
                    ),
                    relatedFeatures = listOf("beverages_value_pct", "diversity_score", "essential_score")
                )
            )
        }

        if (
            householdStrong &&
            essentialsStrong &&
            topProfiles.contains(ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE)
        ) {
            insights.add(
                ConsumptionInsight(
                    title = "Rotina domÃ©stica bem marcada",
                    description = "As compras combinam manutenÃ§Ã£o domÃ©stica com presenÃ§a consistente de itens essenciais.",
                    type = InsightType.BEHAVIORAL_PATTERN,
                    severity = InsightSeverity.LOW,
                    relatedProfiles = listOf(
                        ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE,
                        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
                    ),
                    relatedFeatures = listOf("hygiene_value_pct", "cleaning_value_pct", "essential_score")
                )
            )
        }
    }

    private fun generateHybridBehaviorInsights(
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        // Find second highest probability
        val sortedScores = result.profileScores.values.sortedDescending()
        if (sortedScores.size >= 2 && sortedScores[1] >= 0.25) {
            insights.add(
                ConsumptionInsight(
                    title = "Comportamento hÃ­brido identificado",
                    description = "O consumo apresenta sinais relevantes de mÃºltiplos perfis comportamentais.",
                    type = InsightType.MODEL_INTERPRETATION,
                    severity = InsightSeverity.LOW,
                    relatedFeatures = listOf("second_highest_probability")
                )
            )
        }
    }

    private fun generateConfidenceInsights(
        result: ConsumptionBehaviorResult,
        insights: MutableList<ConsumptionInsight>
    ) {
        if (result.confidence < 0.50) {
            insights.add(
                ConsumptionInsight(
                    title = "Baixa confianÃ§a da classificaÃ§Ã£o",
                    description = "O modelo identificou sinais comportamentais menos consistentes nas compras analisadas.",
                    type = InsightType.MODEL_INTERPRETATION,
                    severity = InsightSeverity.MEDIUM,
                    relatedFeatures = listOf("confidence")
                )
            )
        }
    }

    private fun generateBehavioralComposition(
        result: ConsumptionBehaviorResult
    ): List<BehaviorCompositionItem> {
        return result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (profile, score) ->
                BehaviorCompositionItem(
                    profile = profile,
                    percentage = (score * 100).coerceIn(0.0, 100.0)
                )
            }
    }

    private fun generateSummary(
        result: ConsumptionBehaviorResult,
        metrics: ConsumptionMetrics,
        composition: List<BehaviorCompositionItem>
    ): String {
        return buildString {
            append("As compras analisadas apresentam predominÃ¢ncia de consumo com perfil ")
            append(getProfileDescription(result.mainProfile))
            append(".")

            if (composition.size >= 2) {
                append(" O segundo padrÃ£o identificado Ã© ")
                append(getProfileDescription(composition[1].profile))
                append(".")
            }

            if (metrics.categoryConcentrationIndex >= 0.60) {
                append(" O padrÃ£o geral mostra concentraÃ§Ã£o de gasto em poucas categorias.")
            } else if (metrics.diversityScore >= 0.65) {
                append(" A distribuiÃ§Ã£o entre categorias Ã© relativamente equilibrada.")
            }

            if (metrics.produceToTotalRatio <= 0.05) {
                append(" Destaca-se a baixa presenÃ§a de alimentos frescos.")
            }
        }
    }

    private fun getProfileDescription(profile: ConsumptionBehaviorProfile): String {
        return when (profile) {

            ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "orientado ao consumo de conveniÃªncia"
            ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "focado em itens essenciais"
            ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "diversificado e equilibrado"
            ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "recorrente em bebidas"
            ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "com baixa presenÃ§a de alimentos frescos"
            ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "focado em higiene e limpeza"
            ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "muito concentrado em categoria"
            ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "com caracterÃ­sticas impulsivas"
            ConsumptionBehaviorProfile.UNDEFINED -> "nÃ£o claramente definido"
        }
    }

    private fun InsightSeverity.toPriority(): Int {
        return when (this) {
            InsightSeverity.HIGH -> 3
            InsightSeverity.MEDIUM -> 2
            InsightSeverity.LOW -> 1
        }
    }
}


```

## FILE: app/src/main/java/com/example/consumoai/domain/model/CategoryMetrics.kt

```kotlin
package com.example.consumoai.domain.model

data class CategoryMetrics(
    val category: ProductCategory,
    val totalValue: Double,
    val totalItems: Int,
    val valuePercentage: Double,
    val itemPercentage: Double,
    val frequency: Double,
    val averageValuePerReceipt: Double,
    val averageItemsPerReceipt: Double
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionBehaviorAnalysis.kt

```kotlin
package com.example.consumoai.domain.model

data class ConsumptionBehaviorAnalysis(
    val behaviorResult: ConsumptionBehaviorResult,
    val insights: List<ConsumptionInsight>,
    val summary: String,
    val behavioralComposition: List<BehaviorCompositionItem>
)

data class BehaviorCompositionItem(
    val profile: ConsumptionBehaviorProfile,
    val percentage: Double
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionBehaviorProfile.kt

```kotlin
package com.example.consumoai.domain.model

/**
 * Perfis comportamentais retornados pela classificaÃ§Ã£o (modelo treinado ou fallback tÃ©cnico).
 */
enum class ConsumptionBehaviorProfile {
    // Perfil com maior peso de praticidade, industrializados e compras rÃ¡pidas.
    CONVENIENCE_ORIENTED,
    // Perfil com predominÃ¢ncia de itens essenciais e alimentaÃ§Ã£o bÃ¡sica.
    ESSENTIAL_FOCUSED,
    // Perfil com distribuiÃ§Ã£o mais equilibrada entre diferentes categorias de consumo.
    DIVERSIFIED_BALANCED,
    // Perfil em que bebidas aparecem com alta recorrÃªncia nas notas.
    BEVERAGE_RECURRENT,
    // Perfil com baixa presenÃ§a de hortifruti e alimentos frescos.
    LOW_FRESH_FOOD,
    // Perfil com destaque para higiene, limpeza e manutenÃ§Ã£o da casa.
    HOUSEHOLD_MAINTENANCE,
    // Perfil com gasto concentrado em poucas categorias dominantes.
    HIGHLY_CONCENTRATED,
    // Perfil com maior presenÃ§a de compras nÃ£o essenciais e sinais de impulso.
    IMPULSIVE_CONSUMPTION,
    // SaÃ­da usada quando nÃ£o hÃ¡ confianÃ§a suficiente para definir um padrÃ£o claro.
    UNDEFINED
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionBehaviorResult.kt

```kotlin
package com.example.consumoai.domain.model

// Resultado consolidado da classificaÃ§Ã£o de perfil de consumo.
data class ConsumptionBehaviorResult(
    // Perfil principal escolhido como saÃ­da final da classificaÃ§Ã£o.
    val mainProfile: ConsumptionBehaviorProfile,
    // ConfianÃ§a numÃ©rica da saÃ­da principal.
    val confidence: Double,
    // PontuaÃ§Ãµes de todos os perfis avaliados pelo classificador.
    val profileScores: Map<ConsumptionBehaviorProfile, Double>,
    // Origem da classificaÃ§Ã£o: modelo treinado ou fallback local.
    val source: BehaviorClassificationSource,
    // Resumo interpretativo montado para exibiÃ§Ã£o mais humana.
    val profileSummary: ConsumptionProfileSummary? = null,
    // Motivo do fallback, quando o backend/modelo principal nÃ£o foi usado.
    val fallbackReason: FallbackReason? = null,
    // Tempo gasto para produzir a classificaÃ§Ã£o.
    val inferenceDurationMs: Long = 0L,
    // VersÃ£o do input efetivamente enviada ao backend.
    val requestedInputVersion: String? = null,
    // Quantidade de features efetivamente enviada ao backend.
    val requestedFeatureCount: Int? = null,
    // VersÃ£o informada na resposta do backend, quando disponÃ­vel.
    val responseVersion: String? = null,
    // Quantidade de features informada na resposta do backend, quando disponÃ­vel.
    val responseFeatureCount: Int? = null,
    // Nome do modelo usado no backend, quando disponÃ­vel.
    val backendModelUsed: String? = null,
    // Indica se o input precisou ser saneado antes da inferÃªncia.
    val usedSanitizedInput: Boolean = false,
    // Lista de ajustes aplicados no saneamento do input.
    val sanitizationNotes: List<FeatureSanitizationNote> = emptyList()
)

enum class BehaviorClassificationSource {
    // SaÃ­da produzida pelo modelo treinado/servido pelo backend.
    TRAINED_MODEL,
    // SaÃ­da produzida por regras locais quando o modelo principal nÃ£o estÃ¡ disponÃ­vel.
    RULE_BASED_FALLBACK
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionInsight.kt

```kotlin
package com.example.consumoai.domain.model

data class ConsumptionInsight(
    val title: String,
    val description: String,
    val type: InsightType,
    val severity: InsightSeverity,
    val relatedProfiles: List<ConsumptionBehaviorProfile> = emptyList(),
    val relatedFeatures: List<String> = emptyList()
)

enum class InsightType {
    BEHAVIORAL_PATTERN,
    CATEGORY_DOMINANCE,
    RECURRENCE,
    DIVERSITY,
    CONCENTRATION,
    CONSUMPTION_BALANCE,
    PURCHASE_PATTERN,
    MODEL_INTERPRETATION
}

enum class InsightSeverity {
    LOW,
    MEDIUM,
    HIGH
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionMetrics.kt

```kotlin
package com.example.consumoai.domain.model

/**
 * Full analytics payload used by the app UI, debugging, and future experiments.
 * Nem todas as mÃ©tricas calculadas fazem parte da entrada oficial do modelo.
 */
data class ConsumptionMetrics(
    // MÃ©tricas por categoria
    val valuePercentageByCategory: Map<ProductCategory, Double>,
    val itemPercentageByCategory: Map<ProductCategory, Double>,
    val frequencyByCategory: Map<ProductCategory, Double>,
    val categoryMetrics: Map<ProductCategory, CategoryMetrics>,

    val categoryValueTotals: Map<ProductCategory, Double>,
    val categoryItemTotals: Map<ProductCategory, Int>,

    // MÃ©tricas gerais
    val totalReceipts: Int,
    val totalItems: Int,
    val totalValue: Double,
    val averageTicket: Double,
    val averageItemsPerReceipt: Double,
    val maxCategoryByValue: ProductCategory?,
    val maxCategoryByItems: ProductCategory?,
    val receiptAverageValueByCategory: Map<ProductCategory, Double>,
    val categoryConcentrationIndex: Double,
    val topThreeCategoriesByValue: List<ProductCategory>,
    val averageValuePerItem: Double,
    val highestReceiptValue: Double,
    val lowestReceiptValue: Double,
    val receiptValueAmplitude: Double,
    val highValueReceiptsPercentage: Double,
    val lowValueReceiptsPercentage: Double,
    val categoryDominanceGap: Double,
    val topThreeCategoriesValuePercentage: Double,
    val otherPercentageByValue: Double,
    val otherPercentageByItems: Double,
    val classifiedItemsPercentage: Double,
    val averageCategoriesPerReceipt: Double,
    val categoryDiversityIndex: Double,

    // MÃ©tricas comportamentais
    val essentialCategoriesPercentage: Double,
    val nonEssentialCategoriesPercentage: Double,
    val industrializedToBasicFoodRatio: Double,
    val beveragesToBasicFoodRatio: Double,
    val beveragesToTotalRatio: Double,
    val produceToTotalRatio: Double,
    val receiptsWithIndustrializedPercentage: Double,
    val receiptsWithBeveragesPercentage: Double,
    val receiptsWithBasicFoodPercentage: Double,
    val receiptsWithProducePercentage: Double,
    val receiptsWithHygienePercentage: Double,
    val receiptsWithCleaningPercentage: Double,
    val averageIndustrializedItemsPerReceipt: Double,
    val averageBeveragesItemsPerReceipt: Double,
    val averageBasicFoodItemsPerReceipt: Double,
    val averageProduceItemsPerReceipt: Double,

    // Scores-base reaproveitados nas mÃ©tricas e narrativa do fluxo V2.
    val convenienceScore: Double,
    val essentialScore: Double,
    val diversityScore: Double
)
```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionMetricsV2.kt

```kotlin
package com.example.consumoai.domain.model

data class ConsumptionMetricsV2(
    val baseMetrics: ConsumptionMetrics,
    val timeSpanDays: Double,
    val receiptsPerWeek: Double,
    val averageDaysBetweenReceipts: Double,
    val purchaseRegularityScore: Double,
    val ticketStandardDeviation: Double,
    val ticketVariationCoefficient: Double,
    val itemCountVariationCoefficient: Double,
    val highTicketReceiptsPercentage: Double,
    val lowTicketReceiptsPercentage: Double,
    val categoryStabilityScore: Double,
    val averageCategoryOverlapBetweenReceipts: Double,
    val recurringItemRatio: Double,
    val topItemRepetitionRate: Double,
    val beverageSnackCoOccurrenceFrequency: Double,
    val alcoholSnackCoOccurrenceFrequency: Double,
    val hygieneCleaningCoOccurrenceFrequency: Double,
    val basicProduceCoOccurrenceFrequency: Double,
    val alcoholicBeverageValuePct: Double,
    val alcoholicBeverageFrequency: Double,
    val softDrinkValuePct: Double,
    val softDrinkFrequency: Double,
    val energyDrinkValuePct: Double,
    val energyDrinkFrequency: Double,
    val snackSweetValuePct: Double,
    val snackSweetFrequency: Double,
    val frozenConvenienceValuePct: Double,
    val frozenConvenienceFrequency: Double,
    val dairyValuePct: Double,
    val meatProteinValuePct: Double,
    val freshProduceValuePct: Double,
    val convenienceMealValuePct: Double,
    val convenienceMealFrequency: Double,
    val essentialRoutineScore: Double,
    val convenienceRoutineScore: Double,
    val beverageRoutineScore: Double,
    val householdRoutineScore: Double,
    val freshFoodPresenceScore: Double
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionModelInput.kt

```kotlin
package com.example.consumoai.domain.model


/**
 * Official feature payload consumed by the behavior classifier and, in the future,
 * by the trained model integrated into the app.
 */
data class ConsumptionModelInput(
    val version: String = MODEL_INPUT_VERSION,
    val features: Map<String, Double>
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionProfileSummary.kt

```kotlin
package com.example.consumoai.domain.model

enum class ProfileInterpretationType {
    // Leitura em que um Ãºnico perfil aparece como predominante.
    PURE_PROFILE,
    // Leitura em que hÃ¡ mistura relevante entre dois ou mais perfis.
    HYBRID_PROFILE,
    // Leitura em que o sistema encontrou sinais fracos ou ambÃ­guos.
    LOW_CONFIDENCE_PROFILE
}

// Resumo textual e estrutural da saÃ­da do modelo para consumo na UI.
data class ConsumptionProfileSummary(
    // Perfil principal usado como eixo central da interpretaÃ§Ã£o.
    val primaryProfile: ConsumptionBehaviorProfile,
    // Perfis secundÃ¡rios que tambÃ©m influenciaram a leitura final.
    val secondaryProfiles: List<ConsumptionBehaviorProfile>,
    // ConfianÃ§a consolidada da interpretaÃ§Ã£o exibida.
    val confidence: Double,
    // Tipo de interpretaÃ§Ã£o aplicada sobre o resultado bruto.
    val interpretationType: ProfileInterpretationType,
    // DescriÃ§Ã£o curta em linguagem humana para exibiÃ§Ã£o no app.
    val humanReadableDescription: String,
    // ComposiÃ§Ã£o percentual dos perfis considerados na leitura final.
    val profileComposition: List<BehaviorCompositionItem>
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/FallbackReason.kt

```kotlin
package com.example.consumoai.domain.model

enum class FallbackReason {
    MODEL_LOAD_ERROR,
    INVALID_INPUT,
    BACKEND_REJECTED_INPUT,
    INFERENCE_ERROR,
    EMPTY_FEATURES
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/FeatureSanitizationNote.kt

```kotlin
package com.example.consumoai.domain.model

data class FeatureSanitizationNote(
    val featureName: String,
    val originalValue: Double?,
    val sanitizedValue: Double,
    val reason: String
)

data class SanitizedConsumptionModelInput(
    val input: ConsumptionModelInput,
    val notes: List<FeatureSanitizationNote>
) {
    val hasChanges: Boolean
        get() = notes.isNotEmpty()
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ImportReceiptsResult.kt

```kotlin
package com.example.consumoai.domain.model

data class ImportReceiptsResult(
    val importedCount: Int,
    val skippedCount: Int,
    val failedCount: Int
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ModelFeatureConstants.kt

```kotlin
package com.example.consumoai.domain.model

const val MODEL_INPUT_VERSION = "v2"

/**
 * Contagem total de mÃ©tricas calculadas internamente no app (para narrativa/anÃ¡lise).
 */
const val MODEL_V2_INTERNAL_METRICS_COUNT = 64

/**
 * Contagem de features OFICIAIS enviadas ao modelo XGBoost v2 treinado.
 * O modelo final usa apenas TOP 15 features.
 */
const val MODEL_V2_FINAL_FEATURE_COUNT = 15

/**
 * Ordem OFICIAL das 15 features enviadas ao backend XGBoost V2.
 * Ordem fixa: extraÃ­do de consumoai_v2_top15_features.json (Colab)
 * Modelo: consumoai_xgboost_v2_top15.pkl
 * Label Encoder: consumoai_label_encoder_v2.pkl
 *
 * Mantida fixa para preservar compatibilidade 1:1 com sklearn.predict_proba()
 */
val MODEL_V2_FINAL_FEATURES = listOf(
    "classified_items_percentage",
    "category_stability_score",
    "basic_produce_cooccurrence_frequency",
    "ticket_variation_coefficient",
    "essential_routine_score",
    "household_routine_score",
    "category_concentration_index",
    "produce_frequency",
    "hygiene_cleaning_cooccurrence_frequency",
    "other_value_pct",
    "beverages_frequency",
    "essential_score",
    "category_dominance_gap",
    "essential_categories_percentage",
    "beverage_routine_score"
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ParsedNfceReceipt.kt

```kotlin
package com.example.consumoai.domain.model

import java.time.LocalDate

data class ParsedNfceReceipt(
    val items: List<ProductItem>,
    val issueDate: LocalDate?
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ProductCategory.kt

```kotlin
package com.example.consumoai.domain.model

enum class ProductCategory {
    BASIC_FOOD,
    INDUSTRIALIZED,
    BEVERAGES,
    HYGIENE,
    CLEANING,
    PRODUCE,
    OTHER
}
```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ProductItem.kt

```kotlin
package com.example.consumoai.domain.model

data class ProductItem(
    val id: Long = 0L,
    val receiptId: Long = 0L,
    val itemNumber: Int? = null,
    val name: String,
    val price: Double,
    val category: ProductCategory = ProductCategory.OTHER
)
```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ProductSemanticTag.kt

```kotlin
package com.example.consumoai.domain.model

enum class ProductSemanticTag {
    ALCOHOLIC_BEVERAGE,
    SOFT_DRINK,
    ENERGY_DRINK,
    JUICE,
    SNACK_OR_SWEET,
    FROZEN_OR_READY_MEAL,
    DAIRY,
    MEAT_OR_PROTEIN,
    FRESH_PRODUCE,
    PERSONAL_CARE,
    HOUSEHOLD_CLEANING,
    PET,
    UTILITY,
    UNKNOWN
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/Receipt.kt

```kotlin
package com.example.consumoai.domain.model

import java.time.LocalDate

data class Receipt(
    val id: Long = 0L,
    val accessKeyOrUrl: String? = null,
    val date: LocalDate = LocalDate.now(),
    val source: ReceiptSource,
    val items: List<ProductItem>
) {
    val totalValue: Double
        get() = items.sumOf { it.price }
}
```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ReceiptSource.kt

```kotlin
package com.example.consumoai.domain.model

enum class ReceiptSource {
    QR_CODE
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/StoredConsumptionAnalysis.kt

```kotlin
package com.example.consumoai.domain.model

data class StoredConsumptionAnalysis(
    val receipts: List<Receipt>,
    val metricsV2: ConsumptionMetricsV2,
    val modelInput: ConsumptionModelInput,
    val behaviorResult: ConsumptionBehaviorResult,
    val behaviorAnalysis: ConsumptionBehaviorAnalysis
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/StoredReceiptsSummary.kt

```kotlin
package com.example.consumoai.domain.model

data class StoredReceiptsSummary(
    val totalReceipts: Int,
    val totalItems: Int,
    val totalValue: Double
)

```

## FILE: app/src/main/java/com/example/consumoai/domain/repository/ReceiptRepository.kt

```kotlin
package com.example.consumoai.domain.repository

import com.example.consumoai.domain.model.Receipt

interface ReceiptRepository {

    suspend fun saveReceipt(receipt: Receipt)

    suspend fun getAllReceipts(): List<Receipt>

    suspend fun clearReceipts()

    suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean
}
```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/AnalyzeReceiptFromQrCodeUrlUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import java.time.LocalDate

class AnalyzeReceiptFromQrCodeUrlUseCase(
    private val nfceQrCodeDataSource: NfceQrCodeDataSource,
    private val classifyProductsUseCase: ClassifyProductsUseCase
) {

    suspend operator fun invoke(url: String): Receipt {
        val parsedReceipt = nfceQrCodeDataSource.extractReceipt(url)
        val products = classifyProductsUseCase(parsedReceipt.items)
        return Receipt(
            accessKeyOrUrl = url,
            date = parsedReceipt.issueDate ?: LocalDate.now(),
            source = ReceiptSource.QR_CODE,
            items = products
        )
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/AnalyzeStoredReceiptsUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.insights.ConsumptionInsightsEngine
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.domain.repository.ReceiptRepository

class AnalyzeStoredReceiptsUseCase(
    private val receiptRepository: ReceiptRepository,
    private val calculateConsumptionMetricsV2UseCase: CalculateConsumptionMetricsV2UseCase,
    private val buildConsumptionModelInputUseCase: BuildConsumptionModelInputUseCase,
    private val classifyConsumptionProfileUseCase: ClassifyConsumptionProfileUseCase,
    private val insightsEngine: ConsumptionInsightsEngine,
    private val consumptionFeatureSanitizer: ConsumptionFeatureSanitizer,
    private val buildConsumptionProfileSummaryUseCase: BuildConsumptionProfileSummaryUseCase
) {

    suspend operator fun invoke(): StoredConsumptionAnalysis {
        val receipts = receiptRepository.getAllReceipts()
        if (receipts.isEmpty()) {
            throw IllegalStateException("Nenhuma nota armazenada para anÃ¡lise.")
        }
        val metricsV2 = calculateConsumptionMetricsV2UseCase(receipts)
        val rawModelInput = buildConsumptionModelInputUseCase(metricsV2)
        val sanitizedModelInput = consumptionFeatureSanitizer(rawModelInput)
        val classifiedResult = classifyConsumptionProfileUseCase(sanitizedModelInput.input)
        val profileSummary = buildConsumptionProfileSummaryUseCase(classifiedResult)
        val behaviorResult = classifiedResult.copy(
            profileSummary = profileSummary,
            usedSanitizedInput = sanitizedModelInput.hasChanges,
            sanitizationNotes = sanitizedModelInput.notes
        )
        val behaviorAnalysis = insightsEngine.generate(metricsV2, behaviorResult)

        return StoredConsumptionAnalysis(
            receipts = receipts,
            metricsV2 = metricsV2,
            modelInput = sanitizedModelInput.input,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        )
    }
}


```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/BuildConsumptionModelInputUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetricsV2
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURES
import com.example.consumoai.domain.model.ProductCategory

class BuildConsumptionModelInputUseCase {

    operator fun invoke(metricsV2: ConsumptionMetricsV2): ConsumptionModelInput {
        val base = metricsV2.baseMetrics
        val valuePercentages = base.valuePercentageByCategory
        val frequencies = base.frequencyByCategory

        val allFeatures = linkedMapOf(
            "total_receipts" to base.totalReceipts.toDouble(),
            "total_items" to base.totalItems.toDouble(),
            "total_value" to base.totalValue,
            "average_ticket" to base.averageTicket,
            "average_items_per_receipt" to base.averageItemsPerReceipt,
            "basic_food_value_pct" to valuePercentages[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_value_pct" to valuePercentages[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_value_pct" to valuePercentages[ProductCategory.BEVERAGES].orZero(),
            "hygiene_value_pct" to valuePercentages[ProductCategory.HYGIENE].orZero(),
            "cleaning_value_pct" to valuePercentages[ProductCategory.CLEANING].orZero(),
            "produce_value_pct" to valuePercentages[ProductCategory.PRODUCE].orZero(),
            "other_value_pct" to valuePercentages[ProductCategory.OTHER].orZero(),
            "basic_food_frequency" to frequencies[ProductCategory.BASIC_FOOD].orZero(),
            "industrialized_frequency" to frequencies[ProductCategory.INDUSTRIALIZED].orZero(),
            "beverages_frequency" to frequencies[ProductCategory.BEVERAGES].orZero(),
            "produce_frequency" to frequencies[ProductCategory.PRODUCE].orZero(),
            "hygiene_frequency" to frequencies[ProductCategory.HYGIENE].orZero(),
            "cleaning_frequency" to frequencies[ProductCategory.CLEANING].orZero(),
            "classified_items_percentage" to base.classifiedItemsPercentage,
            "category_concentration_index" to base.categoryConcentrationIndex,
            "category_dominance_gap" to base.categoryDominanceGap,
            "category_diversity_index" to base.categoryDiversityIndex,
            "essential_categories_percentage" to base.essentialCategoriesPercentage,
            "non_essential_categories_percentage" to base.nonEssentialCategoriesPercentage,
            "essential_score" to base.essentialScore,
            "convenience_score" to base.convenienceScore,
            "diversity_score" to base.diversityScore,
            "time_span_days" to metricsV2.timeSpanDays,
            "receipts_per_week" to metricsV2.receiptsPerWeek,
            "average_days_between_receipts" to metricsV2.averageDaysBetweenReceipts,
            "purchase_regularity_score" to metricsV2.purchaseRegularityScore,
            "ticket_standard_deviation" to metricsV2.ticketStandardDeviation,
            "ticket_variation_coefficient" to metricsV2.ticketVariationCoefficient,
            "item_count_variation_coefficient" to metricsV2.itemCountVariationCoefficient,
            "high_ticket_receipts_percentage" to metricsV2.highTicketReceiptsPercentage,
            "low_ticket_receipts_percentage" to metricsV2.lowTicketReceiptsPercentage,
            "category_stability_score" to metricsV2.categoryStabilityScore,
            "average_category_overlap_between_receipts" to metricsV2.averageCategoryOverlapBetweenReceipts,
            "recurring_item_ratio" to metricsV2.recurringItemRatio,
            "top_item_repetition_rate" to metricsV2.topItemRepetitionRate,
            "beverage_snack_cooccurrence_frequency" to metricsV2.beverageSnackCoOccurrenceFrequency,
            "alcohol_snack_cooccurrence_frequency" to metricsV2.alcoholSnackCoOccurrenceFrequency,
            "hygiene_cleaning_cooccurrence_frequency" to metricsV2.hygieneCleaningCoOccurrenceFrequency,
            "basic_produce_cooccurrence_frequency" to metricsV2.basicProduceCoOccurrenceFrequency,
            "alcoholic_beverage_value_pct" to metricsV2.alcoholicBeverageValuePct,
            "alcoholic_beverage_frequency" to metricsV2.alcoholicBeverageFrequency,
            "soft_drink_value_pct" to metricsV2.softDrinkValuePct,
            "soft_drink_frequency" to metricsV2.softDrinkFrequency,
            "energy_drink_value_pct" to metricsV2.energyDrinkValuePct,
            "energy_drink_frequency" to metricsV2.energyDrinkFrequency,
            "snack_sweet_value_pct" to metricsV2.snackSweetValuePct,
            "snack_sweet_frequency" to metricsV2.snackSweetFrequency,
            "frozen_convenience_value_pct" to metricsV2.frozenConvenienceValuePct,
            "frozen_convenience_frequency" to metricsV2.frozenConvenienceFrequency,
            "dairy_value_pct" to metricsV2.dairyValuePct,
            "meat_protein_value_pct" to metricsV2.meatProteinValuePct,
            "fresh_produce_value_pct" to metricsV2.freshProduceValuePct,
            "convenience_meal_value_pct" to metricsV2.convenienceMealValuePct,
            "convenience_meal_frequency" to metricsV2.convenienceMealFrequency,
            "essential_routine_score" to metricsV2.essentialRoutineScore,
            "convenience_routine_score" to metricsV2.convenienceRoutineScore,
            "beverage_routine_score" to metricsV2.beverageRoutineScore,
            "household_routine_score" to metricsV2.householdRoutineScore,
            "fresh_food_presence_score" to metricsV2.freshFoodPresenceScore
        )

        val selectedFeatures = linkedMapOf<String, Double>()
        MODEL_V2_FINAL_FEATURES.forEach { feature ->
            val value = allFeatures[feature]
                ?: error("Feature obrigatoria ausente: $feature")
            selectedFeatures[feature] = if (value.isNaN() || value.isInfinite()) 0.0 else value
        }

        return ConsumptionModelInput(
            version = MODEL_INPUT_VERSION,
            features = selectedFeatures
        )
    }
}

private fun Double?.orZero(): Double = this ?: 0.0

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/BuildConsumptionProfileSummaryUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.BehaviorCompositionItem
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionProfileSummary
import com.example.consumoai.domain.model.ProfileInterpretationType

class BuildConsumptionProfileSummaryUseCase {

    operator fun invoke(result: ConsumptionBehaviorResult): ConsumptionProfileSummary {
        val composition = result.profileScores
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (profile, score) ->
                BehaviorCompositionItem(
                    profile = profile,
                    percentage = (score * 100).coerceIn(0.0, 100.0)
                )
            }

        val primaryProfile = composition.firstOrNull()?.profile ?: result.mainProfile
        val secondaryProfiles = composition.drop(1).map { it.profile }
        val interpretationType = resolveInterpretationType(result, composition)

        return ConsumptionProfileSummary(
            primaryProfile = primaryProfile,
            secondaryProfiles = secondaryProfiles,
            confidence = result.confidence,
            interpretationType = interpretationType,
            humanReadableDescription = buildHumanReadableSummary(
                primaryProfile = primaryProfile,
                secondaryProfiles = secondaryProfiles,
                confidence = result.confidence,
                interpretationType = interpretationType
            ),
            profileComposition = composition
        )
    }

    fun buildHumanReadableSummary(
        primaryProfile: ConsumptionBehaviorProfile,
        secondaryProfiles: List<ConsumptionBehaviorProfile>,
        confidence: Double,
        interpretationType: ProfileInterpretationType
    ): String {
        return when (interpretationType) {
            ProfileInterpretationType.PURE_PROFILE -> {
                "PadrÃ£o predominante de ${primaryProfile.toReadableFragment()} com confianÃ§a de ${(confidence * 100).toInt()}%."
            }
            ProfileInterpretationType.HYBRID_PROFILE -> {
                val secondaryText = secondaryProfiles
                    .take(2)
                    .joinToString(" e ") { it.toReadableFragment() }
                    .ifBlank { "outros sinais complementares" }
                "PadrÃ£o hÃ­brido com predominÃ¢ncia de ${primaryProfile.toReadableFragment()} e influÃªncia de $secondaryText."
            }
            ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> {
                "Os sinais atuais indicam ${primaryProfile.toReadableFragment()}, mas com baixa confianÃ§a para definir um Ãºnico padrÃ£o dominante."
            }
        }
    }

    private fun resolveInterpretationType(
        result: ConsumptionBehaviorResult,
        composition: List<BehaviorCompositionItem>
    ): ProfileInterpretationType {
        val secondScore = composition.getOrNull(1)?.percentage?.div(100.0) ?: 0.0
        return when {
            result.confidence < 0.30 -> ProfileInterpretationType.LOW_CONFIDENCE_PROFILE
            result.confidence < 0.45 && secondScore >= 0.18 -> ProfileInterpretationType.HYBRID_PROFILE
            else -> ProfileInterpretationType.PURE_PROFILE
        }
    }

    private fun ConsumptionBehaviorProfile.toReadableFragment(): String {
        return when (this) {
            ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "consumo orientado Ã  conveniÃªncia"
            ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "foco em itens essenciais"
            ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "consumo diversificado e equilibrado"
            ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "recorrÃªncia de bebidas"
            ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "baixa presenÃ§a de alimentos frescos"
            ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "manutenÃ§Ã£o domÃ©stica"
            ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "consumo altamente concentrado"
            ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "consumo impulsivo"
            ConsumptionBehaviorProfile.UNDEFINED -> "um padrÃ£o ainda indefinido"
        }
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/CalculateConsumptionMetricsUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.CategoryMetrics
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt

class CalculateConsumptionMetricsUseCase {

    operator fun invoke(receipts: List<Receipt>): ConsumptionMetrics {
        if (receipts.isEmpty()) {
            return ConsumptionMetrics(
                valuePercentageByCategory = emptyCategoryMap(),
                itemPercentageByCategory = emptyCategoryMap(),
                frequencyByCategory = emptyCategoryMap(),
                categoryMetrics = emptyCategoryMetricsMap(),
                categoryValueTotals = emptyDoubleMap(),
                categoryItemTotals = emptyIntMap(),
                totalReceipts = 0,
                totalItems = 0,
                totalValue = 0.0,
                averageTicket = 0.0,
                averageItemsPerReceipt = 0.0,
                maxCategoryByValue = null,
                maxCategoryByItems = null,
                receiptAverageValueByCategory = emptyCategoryMap(),
                categoryConcentrationIndex = 0.0,
                topThreeCategoriesByValue = emptyList(),
                averageValuePerItem = 0.0,
                highestReceiptValue = 0.0,
                lowestReceiptValue = 0.0,
                receiptValueAmplitude = 0.0,
                highValueReceiptsPercentage = 0.0,
                lowValueReceiptsPercentage = 0.0,
                categoryDominanceGap = 0.0,
                topThreeCategoriesValuePercentage = 0.0,
                otherPercentageByValue = 0.0,
                otherPercentageByItems = 0.0,
                classifiedItemsPercentage = 0.0,
                averageCategoriesPerReceipt = 0.0,
                categoryDiversityIndex = 0.0,
                essentialCategoriesPercentage = 0.0,
                nonEssentialCategoriesPercentage = 0.0,
                industrializedToBasicFoodRatio = 0.0,
                beveragesToBasicFoodRatio = 0.0,
                beveragesToTotalRatio = 0.0,
                produceToTotalRatio = 0.0,
                receiptsWithIndustrializedPercentage = 0.0,
                receiptsWithBeveragesPercentage = 0.0,
                receiptsWithBasicFoodPercentage = 0.0,
                receiptsWithProducePercentage = 0.0,
                receiptsWithHygienePercentage = 0.0,
                receiptsWithCleaningPercentage = 0.0,
                averageIndustrializedItemsPerReceipt = 0.0,
                averageBeveragesItemsPerReceipt = 0.0,
                averageBasicFoodItemsPerReceipt = 0.0,
                averageProduceItemsPerReceipt = 0.0,
                convenienceScore = 0.0,
                essentialScore = 0.0,
                diversityScore = 0.0
            )
        }

        val allItems = receipts.flatMap { it.items }
        val totalValue = receipts.sumOf { it.totalValue }
        val totalItems = allItems.size
        val totalReceipts = receipts.size

        val categoryValueTotals = ProductCategory.entries.associateWith { category ->
            allItems.filter { it.category == category }.sumOf { it.price }
        }

        val categoryItemTotals = ProductCategory.entries.associateWith { category ->
            allItems.count { it.category == category }
        }

        val valuePercentageByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryValueTotals.valueOf(category), totalValue)
        }

        val itemPercentageByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryItemTotals.countOf(category).toDouble(), totalItems.toDouble())
        }

        val frequencyByCategory = ProductCategory.entries.associateWith { category ->
            val receiptsWithCategory = receipts.count { receipt -> receipt.items.any { it.category == category } }
            safeDivide(receiptsWithCategory.toDouble(), totalReceipts.toDouble())
        }

        val categoryMetrics = ProductCategory.entries.associateWith { category ->
            CategoryMetrics(
                category = category,
                totalValue = categoryValueTotals.valueOf(category),
                totalItems = categoryItemTotals.countOf(category),
                valuePercentage = valuePercentageByCategory.valueOf(category),
                itemPercentage = itemPercentageByCategory.valueOf(category),
                frequency = frequencyByCategory.valueOf(category),
                averageValuePerReceipt = safeDivide(categoryValueTotals.valueOf(category), totalReceipts.toDouble()),
                averageItemsPerReceipt = safeDivide(categoryItemTotals.countOf(category).toDouble(), totalReceipts.toDouble())
            )
        }

        val averageTicket = safeDivide(totalValue, totalReceipts.toDouble())
        val averageItemsPerReceipt = safeDivide(totalItems.toDouble(), totalReceipts.toDouble())
        val averageValuePerItem = safeDivide(totalValue, totalItems.toDouble())
        val receiptAverageValueByCategory = ProductCategory.entries.associateWith { category ->
            safeDivide(categoryValueTotals.valueOf(category), totalReceipts.toDouble())
        }
        val orderedValueCategories = orderedCategoriesByValue(valuePercentageByCategory)
        val maxCategoryByValue = orderedValueCategories.firstOrNull()?.takeIf { valuePercentageByCategory.valueOf(it) > 0.0 }
        val maxCategoryByItems = orderedCategoriesByItems(categoryItemTotals).firstOrNull()?.takeIf { categoryItemTotals.countOf(it) > 0 }
        val categoryConcentrationIndex = valuePercentageByCategory.values.maxOrNull() ?: 0.0
        val topThreeCategoriesByValue = orderedValueCategories.take(3)
        val topThreeCategoriesValuePercentage = orderedValueCategories.take(3).sumOf { valuePercentageByCategory.valueOf(it) }.coerceIn(0.0, 1.0)
        val categoryDominanceGap = if (orderedValueCategories.isNotEmpty()) {
            val first = valuePercentageByCategory.valueOf(orderedValueCategories.first())
            val second = orderedValueCategories.getOrNull(1)?.let { valuePercentageByCategory.valueOf(it) } ?: 0.0
            (first - second).coerceAtLeast(0.0)
        } else {
            0.0
        }
        val highestReceiptValue = receipts.maxOf { it.totalValue }
        val lowestReceiptValue = receipts.minOf { it.totalValue }
        val receiptValueAmplitude = highestReceiptValue - lowestReceiptValue
        val averageCategoriesPerReceipt = safeDivide(
            receipts.sumOf { receipt -> receipt.items.map { it.category }.distinct().size.toDouble() },
            totalReceipts.toDouble()
        )
        val categoryDiversityIndex = safeDivide(
            valuePercentageByCategory.count { it.value > 0.0 }.toDouble(),
            ProductCategory.entries.size.toDouble()
        )
        val essentialCategoriesPercentage = (
            valuePercentageByCategory.valueOf(ProductCategory.BASIC_FOOD) +
                valuePercentageByCategory.valueOf(ProductCategory.PRODUCE) +
                valuePercentageByCategory.valueOf(ProductCategory.HYGIENE) +
                valuePercentageByCategory.valueOf(ProductCategory.CLEANING)
            ).coerceIn(0.0, 1.0)
        val nonEssentialCategoriesPercentage = (
            valuePercentageByCategory.valueOf(ProductCategory.INDUSTRIALIZED) +
                valuePercentageByCategory.valueOf(ProductCategory.BEVERAGES) +
                valuePercentageByCategory.valueOf(ProductCategory.OTHER)
            ).coerceIn(0.0, 1.0)
        val industrializedValue = categoryValueTotals.valueOf(ProductCategory.INDUSTRIALIZED)
        val basicFoodValue = categoryValueTotals.valueOf(ProductCategory.BASIC_FOOD)
        val beveragesValue = categoryValueTotals.valueOf(ProductCategory.BEVERAGES)
        val produceValue = categoryValueTotals.valueOf(ProductCategory.PRODUCE)
        val industrializedToBasicFoodRatio = safeDivide(industrializedValue, basicFoodValue)
        val beveragesToBasicFoodRatio = safeDivide(beveragesValue, basicFoodValue)
        val beveragesToTotalRatio = valuePercentageByCategory.valueOf(ProductCategory.BEVERAGES)
        val produceToTotalRatio = valuePercentageByCategory.valueOf(ProductCategory.PRODUCE)
        val receiptsWithIndustrializedPercentage = frequencyByCategory.valueOf(ProductCategory.INDUSTRIALIZED)
        val receiptsWithBeveragesPercentage = frequencyByCategory.valueOf(ProductCategory.BEVERAGES)
        val receiptsWithBasicFoodPercentage = frequencyByCategory.valueOf(ProductCategory.BASIC_FOOD)
        val receiptsWithProducePercentage = frequencyByCategory.valueOf(ProductCategory.PRODUCE)
        val receiptsWithHygienePercentage = frequencyByCategory.valueOf(ProductCategory.HYGIENE)
        val receiptsWithCleaningPercentage = frequencyByCategory.valueOf(ProductCategory.CLEANING)
        val averageIndustrializedItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.INDUSTRIALIZED).toDouble(),
            totalReceipts.toDouble()
        )
        val averageBeveragesItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.BEVERAGES).toDouble(),
            totalReceipts.toDouble()
        )
        val averageBasicFoodItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.BASIC_FOOD).toDouble(),
            totalReceipts.toDouble()
        )
        val averageProduceItemsPerReceipt = safeDivide(
            categoryItemTotals.countOf(ProductCategory.PRODUCE).toDouble(),
            totalReceipts.toDouble()
        )
        val highValueReceiptsPercentage = safeDivide(
            receipts.count { it.totalValue > averageTicket }.toDouble(),
            totalReceipts.toDouble()
        )
        val lowValueReceiptsPercentage = safeDivide(
            receipts.count { it.totalValue < averageTicket }.toDouble(),
            totalReceipts.toDouble()
        )
        val classifiedItemsPercentage = (1.0 - (itemPercentageByCategory.valueOf(ProductCategory.OTHER))).coerceIn(0.0, 1.0)
        val otherPercentageByValue = valuePercentageByCategory.valueOf(ProductCategory.OTHER)
        val otherPercentageByItems = itemPercentageByCategory.valueOf(ProductCategory.OTHER)
        val convenienceScore = ((valuePercentageByCategory.valueOf(ProductCategory.INDUSTRIALIZED) + beveragesToTotalRatio + receiptsWithIndustrializedPercentage) / 3.0).coerceIn(0.0, 1.0)
        val essentialScore = ((essentialCategoriesPercentage + receiptsWithBasicFoodPercentage + receiptsWithProducePercentage) / 3.0).coerceIn(0.0, 1.0)
        val diversityScore = ((categoryDiversityIndex + safeDivide(averageCategoriesPerReceipt, ProductCategory.entries.size.toDouble())) / 2.0).coerceIn(0.0, 1.0)

        val metrics = ConsumptionMetrics(
            valuePercentageByCategory = valuePercentageByCategory,
            itemPercentageByCategory = itemPercentageByCategory,
            frequencyByCategory = frequencyByCategory,
            categoryMetrics = categoryMetrics,
            categoryValueTotals = categoryValueTotals,
            categoryItemTotals = categoryItemTotals,
            totalReceipts = totalReceipts,
            totalItems = totalItems,
            totalValue = totalValue,
            averageTicket = averageTicket,
            averageItemsPerReceipt = averageItemsPerReceipt,
            maxCategoryByValue = maxCategoryByValue,
            maxCategoryByItems = maxCategoryByItems,
            receiptAverageValueByCategory = receiptAverageValueByCategory,
            categoryConcentrationIndex = categoryConcentrationIndex,
            topThreeCategoriesByValue = topThreeCategoriesByValue,
            averageValuePerItem = averageValuePerItem,
            highestReceiptValue = highestReceiptValue,
            lowestReceiptValue = lowestReceiptValue,
            receiptValueAmplitude = receiptValueAmplitude,
            highValueReceiptsPercentage = highValueReceiptsPercentage,
            lowValueReceiptsPercentage = lowValueReceiptsPercentage,
            categoryDominanceGap = categoryDominanceGap,
            topThreeCategoriesValuePercentage = topThreeCategoriesValuePercentage,
            otherPercentageByValue = otherPercentageByValue,
            otherPercentageByItems = otherPercentageByItems,
            classifiedItemsPercentage = classifiedItemsPercentage,
            averageCategoriesPerReceipt = averageCategoriesPerReceipt,
            categoryDiversityIndex = categoryDiversityIndex,
            essentialCategoriesPercentage = essentialCategoriesPercentage,
            nonEssentialCategoriesPercentage = nonEssentialCategoriesPercentage,
            industrializedToBasicFoodRatio = industrializedToBasicFoodRatio,
            beveragesToBasicFoodRatio = beveragesToBasicFoodRatio,
            beveragesToTotalRatio = beveragesToTotalRatio,
            produceToTotalRatio = produceToTotalRatio,
            receiptsWithIndustrializedPercentage = receiptsWithIndustrializedPercentage,
            receiptsWithBeveragesPercentage = receiptsWithBeveragesPercentage,
            receiptsWithBasicFoodPercentage = receiptsWithBasicFoodPercentage,
            receiptsWithProducePercentage = receiptsWithProducePercentage,
            receiptsWithHygienePercentage = receiptsWithHygienePercentage,
            receiptsWithCleaningPercentage = receiptsWithCleaningPercentage,
            averageIndustrializedItemsPerReceipt = averageIndustrializedItemsPerReceipt,
            averageBeveragesItemsPerReceipt = averageBeveragesItemsPerReceipt,
            averageBasicFoodItemsPerReceipt = averageBasicFoodItemsPerReceipt,
            averageProduceItemsPerReceipt = averageProduceItemsPerReceipt,
            convenienceScore = convenienceScore,
            essentialScore = essentialScore,
            diversityScore = diversityScore
        )

        return metrics
    }

    private fun emptyCategoryMap(): Map<ProductCategory, Double> {
        return ProductCategory.entries.associateWith { 0.0 }
    }

    private fun emptyDoubleMap(): Map<ProductCategory, Double> = emptyCategoryMap()

    private fun emptyIntMap(): Map<ProductCategory, Int> {
        return ProductCategory.entries.associateWith { 0 }
    }

    private fun emptyCategoryMetricsMap(): Map<ProductCategory, CategoryMetrics> {
        return ProductCategory.entries.associateWith { category ->
            CategoryMetrics(
                category = category,
                totalValue = 0.0,
                totalItems = 0,
                valuePercentage = 0.0,
                itemPercentage = 0.0,
                frequency = 0.0,
                averageValuePerReceipt = 0.0,
                averageItemsPerReceipt = 0.0
            )
        }
    }

    private fun safeDivide(numerator: Double, denominator: Double): Double {
        return if (denominator == 0.0) 0.0 else numerator / denominator
    }

    private fun Map<ProductCategory, Double>.valueOf(category: ProductCategory): Double {
        return this[category] ?: 0.0
    }

    private fun Map<ProductCategory, Int>.countOf(category: ProductCategory): Int {
        return this[category] ?: 0
    }

    private fun orderedCategoriesByValue(values: Map<ProductCategory, Double>): List<ProductCategory> {
        return ProductCategory.entries
            .sortedWith(compareByDescending<ProductCategory> { values.valueOf(it) }.thenBy { it.ordinal })
            .filter { values.valueOf(it) > 0.0 }
    }

    private fun orderedCategoriesByItems(values: Map<ProductCategory, Int>): List<ProductCategory> {
        return ProductCategory.entries
            .sortedWith(compareByDescending<ProductCategory> { values.countOf(it) }.thenBy { it.ordinal })
            .filter { values.countOf(it) > 0 }
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/CalculateConsumptionMetricsV2UseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ProductSemanticTagger
import com.example.consumoai.domain.model.ConsumptionMetricsV2
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductSemanticTag
import com.example.consumoai.domain.model.Receipt
import java.text.Normalizer
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CalculateConsumptionMetricsV2UseCase(
    private val calculateConsumptionMetricsUseCase: CalculateConsumptionMetricsUseCase,
    private val semanticTagger: ProductSemanticTagger
) {

    operator fun invoke(receipts: List<Receipt>): ConsumptionMetricsV2 {
        val base = calculateConsumptionMetricsUseCase(receipts)
        if (receipts.isEmpty()) {
            return ConsumptionMetricsV2(
                baseMetrics = base,
                timeSpanDays = 0.0,
                receiptsPerWeek = 0.0,
                averageDaysBetweenReceipts = 0.0,
                purchaseRegularityScore = 0.0,
                ticketStandardDeviation = 0.0,
                ticketVariationCoefficient = 0.0,
                itemCountVariationCoefficient = 0.0,
                highTicketReceiptsPercentage = 0.0,
                lowTicketReceiptsPercentage = 0.0,
                categoryStabilityScore = 0.0,
                averageCategoryOverlapBetweenReceipts = 0.0,
                recurringItemRatio = 0.0,
                topItemRepetitionRate = 0.0,
                beverageSnackCoOccurrenceFrequency = 0.0,
                alcoholSnackCoOccurrenceFrequency = 0.0,
                hygieneCleaningCoOccurrenceFrequency = 0.0,
                basicProduceCoOccurrenceFrequency = 0.0,
                alcoholicBeverageValuePct = 0.0,
                alcoholicBeverageFrequency = 0.0,
                softDrinkValuePct = 0.0,
                softDrinkFrequency = 0.0,
                energyDrinkValuePct = 0.0,
                energyDrinkFrequency = 0.0,
                snackSweetValuePct = 0.0,
                snackSweetFrequency = 0.0,
                frozenConvenienceValuePct = 0.0,
                frozenConvenienceFrequency = 0.0,
                dairyValuePct = 0.0,
                meatProteinValuePct = 0.0,
                freshProduceValuePct = 0.0,
                convenienceMealValuePct = 0.0,
                convenienceMealFrequency = 0.0,
                essentialRoutineScore = 0.0,
                convenienceRoutineScore = 0.0,
                beverageRoutineScore = 0.0,
                householdRoutineScore = 0.0,
                freshFoodPresenceScore = 0.0
            )
        }

        val sortedByDate = receipts.sortedBy { it.date }
        val timeSpanDays = if (sortedByDate.size <= 1) 1.0 else {
            ChronoUnit.DAYS.between(sortedByDate.first().date, sortedByDate.last().date).toDouble().coerceAtLeast(1.0)
        }
        val receiptsPerWeek = receipts.size / (timeSpanDays / 7.0)
        val gaps = sortedByDate.zipWithNext { a, b -> ChronoUnit.DAYS.between(a.date, b.date).toDouble().coerceAtLeast(0.0) }
        val averageDaysBetweenReceipts = gaps.averageOrZero()
        val gapStdDev = standardDeviation(gaps)
        val purchaseRegularityScore = (1.0 - safeDivide(gapStdDev, averageDaysBetweenReceipts.coerceAtLeast(1.0))).coerceIn(0.0, 1.0)

        val tickets = receipts.map { it.totalValue }
        val ticketStandardDeviation = standardDeviation(tickets)
        val ticketVariationCoefficient = safeDivide(ticketStandardDeviation, tickets.averageOrZero().coerceAtLeast(1.0))

        val itemCounts = receipts.map { it.items.size.toDouble() }
        val itemCountVariationCoefficient = safeDivide(standardDeviation(itemCounts), itemCounts.averageOrZero().coerceAtLeast(1.0))

        val highTicketReceiptsPercentage = receipts.count { it.totalValue > base.averageTicket }.toDouble() / receipts.size
        val lowTicketReceiptsPercentage = receipts.count { it.totalValue < base.averageTicket }.toDouble() / receipts.size

        val categorySets = receipts.map { receipt -> receipt.items.map { it.category }.toSet() }
        val overlaps = categorySets.zipWithNext { a, b -> jaccard(a, b) }
        val averageCategoryOverlapBetweenReceipts = overlaps.averageOrZero()
        val categoryStabilityScore = averageCategoryOverlapBetweenReceipts

        val normalizedNames = receipts.flatMap { receipt -> receipt.items.map { normalizeProductNameForRecurrence(it.name) } }
            .filter { it.isNotBlank() }
        val countsByName = normalizedNames.groupingBy { it }.eachCount()
        val recurringDistinctNames = countsByName.count { it.value > 1 }
        val recurringItemRatio = safeDivide(recurringDistinctNames.toDouble(), countsByName.size.toDouble())
        val topItemRepetitionRate = safeDivide((countsByName.maxOfOrNull { it.value } ?: 0).toDouble(), receipts.size.toDouble())

        val receiptTagSets = receipts.map { receipt -> receipt.items.flatMap { semanticTagger.tagsFor(it) }.toSet() }

        val beverageSnackCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.SOFT_DRINK, ProductSemanticTag.SNACK_OR_SWEET)
        val alcoholSnackCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.ALCOHOLIC_BEVERAGE, ProductSemanticTag.SNACK_OR_SWEET)
        val hygieneCleaningCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.PERSONAL_CARE, ProductSemanticTag.HOUSEHOLD_CLEANING)
        val basicProduceCoOccurrenceFrequency = receipts.count { receipt ->
            val categories = receipt.items.map { it.category }.toSet()
            categories.contains(ProductCategory.BASIC_FOOD) && categories.contains(ProductCategory.PRODUCE)
        }.toDouble() / receipts.size

        val totalValue = receipts.sumOf { it.totalValue }.coerceAtLeast(0.00001)

        fun valuePctByTag(tag: ProductSemanticTag): Double {
            val value = receipts.flatMap { it.items }
                .filter { semanticTagger.tagsFor(it).contains(tag) }
                .sumOf { it.price }
            return safeDivide(value, totalValue)
        }

        fun freqByTag(tag: ProductSemanticTag): Double {
            val withTag = receipts.count { receipt -> receipt.items.any { semanticTagger.tagsFor(it).contains(tag) } }
            return safeDivide(withTag.toDouble(), receipts.size.toDouble())
        }

        val alcoholicBeverageValuePct = valuePctByTag(ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        val alcoholicBeverageFrequency = freqByTag(ProductSemanticTag.ALCOHOLIC_BEVERAGE)
        val softDrinkValuePct = valuePctByTag(ProductSemanticTag.SOFT_DRINK)
        val softDrinkFrequency = freqByTag(ProductSemanticTag.SOFT_DRINK)
        val energyDrinkValuePct = valuePctByTag(ProductSemanticTag.ENERGY_DRINK)
        val energyDrinkFrequency = freqByTag(ProductSemanticTag.ENERGY_DRINK)
        val snackSweetValuePct = valuePctByTag(ProductSemanticTag.SNACK_OR_SWEET)
        val snackSweetFrequency = freqByTag(ProductSemanticTag.SNACK_OR_SWEET)
        val frozenConvenienceValuePct = valuePctByTag(ProductSemanticTag.FROZEN_OR_READY_MEAL)
        val frozenConvenienceFrequency = freqByTag(ProductSemanticTag.FROZEN_OR_READY_MEAL)
        val dairyValuePct = valuePctByTag(ProductSemanticTag.DAIRY)
        val meatProteinValuePct = valuePctByTag(ProductSemanticTag.MEAT_OR_PROTEIN)
        val freshProduceValuePct = valuePctByTag(ProductSemanticTag.FRESH_PRODUCE)

        val convenienceMealValuePct = ((frozenConvenienceValuePct + snackSweetValuePct) / 2.0).coerceIn(0.0, 1.0)
        val convenienceMealFrequency = ((frozenConvenienceFrequency + snackSweetFrequency) / 2.0).coerceIn(0.0, 1.0)

        val essentialRoutineScore = averageOf(
            base.frequencyByCategory[ProductCategory.BASIC_FOOD] ?: 0.0,
            base.frequencyByCategory[ProductCategory.PRODUCE] ?: 0.0,
            base.essentialCategoriesPercentage,
            basicProduceCoOccurrenceFrequency
        )

        val convenienceRoutineScore = averageOf(
            base.frequencyByCategory[ProductCategory.INDUSTRIALIZED] ?: 0.0,
            snackSweetFrequency,
            frozenConvenienceFrequency,
            convenienceMealValuePct
        )

        val beverageRoutineScore = averageOf(
            base.frequencyByCategory[ProductCategory.BEVERAGES] ?: 0.0,
            base.valuePercentageByCategory[ProductCategory.BEVERAGES] ?: 0.0,
            alcoholicBeverageFrequency,
            softDrinkFrequency
        )

        val householdRoutineScore = averageOf(
            base.frequencyByCategory[ProductCategory.HYGIENE] ?: 0.0,
            base.frequencyByCategory[ProductCategory.CLEANING] ?: 0.0,
            hygieneCleaningCoOccurrenceFrequency
        )

        val freshFoodPresenceScore = averageOf(
            base.valuePercentageByCategory[ProductCategory.PRODUCE] ?: 0.0,
            base.frequencyByCategory[ProductCategory.PRODUCE] ?: 0.0,
            freshProduceValuePct,
            basicProduceCoOccurrenceFrequency
        )

        return ConsumptionMetricsV2(
            baseMetrics = base,
            timeSpanDays = timeSpanDays,
            receiptsPerWeek = receiptsPerWeek,
            averageDaysBetweenReceipts = averageDaysBetweenReceipts,
            purchaseRegularityScore = purchaseRegularityScore,
            ticketStandardDeviation = ticketStandardDeviation,
            ticketVariationCoefficient = ticketVariationCoefficient,
            itemCountVariationCoefficient = itemCountVariationCoefficient,
            highTicketReceiptsPercentage = highTicketReceiptsPercentage,
            lowTicketReceiptsPercentage = lowTicketReceiptsPercentage,
            categoryStabilityScore = categoryStabilityScore,
            averageCategoryOverlapBetweenReceipts = averageCategoryOverlapBetweenReceipts,
            recurringItemRatio = recurringItemRatio,
            topItemRepetitionRate = topItemRepetitionRate,
            beverageSnackCoOccurrenceFrequency = beverageSnackCoOccurrenceFrequency,
            alcoholSnackCoOccurrenceFrequency = alcoholSnackCoOccurrenceFrequency,
            hygieneCleaningCoOccurrenceFrequency = hygieneCleaningCoOccurrenceFrequency,
            basicProduceCoOccurrenceFrequency = basicProduceCoOccurrenceFrequency,
            alcoholicBeverageValuePct = alcoholicBeverageValuePct,
            alcoholicBeverageFrequency = alcoholicBeverageFrequency,
            softDrinkValuePct = softDrinkValuePct,
            softDrinkFrequency = softDrinkFrequency,
            energyDrinkValuePct = energyDrinkValuePct,
            energyDrinkFrequency = energyDrinkFrequency,
            snackSweetValuePct = snackSweetValuePct,
            snackSweetFrequency = snackSweetFrequency,
            frozenConvenienceValuePct = frozenConvenienceValuePct,
            frozenConvenienceFrequency = frozenConvenienceFrequency,
            dairyValuePct = dairyValuePct,
            meatProteinValuePct = meatProteinValuePct,
            freshProduceValuePct = freshProduceValuePct,
            convenienceMealValuePct = convenienceMealValuePct,
            convenienceMealFrequency = convenienceMealFrequency,
            essentialRoutineScore = essentialRoutineScore,
            convenienceRoutineScore = convenienceRoutineScore,
            beverageRoutineScore = beverageRoutineScore,
            householdRoutineScore = householdRoutineScore,
            freshFoodPresenceScore = freshFoodPresenceScore
        )
    }

    private fun normalizeProductNameForRecurrence(name: String): String {
        val upper = name.uppercase(Locale.ROOT)
        val noAccents = Normalizer.normalize(upper, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
        val noMeasures = noAccents
            .replace(Regex("\\b\\d+[.,]?\\d*\\s?(ML|L|G|KG|UN|UNS)\\b"), " ")
            .replace(Regex("[^A-Z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        return noMeasures
    }

    private fun coOccurrence(receiptTagSets: List<Set<ProductSemanticTag>>, first: ProductSemanticTag, second: ProductSemanticTag): Double {
        if (receiptTagSets.isEmpty()) return 0.0
        val withBoth = receiptTagSets.count { it.contains(first) && it.contains(second) }
        return safeDivide(withBoth.toDouble(), receiptTagSets.size.toDouble())
    }

    private fun jaccard(a: Set<ProductCategory>, b: Set<ProductCategory>): Double {
        val union = (a + b).size.toDouble()
        if (union == 0.0) return 0.0
        val intersection = a.intersect(b).size.toDouble()
        return safeDivide(intersection, union)
    }

    private fun standardDeviation(values: List<Double>): Double {
        if (values.isEmpty()) return 0.0
        val mean = values.average()
        val variance = values.sumOf { (it - mean).pow(2) } / values.size
        return sqrt(abs(variance))
    }

    private fun safeDivide(numerator: Double, denominator: Double): Double {
        if (denominator == 0.0 || denominator.isNaN() || denominator.isInfinite()) return 0.0
        val result = numerator / denominator
        return if (result.isNaN() || result.isInfinite()) 0.0 else result
    }

    private fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()

    private fun averageOf(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        return values.map { if (it.isFinite()) it else 0.0 }.average().coerceIn(0.0, 1.0)
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ClassifyConsumptionProfileUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Encapsula a classificaÃ§Ã£o de perfil usando o backend treinado,
 * com fallback local apenas em falhas tÃ©cnicas.
 */
class ClassifyConsumptionProfileUseCase(
    private val consumptionBehaviorClassifier: ConsumptionBehaviorClassifier
) {

    suspend operator fun invoke(modelInput: ConsumptionModelInput): ConsumptionBehaviorResult {
        return consumptionBehaviorClassifier.classify(modelInput)
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ClassifyProductsUseCase.kt

```kotlin
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

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ClearReceiptsUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.repository.ReceiptRepository

class ClearReceiptsUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke() {
        receiptRepository.clearReceipts()
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ConsumptionFeatureSanitizer.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FeatureSanitizationNote
import com.example.consumoai.domain.model.SanitizedConsumptionModelInput

class ConsumptionFeatureSanitizer {

    operator fun invoke(input: ConsumptionModelInput): SanitizedConsumptionModelInput {
        val notes = mutableListOf<FeatureSanitizationNote>()
        val sanitizedFeatures = input.features.mapValues { (name, value) ->
            sanitizeFeature(name, value, notes)
        }

        return SanitizedConsumptionModelInput(
            input = input.copy(features = sanitizedFeatures),
            notes = notes
        )
    }

    private fun sanitizeFeature(
        featureName: String,
        value: Double,
        notes: MutableList<FeatureSanitizationNote>
    ): Double {
        val sanitized = when {
            value.isNaN() || value.isInfinite() -> 0.0
            isClampedZeroToOneFeature(featureName) -> value.coerceIn(0.0, 1.0)
            isNonNegativeFeature(featureName) -> value.coerceAtLeast(0.0)
            else -> value
        }

        if (sanitized != value) {
            notes += FeatureSanitizationNote(
                featureName = featureName,
                originalValue = value,
                sanitizedValue = sanitized,
                reason = buildReason(featureName, value, sanitized)
            )
        }

        return sanitized
    }

    private fun isClampedZeroToOneFeature(featureName: String): Boolean {
        return featureName.endsWith("_pct") ||
            featureName.endsWith("_percentage") ||
            featureName.endsWith("_frequency") ||
            featureName.endsWith("_index") ||
            featureName.endsWith("_score") ||
            featureName.endsWith("_ratio") ||
            featureName == "classified_items_percentage"
    }

    private fun isNonNegativeFeature(featureName: String): Boolean {
        return featureName.startsWith("total_") ||
            featureName.startsWith("average_") ||
            featureName.contains("value") ||
            featureName.contains("items")
    }

    private fun buildReason(featureName: String, originalValue: Double, sanitizedValue: Double): String {
        return when {
            originalValue.isNaN() -> "Valor NaN substituÃ­do por 0.0"
            originalValue.isInfinite() -> "Valor infinito substituÃ­do por 0.0"
            isClampedZeroToOneFeature(featureName) && originalValue < 0.0 -> "Feature limitada ao intervalo 0..1"
            isClampedZeroToOneFeature(featureName) && originalValue > 1.0 -> "Feature limitada ao intervalo 0..1"
            sanitizedValue == 0.0 && originalValue < 0.0 -> "Valor negativo invÃ¡lido ajustado para 0.0"
            else -> "Valor sanitizado para manter consistÃªncia do modelo"
        }
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/GetStoredReceiptsSummaryUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.domain.repository.ReceiptRepository

class GetStoredReceiptsSummaryUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke(): StoredReceiptsSummary {
        val receipts = receiptRepository.getAllReceipts()
        return StoredReceiptsSummary(
            totalReceipts = receipts.size,
            totalItems = receipts.sumOf { it.items.size },
            totalValue = receipts.sumOf { it.totalValue }
        )
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ImportSampleNfceReceiptsUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.repository.ReceiptRepository

class ImportSampleNfceReceiptsUseCase(
    private val analyzeReceiptFromQrCodeUrlUseCase: AnalyzeReceiptFromQrCodeUrlUseCase,
    private val saveReceiptUseCase: SaveReceiptUseCase,
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke(): ImportReceiptsResult {
        var importedCount = 0
        var skippedCount = 0
        var failedCount = 0

        sampleNfceUrls.forEach { url ->
            if (receiptRepository.existsByAccessKeyOrUrl(url)) {
                skippedCount += 1
                return@forEach
            }

            val receipt = runCatching {
                analyzeReceiptFromQrCodeUrlUseCase(url)
            }.getOrElse {
                failedCount += 1
                return@forEach
            }

            if (receipt.items.isEmpty()) {
                failedCount += 1
                return@forEach
            }

            saveReceiptUseCase(receipt)
            importedCount += 1
        }

        return ImportReceiptsResult(
            importedCount = importedCount,
            skippedCount = skippedCount,
            failedCount = failedCount
        )
    }

    companion object {
        val sampleNfceUrls = listOf(
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43260593015006003058651220007718841723961687%7C2%7C1%7C1%7C346E1BEACD34130C83316F50ADC2B99CF87234B5",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43260193015006003058651250002348791908618822%7C2%7C1%7C1%7C6700EF69E0C329B7C4B1635207B8DE60FBFE7304",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651100007585681908712507%7C2%7C1%7C1%7C3F121EB9CD436647D37A278254D436AE0A4AF434",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43251093015006000970651060016444861954157476%7C2%7C1%7C1%7C576A715FC4E818C83F0E63BA50AC3A0F7DDE6187",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651110004729671059269372%7C2%7C1%7C1%7C8EA38B5FD4177E60DF007DE86BE122680A4A19A4",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651110004734691015947881%7C2%7C1%7C1%7CC0CACBD27019F6EAD3979E599A3FFFE417813C02",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651100007554131591952030%7C2%7C1%7C1%7C69824D7F84E9E54DD11277F513B853241BA6C691",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651130002226791920952098%7C2%7C1%7C1%7C8691A8AD876EC8861C3F8AC1D546A5D5A4F2D3AB",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43250993015006000890651090007110791850810422%7C2%7C1%7C1%7CF122CB142998F8F1C9891025165339F2F5757225",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43251093015006000890651100007626041550452981%7C2%7C1%7C1%7C8FA68A81F0078C53F44444C8744E663DF5F90EB3",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43260593015006000890651130002606181030121564%7C2%7C1%7C1%7CC3B751B5B340C0112CC46A4C34FBF55A53C6E24E",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260593015006000890651030008658621784452963%7C2%7C1%7C1%7C02D5E125461C5871D667E9DD4534E3A1C4F2235C",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260593015006000890651030008656571286908077%7C2%7C1%7C1%7CC2FFA17A9AFB527A307B47347931A96618F761BC",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260593015006000890651120004331571588542386%7C2%7C1%7C1%7CA6F29ECBB977366AA30D8B3412E5D3D00BDBE438",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260593015006000890651140002243891524771522%7C2%7C1%7C1%7CD9BECF0DC883312BC4EE81F306E638EADA72EB67",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651110005065661339361376%7C2%7C1%7C1%7CFF3FAE5434DBA6C2063809897A113837E03CF738",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651020008833931118068707%7C2%7C1%7C1%7CB96669FC6B7665085B805D54D2E146596C1EE154",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651120004299671201219965%7C2%7C1%7C1%7CB5445B0708B63E2641727B8E23B90E172D5870F4",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651090007392011267905116%7C2%7C1%7C1%7CB5353A982C00D0F8C60C2BA510155AE74A318AB3",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651030008626931835113794%7C2%7C1%7C1%7CC9F3F108C99DEBFB80BEDDD8E1CAE7204394821C",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651130002570861493098887%7C2%7C1%7C1%7C3B24698BE95BB2674B62CB27C535B41B4CB2F605",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651100008237991892618994%7C2%7C1%7C1%7C1CFF9CD4251FC7EB4E8599027D7D710A54F6CF81",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651130002561491858615395%7C2%7C1%7C1%7CB7392D437D333ABDF4910C4237D1E09CBC9711D8",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651120004255101646290973%7C2%7C1%7C1%7CFBC7982F735600903D580CACBF8A8C0A2837C27C",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651030008603141212965654%7C2%7C1%7C1%7C1FDEA44BB00FDBA78D8AC397245A37FF10B18905",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651120004233891372002470%7C2%7C1%7C1%7C7BCE0F09132B5C8A9AD8F9B5FCCA3666EFFEA56A",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651030008595631364106460%7C2%7C1%7C1%7CB3697D5820F0C935FA27AF0A595C28B555A56B44",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651020008809731443239856%7C2%7C1%7C1%7C734B05699F45AC642F5CCC30E1EC2987C75D3E2B",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260493015006000890651110005029951374512526%7C2%7C1%7C1%7C9BA73E75B38DD4CFEE1458124AC31DE992FF2092",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260393015006000890651110005023901579048250%7C2%7C1%7C1%7CBC5D7138F1D55D8BD936B8A6CEF7A118D79216C8",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260393015006000890651120004167121149677848%7C2%7C1%7C1%7CA83C2141F2F7D1E099D5C2B34DEC315516E1B810",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260393015006000890651020008795941498702312%7C2%7C1%7C1%7C4C72C61C98F47837A6DB3CD984FD75398BFDB451",
            "https://www.sefaz.rs.gov.br/NFCE/NFCE-COM.aspx?p=43260393015006000890651110005018161643159342%7C2%7C1%7C1%7C924B37788E3BE7E4E83B6ECD5A870AAC4E7DF88F",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43260593015006003058651040004728211381220598%7C2%7C1%7C1%7C8079CE19E99A586900DCEAB7501398CBDA402957",
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43260593015006003058651180008733551869610299%7C2%7C1%7C1%7C6ED3EBF2917B1C8481FDCA5DD05BE45F5FF64900"
        )
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/SaveReceiptUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.repository.ReceiptRepository

class SaveReceiptUseCase(
    private val receiptRepository: ReceiptRepository
) {

    suspend operator fun invoke(receipt: Receipt) {
        receiptRepository.saveReceipt(receipt)
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/MainActivity.kt

```kotlin
package com.example.consumoai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.consumoai.presentation.home.HomeRoute
import com.example.consumoai.ui.theme.ConsumoAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConsumoAITheme {
                HomeRoute()
            }
        }
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeRoute.kt

```kotlin
package com.example.consumoai.presentation.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenV2(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}
```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeScreenAction.kt

```kotlin
package com.example.consumoai.presentation.home

sealed interface HomeScreenAction {
    data object OnImportSampleNfceUrlsClick : HomeScreenAction
    data object OnAnalyzeStoredReceiptsClick : HomeScreenAction
    data object OnClearReceiptsClick : HomeScreenAction
}


```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeScreenV2.kt

```kotlin
package com.example.consumoai.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.presentation.home.model.HomeAnalysisPresentation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenV2(
    uiState: HomeUiState,
    onAction: (HomeScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "ConsumoAI",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Modelo XGBoost V2 Top 15",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AÃ§Ãµes principais
            SectionTitle("AÃ§Ãµes principais")
            ActionButtonsV2(
                isImporting = uiState.isImporting,
                isAnalyzing = uiState.isAnalyzing,
                onAction = onAction
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Card de resumo local
            StoredReceiptsCardV2(
                summary = uiState.localSummary,
                importResult = uiState.importResult
            )

            // AnÃ¡lise principal (se disponÃ­vel)
            uiState.analysisPresentation?.let { presentation ->
                Spacer(modifier = Modifier.height(8.dp))
                SectionTitle("AnÃ¡lise")

                // Card principal: Perfil identificado
                ProfileResultCardV2(presentation)

                // Card: Leitura do consumo
                ConsumptionReadingCardV2(presentation.consumptionReading)

                // Card: Principais sinais
                PrimarySignalsCardV2(presentation.primarySignals)

                // Card recolhÃ­vel: Detalhes tÃ©cnicos
                TechnicalDetailsCardV2(presentation.technicalItems)
            }

            // Estados: loading ou erro
            when {
                uiState.isImporting || uiState.isAnalyzing -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(32.dp)
                    )
                }

                uiState.errorMessage != null -> {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Text(
                            text = uiState.errorMessage,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ActionButtonsV2(
    isImporting: Boolean,
    isAnalyzing: Boolean,
    onAction: (HomeScreenAction) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { onAction(HomeScreenAction.OnImportSampleNfceUrlsClick) },
            enabled = !isImporting && !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = if (isImporting) "Importando..." else "Importar notas NFC-e")
        }

        Button(
            onClick = { onAction(HomeScreenAction.OnAnalyzeStoredReceiptsClick) },
            enabled = !isAnalyzing && !isImporting,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = if (isAnalyzing) "Analisando..." else "Analisar consumo")
        }

        Button(
            onClick = { onAction(HomeScreenAction.OnClearReceiptsClick) },
            enabled = !isImporting && !isAnalyzing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Limpar dados locais")
        }
    }
}

@Composable
private fun StoredReceiptsCardV2(
    summary: StoredReceiptsSummary?,
    importResult: ImportReceiptsResult?
) {
    if (summary == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Dados locais armazenados", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricBadge("Notas", summary.totalReceipts.toString())
                MetricBadge("Itens", summary.totalItems.toString())
                MetricBadge("Valor total", summary.totalValue.toCurrencyText())
            }

            if (importResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ãšltima importaÃ§Ã£o: ${importResult.importedCount} novas, ${importResult.skippedCount} duplicadas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricBadge(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.padding(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun ProfileResultCardV2(presentation: HomeAnalysisPresentation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Perfil identificado",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                presentation.profileTitle.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                presentation.profileDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "ConfianÃ§a",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        presentation.confidenceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column {
                    Text(
                        "Origem",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        presentation.sourceLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsumptionReadingCardV2(reading: String) {
    if (reading.isBlank()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Leitura do consumo",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                reading,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight.times(1.5f),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PrimarySignalsCardV2(signals: List<String>) {
    if (signals.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Principais sinais",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            signals.forEach { signal ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(end = 12.dp),
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Text(
                            "â€¢",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        signal,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicalDetailsCardV2(items: List<Pair<String, String>>) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Detalhes tÃ©cnicos",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Text(
                        if (expanded) "âˆ’" else "+",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                items.forEach { (label, value) ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            value,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Double.toCurrencyText(): String = "R$ ${"%.2f".format(java.util.Locale.US, this).replace('.', ',')}"

```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeUiState.kt

```kotlin
package com.example.consumoai.presentation.home

import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.presentation.home.model.HomeAnalysisPresentation

data class HomeUiState(
    val isImporting: Boolean = false,
    val isAnalyzing: Boolean = false,
    val importResult: ImportReceiptsResult? = null,
    val localSummary: StoredReceiptsSummary? = null,
    val storedAnalysis: StoredConsumptionAnalysis? = null,
    val analysisPresentation: HomeAnalysisPresentation? = null,
    val errorMessage: String? = null
)
```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeViewModel.kt

```kotlin
package com.example.consumoai.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.consumoai.domain.usecase.AnalyzeStoredReceiptsUseCase
import com.example.consumoai.domain.usecase.ClearReceiptsUseCase
import com.example.consumoai.domain.usecase.GetStoredReceiptsSummaryUseCase
import com.example.consumoai.domain.usecase.ImportSampleNfceReceiptsUseCase
import com.example.consumoai.presentation.home.model.toHomeAnalysisPresentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val importSampleNfceReceiptsUseCase: ImportSampleNfceReceiptsUseCase,
    private val analyzeStoredReceiptsUseCase: AnalyzeStoredReceiptsUseCase,
    private val getStoredReceiptsSummaryUseCase: GetStoredReceiptsSummaryUseCase,
    private val clearReceiptsUseCase: ClearReceiptsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onAction(action: HomeScreenAction) {
        when (action) {
            HomeScreenAction.OnImportSampleNfceUrlsClick -> importSampleReceipts()
            HomeScreenAction.OnAnalyzeStoredReceiptsClick -> analyzeStoredReceipts()
            HomeScreenAction.OnClearReceiptsClick -> clearReceipts()
        }
    }

    private fun importSampleReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, errorMessage = null)

            runCatching {
                // Run IO operations on Dispatchers.IO
                val result = withContext(Dispatchers.IO) {
                    importSampleNfceReceiptsUseCase()
                }
                val summary = withContext(Dispatchers.IO) {
                    getStoredReceiptsSummaryUseCase()
                }
                result to summary
            }.onSuccess { (result, summary) ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result,
                    localSummary = summary,
                    storedAnalysis = null,
                    analysisPresentation = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    errorMessage = error.message ?: "Erro ao importar notas NFC-e"
                )
            }
        }
    }

    private fun analyzeStoredReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)

            runCatching {
                // Run heavy computation on Dispatchers.Default
                withContext(Dispatchers.Default) {
                    analyzeStoredReceiptsUseCase()
                }
            }.onSuccess { analysis ->
                val summary = withContext(Dispatchers.IO) {
                    getStoredReceiptsSummaryUseCase()
                }
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    localSummary = summary,
                    storedAnalysis = analysis,
                    analysisPresentation = analysis.toHomeAnalysisPresentation()
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = error.message ?: "Erro ao analisar notas armazenadas"
                )
            }
        }
    }


    private fun clearReceipts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                isAnalyzing = true,
                errorMessage = null
            )

            runCatching {
                withContext(Dispatchers.IO) {
                    clearReceiptsUseCase()
                }
            }.onSuccess {
                _uiState.value = HomeUiState()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    isAnalyzing = false,
                    errorMessage = error.message ?: "Erro ao limpar notas locais"
                )
            }
        }
    }

}

```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/model/HomeAnalysisPresentation.kt

```kotlin
package com.example.consumoai.presentation.home.model

data class HomeAnalysisPresentation(
    val profileTitle: String,
    val profileDescription: String,
    val consumptionReading: String,
    val confidenceLabel: String,
    val sourceLabel: String,
    val sourceWarning: String?,
    val primarySignals: List<String>,
    val technicalItems: List<Pair<String, String>>
)

```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/model/HomeAnalysisPresentationMapper.kt

```kotlin
package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.FallbackReason
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_V2_INTERNAL_METRICS_COUNT
import com.example.consumoai.domain.model.ProfileInterpretationType
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import java.util.Locale

fun StoredConsumptionAnalysis.toHomeAnalysisPresentation(): HomeAnalysisPresentation {
	val selectedInput = modelInput
	val baseMetrics = metricsV2.baseMetrics
	val summary = behaviorResult.profileSummary

	val technical = mutableListOf<Pair<String, String>>()
	technical += "Modelo" to "XGBoost V2 Top 15"
	technical += "Backend" to (behaviorResult.backendModelUsed ?: "v2")
	technical += "Features enviadas ao modelo" to "${MODEL_V2_FINAL_FEATURE_COUNT} (de ${MODEL_V2_INTERNAL_METRICS_COUNT} mÃ©tricas internas)"
	technical += "VersÃ£o de entrada" to (behaviorResult.requestedInputVersion ?: selectedInput.version)
	technical += "Fonte da classificaÃ§Ã£o" to behaviorResult.source.toTechnicalLabel()
	technical += "ConfianÃ§a" to behaviorResult.confidence.toPercentageText()
	technical += "Tipo de interpretaÃ§Ã£o" to (summary?.interpretationType?.name ?: ProfileInterpretationType.PURE_PROFILE.name)
	technical += "Itens classificados" to baseMetrics.classifiedItemsPercentage.toPercentageText()
	technical += "OTHER por valor" to baseMetrics.otherPercentageByValue.toPercentageText()
	technical += "InferÃªncia (ms)" to behaviorResult.inferenceDurationMs.toString()
	technical += "Input sanitizado" to if (behaviorResult.usedSanitizedInput) "Sim (${behaviorResult.sanitizationNotes.size} ajustes)" else "NÃ£o"
	behaviorResult.fallbackReason?.let { technical += "Motivo do fallback" to it.name }
	technical += "MÃ©tricas internas" to "${MODEL_V2_INTERNAL_METRICS_COUNT} calculadas"
	technical += "Notas analisadas" to baseMetrics.totalReceipts.toString()
	technical += "Itens analisados" to baseMetrics.totalItems.toString()
	if (baseMetrics.classifiedItemsPercentage < 0.70) {
		technical += "Aviso tÃ©cnico" to "HÃ¡ muitos itens nÃ£o classificados. Isso pode reduzir a confiabilidade da anÃ¡lise."
	}

	selectedInput.features.toSortedMap().forEach { (name, value) ->
		technical += "Feature $name" to value.toNumberText()
	}

	behaviorResult.profileScores
		.toList()
		.sortedByDescending { (_, score) -> score }
		.take(3)
		.forEach { (profile, score) ->
			technical += "Probabilidade ${profile.toDisplayName()}" to score.toPercentageText()
		}

	return HomeAnalysisPresentation(
		profileTitle = summary.toPresentationTitle(behaviorResult.mainProfile),
		profileDescription = behaviorResult.mainProfile.toDescription(),
		consumptionReading = buildBehavioralReading(this),
		confidenceLabel = behaviorResult.confidence.toConfidenceLabel(),
		sourceLabel = behaviorResult.source.toDisplayLabel(),
		sourceWarning = behaviorResult.source.toWarningMessage(behaviorResult.fallbackReason),
		primarySignals = buildPrimarySignals(this),
		technicalItems = technical
	)
}

private fun buildPrimarySignals(analysis: StoredConsumptionAnalysis): List<String> {
	val base = analysis.metricsV2.baseMetrics
	val v2 = analysis.metricsV2

	return buildList {
		add("Bebidas presentes em ${base.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()} das notas")
		add("AlimentaÃ§Ã£o bÃ¡sica representa ${base.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero().toPercentageText()} do valor")
		add("Diversidade alta entre categorias")
		add("RecorrÃªncia de itens em ${v2.recurringItemRatio.toPercentageText()} das compras")
		add("Bebidas + snacks em ${v2.beverageSnackCoOccurrenceFrequency.toPercentageText()} das notas")
	}.take(5)
}

private fun buildBehavioralReading(analysis: StoredConsumptionAnalysis): String {
	val base = analysis.metricsV2.baseMetrics
	val beverageFrequency = base.frequencyByCategory[ProductCategory.BEVERAGES].orZero().toPercentageText()
	val essentialValue = base.valuePercentageByCategory[ProductCategory.BASIC_FOOD].orZero().toPercentageText()
	val cooccurrence = analysis.metricsV2.beverageSnackCoOccurrenceFrequency.toPercentageText()

	return buildString {
		append("As compras analisadas mostram um padrÃ£o variado, com presenÃ§a frequente de bebidas")
		append(" (")
		append(beverageFrequency)
		append(" das notas)")
		append(" e combinaÃ§Ã£o com snacks em ")
		append(cooccurrence)
		append('.')
		append("\n\n")
		append("Apesar disso, alimentaÃ§Ã£o bÃ¡sica continua relevante em valor (")
		append(essentialValue)
		append("), indicando que o consumo nÃ£o estÃ¡ concentrado apenas em conveniÃªncia. ")
		append("A diversidade entre categorias sugere uma rotina relativamente equilibrada, com presenÃ§a complementar de itens domÃ©sticos e higiene.")
	}
}

private fun com.example.consumoai.domain.model.ConsumptionProfileSummary?.toPresentationTitle(
	defaultProfile: ConsumptionBehaviorProfile
): String {
	val summary = this ?: return defaultProfile.toDisplayName()
	return when (summary.interpretationType) {
		ProfileInterpretationType.PURE_PROFILE -> summary.primaryProfile.toDisplayName()
		ProfileInterpretationType.HYBRID_PROFILE -> "Perfil hÃ­brido: ${summary.primaryProfile.toDisplayName()}"
		ProfileInterpretationType.LOW_CONFIDENCE_PROFILE -> "Baixa confianÃ§a: ${summary.primaryProfile.toDisplayName()}"
	}
}

fun ConsumptionBehaviorProfile.toDisplayName(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Orientado Ã  conveniÃªncia"
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Focado no essencial"
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Diversificado e equilibrado"
		ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Recorrente em bebidas"
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa presenÃ§a de hortifruti"
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Foco em manutenÃ§Ã£o domÃ©stica"
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Consumo concentrado"
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Consumo impulsivo"
		ConsumptionBehaviorProfile.UNDEFINED -> "Indefinido"
	}
}

fun ConsumptionBehaviorProfile.toDescription(): String {
	return when (this) {
		ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Maior presenÃ§a de produtos industrializados e compras voltadas Ã  praticidade."
		ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "PredominÃ¢ncia de itens essenciais e alimentaÃ§Ã£o bÃ¡sica nas compras analisadas."
		ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "DistribuiÃ§Ã£o relativamente equilibrada entre diferentes categorias de consumo."
		ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Bebidas aparecem com recorrÃªncia relevante nas notas analisadas."
		ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa participaÃ§Ã£o de hortifruti e alimentos frescos no consumo analisado."
		ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Maior presenÃ§a de produtos de higiene e limpeza domÃ©stica."
		ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Grande parte do consumo estÃ¡ concentrada em poucas categorias."
		ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Maior presenÃ§a de categorias nÃ£o essenciais e compras de conveniÃªncia."
		ConsumptionBehaviorProfile.UNDEFINED -> "NÃ£o foi possÃ­vel identificar um padrÃ£o confiÃ¡vel com os dados atuais."
	}
}

private fun BehaviorClassificationSource.toDisplayLabel(): String {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> "Modelo treinado"
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> "Fallback local"
	}
}

private fun BehaviorClassificationSource.toTechnicalLabel(): String {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> "TRAINED_MODEL"
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> "RULE_BASED_FALLBACK"
	}
}

private fun BehaviorClassificationSource.toWarningMessage(fallbackReason: FallbackReason?): String? {
	return when (this) {
		BehaviorClassificationSource.TRAINED_MODEL -> null
		BehaviorClassificationSource.RULE_BASED_FALLBACK -> buildString {
			if (fallbackReason == FallbackReason.BACKEND_REJECTED_INPUT) {
				append("NÃ£o foi possÃ­vel usar o modelo treinado V2. Resultado gerado localmente.")
			} else {
				append("Backend indisponÃ­vel. Resultado gerado por fallback local")
				fallbackReason?.let { append(" (${it.name})") }
				append('.')
			}
		}
	}
}


private fun Double.toConfidenceLabel(): String {
	return when {
		this >= 0.85 -> "PadrÃ£o de consumo muito consistente"
		this >= 0.70 -> "PadrÃ£o de consumo consistente"
		this >= 0.50 -> "PadrÃ£o de consumo parcialmente consistente"
		else -> "PadrÃ£o de consumo variado"
	}
}


private fun Double.toPercentageText(): String = "${"%.1f".format(Locale.US, this * 100)}%"

private fun Double.toNumberText(): String = "%.4f".format(Locale.US, this)

private fun Double?.orZero(): Double = this ?: 0.0

```

## FILE: app/src/main/java/com/example/consumoai/ui/theme/Color.kt

```kotlin
package com.example.consumoai.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
```

## FILE: app/src/main/java/com/example/consumoai/ui/theme/Theme.kt

```kotlin
package com.example.consumoai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ConsumoAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

## FILE: app/src/main/java/com/example/consumoai/ui/theme/Type.kt

```kotlin
package com.example.consumoai.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)
```

## FILE: app/src/test/java/com/example/consumoai/data/classifier/KeywordProductClassifierDataSourceTest.kt

```kotlin
package com.example.consumoai.data.classifier

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for KeywordProductClassifierDataSource (V1.1 - 2026-05-14)
 *
 * Objective: Validate that ~125+ items from OTHER are reclassified to proper categories
 * based on expanded keywords and special rule functions.
 *
 * Status: Covers beverages, hygiene, cleaning, industrialized, basic_food, and produce
 */
class KeywordProductClassifierDataSourceTest {

    private val classifier = KeywordProductClassifierDataSource()

    // ========== BEVERAGES (including alcoholic and energy drinks) ==========

    @Test
    fun classify_beverage_softDrinks() {
        assertCategory("COCA-COLA ORIGINAL LATA 350ML", ProductCategory.BEVERAGES)
        assertCategory("SUCO NATURALE LARANJA", ProductCategory.BEVERAGES)
        assertCategory("GUARANA 2L", ProductCategory.BEVERAGES)
        assertCategory("FANTA UVAICE 1LT", ProductCategory.BEVERAGES)
        assertCategory("SPRITE 2LT", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_alcoholic() {
        // Beer - common brands and styles
        assertCategory("CHOPP TUPINIQUIM IPA 1L", ProductCategory.BEVERAGES)
        assertCategory("CV BADEN BADEN IPA LT 473ML", ProductCategory.BEVERAGES)
        assertCategory("CV BLUE MOON BELGIAN LT 350ML", ProductCategory.BEVERAGES)
        assertCategory("KAISERDOM PREMIUM 350ML", ProductCategory.BEVERAGES)
        assertCategory("PILSEN BRAHMA 350ML", ProductCategory.BEVERAGES)

        // Wine
        assertCategory("VH AURORA C.SAUV 750ML", ProductCategory.BEVERAGES)
        assertCategory("VINHO CONCHA Y TORO 750ML", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_energy() {
        assertCategory("ENERG MONSTER 473ML", ProductCategory.BEVERAGES)
        assertCategory("ENERGETICO RED BULL 250ML", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_water() {
        assertCategory("AGUA MINERAL PUREZA 1.5L", ProductCategory.BEVERAGES)
        assertCategory("CHA GELADO", ProductCategory.BEVERAGES)
    }

    // ========== HYGIENE ==========

    @Test
    fun classify_hygiene_paperProducts() {
        assertCategory("P H NEVE T.SEDA DUPL L16P15 30M", ProductCategory.HYGIENE)
        assertCategory("PAPEL HIGIENICO SOFT 30M", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_hairCare() {
        assertCategory("SH HEAD&SHOULDERS A.COC 650ML", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN LIMP PROF 400ML", ProductCategory.HYGIENE)
        assertCategory("CONDICIONADOR PANTENE 200ML", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_personalCare() {
        assertCategory("SABONETE DOVE 90G", ProductCategory.HYGIENE)
        assertCategory("DESODORANTE REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("CARGA GILLETTE MACH3 SENS C/2", ProductCategory.HYGIENE)
        assertCategory("CR D COLGATE T.ACAO 180G PROM", ProductCategory.HYGIENE)
        assertCategory("ESCOVA DENTAL COLGATE", ProductCategory.HYGIENE)
        assertCategory("FIO DENTAL ORAL-B 50M", ProductCategory.HYGIENE)
        assertCategory("ABSORVENTE KOTEX PROTEPLUS", ProductCategory.HYGIENE)
        assertCategory("FRALDA PAMPERS RN", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_wipes() {
        assertCategory("TOALHA UMED HUGGIES L4P3 C/48UN", ProductCategory.HYGIENE)
        assertCategory("LENCO UMED HUGGIES T.PROT C/88", ProductCategory.HYGIENE)
        assertCategory("LENCO UMEDECIDO KLEENEX", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_otherProducts() {
        assertCategory("ALGODAO JOHNSON 100G", ProductCategory.HYGIENE)
        assertCategory("COTONETE JOHNSON", ProductCategory.HYGIENE)
        assertCategory("PRESERVATIVO OLLA", ProductCategory.HYGIENE)
    }

    // ========== CLEANING ==========

    @Test
    fun classify_cleaning_dishDetergent() {
        assertCategory("DET LQ LOUCA LIMPOL LIMAO 500ML", ProductCategory.CLEANING)
        assertCategory("DETERGENTE SUNDOWN 500ML", ProductCategory.CLEANING)
        assertCategory("LAV LOUCA ULTRAPURO 500ML", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_laundry() {
        assertCategory("L ROUP PO OMO L.PERFEITA 2,2KG", ProductCategory.CLEANING)
        assertCategory("LAVA ROUPAS PO ARIEL 1.5KG", ProductCategory.CLEANING)
        assertCategory("AMAC CONC COMFORT FRESCOR 1L", ProductCategory.CLEANING)
        assertCategory("AMACIANTE DOWNY 900ML", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_floors() {
        assertCategory("LIMP AJAX FRESH PODER 1L", ProductCategory.CLEANING)
        assertCategory("LIMP PISO MAD DESTAC L&VAN 750ML", ProductCategory.CLEANING)
        assertCategory("MULTIUSO SPRAY AJAX", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_disinfectants() {
        assertCategory("DESINF PINHO SOL ORIG L500P450ML", ProductCategory.CLEANING)
        assertCategory("DESINFETANTE LYSOFORM", ProductCategory.CLEANING)
        assertCategory("AGUA SANITARIA 1L", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_otherProducts() {
        assertCategory("SACO LIXO UTILO 50L C/10", ProductCategory.CLEANING)
        assertCategory("ESPONJA ESFREBOM BOMBRIL", ProductCategory.CLEANING)
        assertCategory("P TOALHA MILI 3X200F", ProductCategory.CLEANING)
        assertCategory("PAPEL TOALHA ROLL 200M", ProductCategory.CLEANING)
        assertCategory("FILTRO PAPEL LITE", ProductCategory.CLEANING)
    }

    // ========== INDUSTRIALIZED ==========

    @Test
    fun classify_industrialized_pizza() {
        assertCategory("PIZZA SADIA MUSSARELA 440G", ProductCategory.INDUSTRIALIZED)
        assertCategory("PIZZA SEARA CALABRESA 500G", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_pasta() {
        assertCategory("LASANHA SADIA BOLONHESA MN350G", ProductCategory.INDUSTRIALIZED)
        assertCategory("MAC NISSIN GALINHA CAIPIRA 85G", ProductCategory.INDUSTRIALIZED)
        assertCategory("NISSIN LAMEN FRANGO 85G", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_snacks() {
        assertCategory("SALG FANDANGOS PRESUNTO 160G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALG DORITOS MOSTARDA HEINZ 110G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALG CHEETOS ONDA REQUEIJAO 105G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALGADINHO FRITO LAY", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_sweets() {
        assertCategory("TRENTO DARK 55% CACAU 29G", ProductCategory.INDUSTRIALIZED)
        assertCategory("BOLO MARMORE S.BOYS 250G", ProductCategory.INDUSTRIALIZED)
        assertCategory("ROSCA POLV TRAD GUSMAN 70G", ProductCategory.INDUSTRIALIZED)
        assertCategory("CHOCOLATE BRIGADEIRO", ProductCategory.INDUSTRIALIZED)
        assertCategory("BALA HALLS MENTA", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_snackFood() {
        assertCategory("PIPOCA MIC YOKI COB CARAM 160G", ProductCategory.INDUSTRIALIZED)
        assertCategory("COOKIE BAUDUCCO CHOCOLATE", ProductCategory.INDUSTRIALIZED)
        assertCategory("WAFER BAUDUCCO CHOCOLATE", ProductCategory.INDUSTRIALIZED)
        assertCategory("BISCOITO ISABELA", ProductCategory.INDUSTRIALIZED)
        assertCategory("WRAP TORTILHA FRESCATA", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_sauces() {
        assertCategory("MAIONESE HELLMANN 500ML", ProductCategory.INDUSTRIALIZED)
        assertCategory("KETCHUP HEINZ 1KG", ProductCategory.INDUSTRIALIZED)
        assertCategory("MOSTARDA HEINZ 390G", ProductCategory.INDUSTRIALIZED)
        assertCategory("TEMPERO SAZON 60G", ProductCategory.INDUSTRIALIZED)
    }

    // ========== BASIC FOOD ==========

    @Test
    fun classify_basicFood_meat() {
        assertCategory("PATINHO ZAFFARI NOV E.LIMPO", ProductCategory.BASIC_FOOD)
        assertCategory("COXAO DENTRO PEDACO", ProductCategory.BASIC_FOOD)
        assertCategory("CARNE MOIDA 500G", ProductCategory.BASIC_FOOD)
        assertCategory("FRANGO INTEIRO SADIA", ProductCategory.BASIC_FOOD)
        assertCategory("PEITO FRANGO CONGELADO", ProductCategory.BASIC_FOOD)
        assertCategory("SALSICHA SADIA 500G", ProductCategory.BASIC_FOOD)
        assertCategory("LINGUICA PERDIGAO 500G", ProductCategory.BASIC_FOOD)
        assertCategory("PRESUNTO SADIA FATIADO", ProductCategory.BASIC_FOOD)
        assertCategory("MORTADELA SADIA 500G", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_dairy() {
        assertCategory("LEITE INTEGRAL PARMALAT 1L", ProductCategory.BASIC_FOOD)
        assertCategory("IOGURTE ATIVA 540G", ProductCategory.BASIC_FOOD)
        assertCategory("IOG NATURAL INTEGRAL", ProductCategory.BASIC_FOOD)
        assertCategory("QJO MUSSARELA PRES FAT 300G", ProductCategory.BASIC_FOOD)
        assertCategory("QUEIJO MOZZARELLA SADIA", ProductCategory.BASIC_FOOD)
        assertCategory("REQUEIJAO VIGOR CREM TRAD 400G", ProductCategory.BASIC_FOOD)
        assertCategory("MANTEIGA COM SAL BRIT", ProductCategory.BASIC_FOOD)
        assertCategory("MARGARINA DELICIA", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_tomatoProducts() {
        assertCategory("MOLHO TOMATE HEINZ 350G", ProductCategory.BASIC_FOOD)
        assertCategory("PASSATA MOLISANA 690G", ProductCategory.BASIC_FOOD)
        assertCategory("EXTRATO TOMATE CASARÃƒO", ProductCategory.BASIC_FOOD)
        assertCategory("TOMATE PELADO MUTTI 400G", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_grains() {
        assertCategory("ARROZ TIPO 1 INTEGRAL", ProductCategory.BASIC_FOOD)
        assertCategory("FEIJAO PRETO 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("MACARRAO SEMOLINA GALLO", ProductCategory.BASIC_FOOD)
        assertCategory("ESPAGUETE GALLO 500G", ProductCategory.BASIC_FOOD)
        assertCategory("FARINHA BRANCA DONA BENTA", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_otherProducts() {
        assertCategory("CAFE PREMIUM 500G", ProductCategory.BASIC_FOOD)
        assertCategory("ACUCAR CRISTAL CRISTALINO 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("SAL REFINADO EXTRA IOD CISNE 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("OLEO SOJA CARGILL 900ML", ProductCategory.BASIC_FOOD)
        assertCategory("MEL APIARIO PADRE ASSIS 500G", ProductCategory.BASIC_FOOD)
        assertCategory("AVEIA FLOCOS FINOS TUTTI 170G", ProductCategory.BASIC_FOOD)
        assertCategory("AMENDOIM BCO PREM F.FRIDA 400G", ProductCategory.BASIC_FOOD)
        assertCategory("MILHO VERDE 700G", ProductCategory.BASIC_FOOD)
        assertCategory("PAO FRANCES DIARIO", ProductCategory.BASIC_FOOD)
        assertCategory("CACETINHO PRADO", ProductCategory.BASIC_FOOD)
        assertCategory("OVO CAIPIRA DZIA 30UN", ProductCategory.BASIC_FOOD)
        assertCategory("ATUM GOMES DA COSTA 170G", ProductCategory.BASIC_FOOD)
        assertCategory("SARDINHA CONSERVA", ProductCategory.BASIC_FOOD)
    }

    // ========== PRODUCE ==========

    @Test
    fun classify_produce_fruits() {
        assertCategory("BANANA PRATA", ProductCategory.PRODUCE)
        assertCategory("MACA RED 1KG", ProductCategory.PRODUCE)
        assertCategory("MAMAO PAPAYA", ProductCategory.PRODUCE)
        assertCategory("LARANJA PERA MUDA", ProductCategory.PRODUCE)
        assertCategory("MORANGO 500G", ProductCategory.PRODUCE)
        assertCategory("UVA VERDE 1KG", ProductCategory.PRODUCE)
        assertCategory("LIMAO TAHITI", ProductCategory.PRODUCE)
        assertCategory("ABACAXI PEROLA", ProductCategory.PRODUCE)
        assertCategory("MELANCIA ESTACAO CUBO AS", ProductCategory.PRODUCE)
        assertCategory("MELAO 1,5KG", ProductCategory.PRODUCE)
        assertCategory("PERA IMPORTADA", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_vegetables() {
        assertCategory("TOMATE ITALIANO 500G", ProductCategory.PRODUCE)
        assertCategory("BATATA COMUM 1KG", ProductCategory.PRODUCE)
        assertCategory("CENOURA 1KG", ProductCategory.PRODUCE)
        assertCategory("CEBOLA ROXA 1KG", ProductCategory.PRODUCE)
        assertCategory("ALFACE HIDROPONICA", ProductCategory.PRODUCE)
        assertCategory("PIMENTAO VERDE GRANEL", ProductCategory.PRODUCE)
        assertCategory("CEBOLINHA FRESCA 100G", ProductCategory.PRODUCE)
        assertCategory("ALHO GRANEL", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_greenVegetables() {
        assertCategory("BROCOLIS 500G", ProductCategory.PRODUCE)
        assertCategory("COUVE MINEIRA FRESCA", ProductCategory.PRODUCE)
        assertCategory("REPOLHO VERDE 1KG", ProductCategory.PRODUCE)
        assertCategory("PEPINO COMUM", ProductCategory.PRODUCE)
        assertCategory("ABOBRINHA ITALIANA", ProductCategory.PRODUCE)
        assertCategory("BERINJELA ROXA", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_roots() {
        assertCategory("MANDIOCA FRESCA 1KG", ProductCategory.PRODUCE)
        assertCategory("AIPIM DESCASCADO", ProductCategory.PRODUCE)
        assertCategory("INHAME BRANCO", ProductCategory.PRODUCE)
    }

    // ========== AMBIGUOUS/EDGE CASES ==========

    @Test
    fun classify_ambiguous_paperVsCloth() {
        // P TOALHA should be CLEANING, not HYGIENE
        assertCategory("P TOALHA MILI 3X200F", ProductCategory.CLEANING)

        // TOALHA UMED should be HYGIENE
        assertCategory("TOALHA UMED HUGGIES L4P3 C/48UN", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_desodorantContext() {
        // DES with REXONA or DOVE -> HYGIENE
        assertCategory("DES REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("DES DOVE", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_shampooBrand() {
        // SH with CLEAR or HEAD -> HYGIENE
        assertCategory("SH HEAD&SHOULDERS A.COC 650ML", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN LIMP PROF 400ML", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_shortTokens_doNotGenerateFalsePositivesWithoutContext() {
        assertCategory("SH 250ML", ProductCategory.OTHER)
        assertCategory("DES 150ML", ProductCategory.OTHER)
        assertCategory("SAB 90G", ProductCategory.OTHER)
        assertCategory("ALE 600ML", ProductCategory.OTHER)
        assertCategory("VH 750ML", ProductCategory.OTHER)
        assertCategory("BEB MISTA", ProductCategory.OTHER)
    }

    @Test
    fun classify_ambiguous_shortTokens_matchWhenContextIsSafe() {
        assertCategory("VH AURORA C.SAUV 750ML", ProductCategory.BEVERAGES)
        assertCategory("DES REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("SAB DOVE KARITE 90G", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN 400ML", ProductCategory.HYGIENE)
        assertCategory("AMAC COMFORT FRESCOR 1L", ProductCategory.CLEANING)
    }

    @Test
    fun classify_ambiguous_fleshTomatoVsProcessed() {
        // Pure TOMATE -> PRODUCE
        assertCategory("TOMATE ITALIANO 500G", ProductCategory.PRODUCE)

        // MOLHO TOMATE -> BASIC_FOOD
        assertCategory("MOLHO TOMATE HEINZ 350G", ProductCategory.BASIC_FOOD)
    }

    // ========== ITEMS THAT SHOULD REMAIN OTHER (non-food) ==========

    @Test
    fun classify_nonFood_shouldRemainOther() {
        // These items don't fit into food/personal care categories in V1
        assertCategory("TULIPA VASO 12", ProductCategory.OTHER)
        assertCategory("CANECA PLASUTIL 14468 MICK 360ML", ProductCategory.OTHER)
        assertCategory("FITA CREPE T.BOND FIXA 48X50M", ProductCategory.OTHER)
        assertCategory("CAD 1M 80F CREDEAL ESSENC 281635", ProductCategory.OTHER)
        assertCategory("PULVERIZ SANREMO 512 AZ 350ML", ProductCategory.OTHER)
        assertCategory("ALIM CAO PEDIGREE FIL 900G", ProductCategory.OTHER)  // Pet food
        assertCategory("BIFINHO PETHAND SENIOR CARN 65G", ProductCategory.OTHER)  // Pet food
    }

    // ========== TEST COVERAGE ==========

    @Test
    fun classifyAll_generatesClassificationSummary() {
        val items = listOf(
            ProductItem(name = "COCA-COLA 350ML", price = 5.0),
            ProductItem(name = "ARROZ 1KG", price = 8.0),
            ProductItem(name = "TOMATE", price = 3.0),
            ProductItem(name = "SABONETE DOVE", price = 2.5),
            ProductItem(name = "DETERGENTE 500ML", price = 3.0),
            ProductItem(name = "PIZZA SADIA", price = 12.0),
            ProductItem(name = "DESCONHECIDO XYZ", price = 99.0)  // This will be OTHER
        )

        val results = classifier.classifyAll(items)

        // Summary will be logged, we just ensure it doesn't crash
        assertEquals(7, results.size)

        // Check that at least 6 are not OTHER
        val otherCount = results.count { it.category == ProductCategory.OTHER }
        assertEquals(1, otherCount)
    }

    private fun assertCategory(name: String, expected: ProductCategory) {
        val item = ProductItem(name = name, price = 10.0)
        val result = classifier.classify(item)
        assertEquals("Failed for item: '$name'", expected, result.category)
    }
}
```

## FILE: app/src/test/java/com/example/consumoai/data/classifier/KeywordProductSemanticTaggerTest.kt

```kotlin
package com.example.consumoai.data.classifier

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductSemanticTag
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

    private fun assertHas(name: String, tag: ProductSemanticTag) {
        val tags = tagger.tagsFor(ProductItem(name = name, price = 1.0, category = ProductCategory.OTHER))
        assertTrue("Expected $tag for '$name' but got $tags", tags.contains(tag))
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/data/classifier/RemoteConsumptionBehaviorClassifierTest.kt

```kotlin
package com.example.consumoai.data.classifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.FallbackReason
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURES
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
class RemoteConsumptionBehaviorClassifierTest {
    @Test
    fun classify_returnsRemotePredictionWhenApiSucceeds() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    assertEquals(MODEL_INPUT_VERSION, request.version)
                    assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, request.features.size)
                    return ModelPredictionResponseDto(
                        main_profile = "BEVERAGE_RECURRENT",
                        confidence = 0.465,
                        profile_scores = mapOf(
                            "BEVERAGE_RECURRENT" to 0.465,
                            "DIVERSIFIED_BALANCED" to 0.295,
                            "LOW_FRESH_FOOD" to 0.13
                        ),
                        version = request.version,
                        feature_count = request.features.size,
                        model = "consumoai_xgboost_v2_top15.pkl"
                    )
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(input())
        assertEquals(BehaviorClassificationSource.TRAINED_MODEL, result.source)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, result.mainProfile)
        assertEquals(0.465, result.confidence, 0.0001)
        assertEquals(0.295, result.profileScores[ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED] ?: -1.0, 0.0001)
        assertEquals("v2", result.requestedInputVersion)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, result.requestedFeatureCount)
        assertEquals("v2", result.responseVersion)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, result.responseFeatureCount)
        assertEquals("consumoai_xgboost_v2_top15.pkl", result.backendModelUsed)
        assertEquals(true, result.inferenceDurationMs >= 0L)
    }
    @Test
    fun classify_usesFallbackWhenApiFails() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("backend offline")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(
            input(
                "beverages_frequency" to 0.75
            )
        )
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(1.0, result.confidence, 0.0001)
        assertEquals(FallbackReason.INFERENCE_ERROR, result.fallbackReason)
        assertEquals("v2", result.requestedInputVersion)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, result.requestedFeatureCount)
    }

    @Test
    fun classify_usesFallbackWhenBackendRejectsInputWith400() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    throw HttpException(Response.error<Any>(400, "bad request".toResponseBody(null)))
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )

        val result = classifier.classify(input())

        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(FallbackReason.BACKEND_REJECTED_INPUT, result.fallbackReason)
        assertEquals("v2", result.requestedInputVersion)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, result.requestedFeatureCount)
    }
    @Test
    fun classify_usesFallbackWhenInputHasNoFeatures() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    error("should not call API")
                }
            },
            fallbackClassifier = RuleBasedConsumptionBehaviorClassifier()
        )
        val result = classifier.classify(ConsumptionModelInput(features = emptyMap()))
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(FallbackReason.EMPTY_FEATURES, result.fallbackReason)
    }
    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = MODEL_V2_FINAL_FEATURES
            .associateWith { 0.1 }
            .toMutableMap()
            .apply {
                // Definir valores especÃ­ficos para cada uma das 15 features de TOP15
                this["classified_items_percentage"] = 0.90
                this["category_stability_score"] = 0.65
                this["basic_produce_cooccurrence_frequency"] = 0.40
                this["ticket_variation_coefficient"] = 0.25
                this["essential_routine_score"] = 0.55
                this["household_routine_score"] = 0.20
                this["category_concentration_index"] = 0.30
                this["produce_frequency"] = 0.35
                this["hygiene_cleaning_cooccurrence_frequency"] = 0.15
                this["other_value_pct"] = 0.08
                this["beverages_frequency"] = 0.70
                this["essential_score"] = 0.60
                this["category_dominance_gap"] = 0.25
                this["essential_categories_percentage"] = 0.50
                this["beverage_routine_score"] = 0.45
            }
        assertEquals(MODEL_V2_FINAL_FEATURES, defaults.keys.toList())
        overrides.forEach { (key, value) ->
            defaults[key] = value
        }
        return ConsumptionModelInput(features = defaults)
    }
}
```

## FILE: app/src/test/java/com/example/consumoai/data/parser/NfceHtmlParserDataSourceTest.kt

```kotlin
package com.example.consumoai.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class NfceHtmlParserDataSourceTest {

    private val parser = NfceHtmlParserDataSource()

    @Test
    fun parse_extractsProductsFromRowsAndIgnoresFooter() {
        val html = """
            <html>
              <body>
                <table>
                  <tr><td>1 SUCO NATURALE 13,90</td></tr>
                  <tr><td>2 COCA-COLA ORIG 2L 10,93</td></tr>
                  <tr><td>VALOR TOTAL 24,83</td></tr>
                </table>
              </body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(Jsoup.parse(html))
        val products = parsed.items

        assertEquals(2, products.size)
        assertEquals(1, products[0].itemNumber)
        assertEquals("SUCO NATURALE", products[0].name)
        assertEquals(13.90, products[0].price, 0.0001)
        assertEquals(2, products[1].itemNumber)
        assertEquals("COCA-COLA ORIG 2L", products[1].name)
        assertEquals(10.93, products[1].price, 0.0001)
    }

    @Test
    fun parse_extractsIssueDateWhenEmissionIsPresent() {
        val html = """
            <html>
              <body>
                Data de Emissao: 14/05/2026 18:45:11
                <table>
                  <tr><td>1 ARROZ 8,90</td></tr>
                </table>
              </body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(Jsoup.parse(html))

        assertNotNull(parsed.issueDate)
        assertEquals(LocalDate.of(2026, 5, 14), parsed.issueDate)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/data/repository/ReceiptRepositoryImplTest.kt

```kotlin
package com.example.consumoai.data.repository

import com.example.consumoai.data.local.dao.ReceiptDao
import com.example.consumoai.data.local.entity.ProductItemEntity
import com.example.consumoai.data.local.entity.ReceiptEntity
import com.example.consumoai.data.local.entity.ReceiptWithItems
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptRepositoryImplTest {

    @Test
    fun saveReceipt_persistsReceiptAndSkipsDuplicatesByUrl() = runBlocking {
        val dao = FakeReceiptDao()
        val repository = ReceiptRepositoryImpl(dao)
        val receipt = Receipt(
            accessKeyOrUrl = "https://example.com/nfce/1",
            source = ReceiptSource.QR_CODE,
            items = listOf(
                ProductItem(itemNumber = 1, name = "SUCO", price = 10.0),
                ProductItem(itemNumber = 2, name = "PAO", price = 5.0)
            )
        )

        repository.saveReceipt(receipt)
        repository.saveReceipt(receipt)

        val stored = repository.getAllReceipts()
        assertEquals(1, stored.size)
        assertEquals(2, stored.first().items.size)
        assertEquals(15.0, stored.first().totalValue, 0.0001)
        assertTrue(repository.existsByAccessKeyOrUrl("https://example.com/nfce/1"))
    }

    @Test
    fun clearReceipts_removesAllStoredData() = runBlocking {
        val dao = FakeReceiptDao()
        val repository = ReceiptRepositoryImpl(dao)

        repository.saveReceipt(
            Receipt(
                accessKeyOrUrl = "https://example.com/nfce/2",
                source = ReceiptSource.QR_CODE,
                items = listOf(ProductItem(itemNumber = 1, name = "BISCOITO", price = 3.5))
            )
        )

        repository.clearReceipts()

        assertTrue(repository.getAllReceipts().isEmpty())
    }

    private class FakeReceiptDao : ReceiptDao {
        private val receipts = mutableListOf<ReceiptEntity>()
        private val items = mutableListOf<ProductItemEntity>()
        private var nextReceiptId = 1L
        private var nextItemId = 1L

        override suspend fun insertReceipt(receipt: ReceiptEntity): Long {
            val id = nextReceiptId++
            receipts.add(receipt.copy(id = id))
            return id
        }

        override suspend fun insertItems(items: List<ProductItemEntity>) {
            this.items.addAll(items.map { it.copy(id = nextItemId++) })
        }

        override suspend fun getAllReceiptsWithItems(): List<ReceiptWithItems> {
            return receipts.reversed().map { receipt ->
                ReceiptWithItems(
                    receipt = receipt,
                    items = items.filter { it.receiptId == receipt.id }
                )
            }
        }

        override suspend fun deleteAllReceipts() {
            receipts.clear()
            items.clear()
            nextReceiptId = 1L
            nextItemId = 1L
        }

        override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean {
            return receipts.any { it.accessKeyOrUrl == accessKeyOrUrl }
        }
    }
}
```

## FILE: app/src/test/java/com/example/consumoai/domain/insights/DefaultConsumptionInsightsEngineTest.kt

```kotlin
package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsV2UseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultConsumptionInsightsEngineTest {

    @Test
    fun generate_ordersInsightsBySeverityAndProducesNeutralUtf8Text() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 9.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SALGADINHO", price = 7.0, category = ProductCategory.INDUSTRIALIZED)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ENERGETICO", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BARRA", price = 6.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase()(receipts)
        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val result = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
            confidence = 0.45,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.46,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.29,
                ConsumptionBehaviorProfile.LOW_FRESH_FOOD to 0.13
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )

        val analysis = DefaultConsumptionInsightsEngine().generate(metricsV2, result)

        assertFalse(analysis.insights.isEmpty())
        assertEquals("Bebidas aparecem com alta recorrÃªncia", analysis.insights.first().title)
        assertTrue(analysis.summary.contains("padrÃ£o"))

        val allText = buildString {
            analysis.insights.forEach {
                append(it.title)
                append(it.description)
            }
            append(analysis.summary)
        }
        assertFalse(allText.contains("Ãƒ"))
    }

    @Test
    fun generate_addsCompositeInsightWhenTopProfilesAndMetricsSupportHybridNarrative() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 14.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 12.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "ALFACE", price = 6.0, category = ProductCategory.PRODUCE)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "FEIJAO", price = 11.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "SABONETE", price = 8.0, category = ProductCategory.HYGIENE)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ENERGETICO", price = 15.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "PAO", price = 9.0, category = ProductCategory.BASIC_FOOD),
                    ProductItem(name = "TOMATE", price = 7.0, category = ProductCategory.PRODUCE)
                )
            )
        )

        val metrics = CalculateConsumptionMetricsUseCase()(receipts)
        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val result = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
            confidence = 0.41,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.41,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.27,
                ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED to 0.22
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )

        val analysis = DefaultConsumptionInsightsEngine().generate(metricsV2, result)

        assertTrue(
            analysis.insights.any {
                it.title.contains("EquilÃ­brio entre itens essenciais e bebidas")
            }
        )
    }
}
```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/AnalyzeStoredReceiptsUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURES
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AnalyzeStoredReceiptsUseCaseTest {

    @Test
    fun invoke_returnsStoredAnalysisFromRepository() = runBlocking {
        val repository = object : ReceiptRepository {
            override suspend fun saveReceipt(receipt: Receipt) = Unit

            override suspend fun getAllReceipts(): List<Receipt> = listOf(
                Receipt(
                    id = 1L,
                    accessKeyOrUrl = "u1",
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                        ProductItem(name = "PAO", price = 5.0, category = ProductCategory.BASIC_FOOD)
                    )
                ),
                Receipt(
                    id = 2L,
                    accessKeyOrUrl = "u2",
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "LEITE", price = 7.5, category = ProductCategory.BASIC_FOOD)
                    )
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        val analysis = AnalyzeStoredReceiptsUseCase(
            receiptRepository = repository,
            calculateConsumptionMetricsV2UseCase = CalculateConsumptionMetricsV2UseCase(
                calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
                semanticTagger = KeywordProductSemanticTagger()
            ),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
            ),
            insightsEngine = DefaultConsumptionInsightsEngine(),
            consumptionFeatureSanitizer = ConsumptionFeatureSanitizer(),
            buildConsumptionProfileSummaryUseCase = BuildConsumptionProfileSummaryUseCase()
        )()

        assertEquals(2, analysis.receipts.size)
        assertEquals(2, analysis.metricsV2.baseMetrics.totalReceipts)
        assertEquals(3, analysis.metricsV2.baseMetrics.totalItems)
        assertEquals(22.5, analysis.metricsV2.baseMetrics.totalValue, 0.0001)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, analysis.modelInput.features.size)
        assertEquals(MODEL_V2_FINAL_FEATURES, analysis.modelInput.features.keys.toList())
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, analysis.behaviorResult.source)
        assertEquals(true, analysis.behaviorResult.profileSummary != null)
        assertEquals(analysis.behaviorResult.mainProfile, analysis.behaviorAnalysis.behaviorResult.mainProfile)
        assertEquals(true, analysis.behaviorAnalysis.insights.isNotEmpty())
        assertEquals(true, analysis.behaviorAnalysis.behavioralComposition.isNotEmpty())
        assertEquals(true, analysis.behaviorAnalysis.summary.isNotEmpty())
    }

    @Test
    fun invoke_sendsV2InputToClassifierWithTop15Features() = runBlocking {
        val repository = object : ReceiptRepository {
            override suspend fun saveReceipt(receipt: Receipt) = Unit

            override suspend fun getAllReceipts(): List<Receipt> = listOf(
                Receipt(
                    id = 1L,
                    accessKeyOrUrl = "u1",
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                        ProductItem(name = "PAO", price = 5.0, category = ProductCategory.BASIC_FOOD)
                    )
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        var capturedVersion: String? = null
        var capturedFeatureCount: Int? = null

        val classifier = object : ConsumptionBehaviorClassifier {
            override suspend fun classify(input: com.example.consumoai.domain.model.ConsumptionModelInput): ConsumptionBehaviorResult {
                capturedVersion = input.version
                capturedFeatureCount = input.features.size
                return ConsumptionBehaviorResult(
                    mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                    confidence = 0.8,
                    profileScores = mapOf(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.8),
                    source = BehaviorClassificationSource.RULE_BASED_FALLBACK
                )
            }
        }

        val analysis = AnalyzeStoredReceiptsUseCase(
            receiptRepository = repository,
            calculateConsumptionMetricsV2UseCase = CalculateConsumptionMetricsV2UseCase(
                calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
                semanticTagger = KeywordProductSemanticTagger()
            ),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = classifier
            ),
            insightsEngine = DefaultConsumptionInsightsEngine(),
            consumptionFeatureSanitizer = ConsumptionFeatureSanitizer(),
            buildConsumptionProfileSummaryUseCase = BuildConsumptionProfileSummaryUseCase()
        )()

        assertEquals(MODEL_INPUT_VERSION, capturedVersion)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, capturedFeatureCount)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, analysis.modelInput.features.size)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/BuildConsumptionModelInputUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURE_COUNT
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURES
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BuildConsumptionModelInputUseCaseTest {

    private val useCase = BuildConsumptionModelInputUseCase()

    @Test
    fun invoke_buildsOfficialV2Top15FeatureMapWithoutNaNOrInfinity() {
        val receipts = listOf(
            Receipt(
                date = LocalDate.of(2026, 5, 1),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 8.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 10),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SABAO", price = 9.0, category = ProductCategory.CLEANING),
                    ProductItem(name = "MELANCIA", price = 12.0, category = ProductCategory.PRODUCE)
                )
            )
        )

        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)

        val result = useCase(metricsV2)

        assertEquals(MODEL_INPUT_VERSION, result.version)
        assertEquals(MODEL_V2_FINAL_FEATURE_COUNT, result.features.size)
        assertEquals(15, result.features.size)
        assertEquals(MODEL_V2_FINAL_FEATURES, result.features.keys.toList())

        // Validar presenÃ§a de algumas das 15 features oficiais
        assertTrue(result.features.containsKey("classified_items_percentage"))
        assertTrue(result.features.containsKey("category_stability_score"))
        assertTrue(result.features.containsKey("essential_routine_score"))
        assertTrue(result.features.containsKey("beverage_routine_score"))

        // Validar integridade
        assertFalse(result.features.values.any { it.isNaN() })
        assertFalse(result.features.values.any { it.isInfinite() })
    }
}


```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/BuildConsumptionProfileSummaryUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ProfileInterpretationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConsumptionProfileSummaryUseCaseTest {

    private val useCase = BuildConsumptionProfileSummaryUseCase()

    @Test
    fun invoke_marksPureProfileWhenConfidenceIsHigh() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                confidence = 0.62,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.62,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.20
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.PURE_PROFILE, summary.interpretationType)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, summary.primaryProfile)
    }

    @Test
    fun invoke_marksHybridProfileWhenConfidenceIsLowButSignalsAreMixed() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
                confidence = 0.40,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.40,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.28,
                    ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE to 0.20
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.HYBRID_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("hÃ­brido"))
        assertEquals(2, summary.secondaryProfiles.size)
    }

    @Test
    fun invoke_marksLowConfidenceProfileWhenConfidenceIsVeryLow() {
        val summary = useCase(
            ConsumptionBehaviorResult(
                mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
                confidence = 0.20,
                profileScores = mapOf(
                    ConsumptionBehaviorProfile.UNDEFINED to 0.20,
                    ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.18
                ),
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        )

        assertEquals(ProfileInterpretationType.LOW_CONFIDENCE_PROFILE, summary.interpretationType)
        assertTrue(summary.humanReadableDescription.contains("baixa confianÃ§a"))
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/CalculateConsumptionMetricsUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

class CalculateConsumptionMetricsUseCaseTest {

    private val useCase = CalculateConsumptionMetricsUseCase()

    @Test
    fun invoke_computesExtendedMetricsAndTotals() {
        val receipts = listOf(
            Receipt(
                id = 1,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 10.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 20.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                id = 2,
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "DETERGENTE", price = 5.0, category = ProductCategory.CLEANING),
                    ProductItem(name = "ITEM DESCONHECIDO", price = 15.0, category = ProductCategory.OTHER)
                )
            )
        )

        val metrics = useCase(receipts)

        assertEquals(2, metrics.totalReceipts)
        assertEquals(4, metrics.totalItems)
        assertEquals(50.0, metrics.totalValue, 0.0001)
        assertEquals(25.0, metrics.averageTicket, 0.0001)
        assertEquals(2.0, metrics.averageItemsPerReceipt, 0.0001)
        assertEquals(ProductCategory.BASIC_FOOD, metrics.maxCategoryByValue)
        assertEquals(ProductCategory.BASIC_FOOD, metrics.maxCategoryByItems)
        assertEquals(12.5, metrics.averageValuePerItem, 0.0001)
        assertEquals(30.0, metrics.highestReceiptValue, 0.0001)
        assertEquals(20.0, metrics.lowestReceiptValue, 0.0001)
        assertEquals(10.0, metrics.receiptValueAmplitude, 0.0001)
        assertEquals(20.0, metrics.categoryValueTotals[ProductCategory.BASIC_FOOD] ?: -1.0, 0.0001)
        assertEquals(1, metrics.categoryItemTotals[ProductCategory.CLEANING])
        assertEquals(20.0, metrics.categoryMetrics[ProductCategory.BASIC_FOOD]?.totalValue ?: -1.0, 0.0001)
        assertEquals(1, metrics.categoryMetrics[ProductCategory.CLEANING]?.totalItems ?: -1)
        assertEquals(5.0, metrics.categoryMetrics[ProductCategory.BEVERAGES]?.averageValuePerReceipt ?: -1.0, 0.0001)
        assertEquals(5.0, metrics.receiptAverageValueByCategory[ProductCategory.BEVERAGES] ?: -1.0, 0.0001)
        assertEquals(0.40, metrics.categoryConcentrationIndex, 0.0001)
        assertEquals(listOf(ProductCategory.BASIC_FOOD, ProductCategory.OTHER, ProductCategory.BEVERAGES), metrics.topThreeCategoriesByValue)
        assertEquals(0.90, metrics.topThreeCategoriesValuePercentage, 0.0001)
        assertEquals(0.10, metrics.categoryDominanceGap, 0.0001)
        assertEquals(2.0, metrics.averageCategoriesPerReceipt, 0.0001)
        assertEquals(4.0 / 7.0, metrics.categoryDiversityIndex, 0.0001)
        assertEquals(0.50, metrics.essentialCategoriesPercentage, 0.0001)
        assertEquals(0.50, metrics.nonEssentialCategoriesPercentage, 0.0001)
        assertEquals(0.0, metrics.industrializedToBasicFoodRatio, 0.0001)
        assertEquals(0.50, metrics.beveragesToBasicFoodRatio, 0.0001)
        assertEquals(0.20, metrics.beveragesToTotalRatio, 0.0001)
        assertEquals(0.0, metrics.produceToTotalRatio, 0.0001)
        assertEquals(0.0, metrics.receiptsWithIndustrializedPercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithBeveragesPercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithBasicFoodPercentage, 0.0001)
        assertEquals(0.0, metrics.receiptsWithProducePercentage, 0.0001)
        assertEquals(0.0, metrics.receiptsWithHygienePercentage, 0.0001)
        assertEquals(0.50, metrics.receiptsWithCleaningPercentage, 0.0001)
        assertEquals(0.0, metrics.averageIndustrializedItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.averageBeveragesItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.averageBasicFoodItemsPerReceipt, 0.0001)
        assertEquals(0.0, metrics.averageProduceItemsPerReceipt, 0.0001)
        assertEquals(0.50, metrics.highValueReceiptsPercentage, 0.0001)
        assertEquals(0.50, metrics.lowValueReceiptsPercentage, 0.0001)
        assertEquals(0.0666666667, metrics.convenienceScore, 0.0001)
        assertEquals(0.3333333333, metrics.essentialScore, 0.0001)
        assertEquals(0.4285714286, metrics.diversityScore, 0.0001)
        assertTrue(metrics.valuePercentageByCategory.values.all { it >= 0.0 })
        assertEquals(0.30, metrics.otherPercentageByValue, 0.0001)
        assertEquals(0.25, metrics.otherPercentageByItems, 0.0001)
        assertEquals(0.75, metrics.classifiedItemsPercentage, 0.0001)
    }

    @Test
    fun invoke_returnsZeroedMetricsForEmptyInput() {
        val metrics = useCase(emptyList())

        assertEquals(0, metrics.totalReceipts)
        assertEquals(0, metrics.totalItems)
        assertEquals(0.0, metrics.totalValue, 0.0001)
        assertEquals(0.0, metrics.averageItemsPerReceipt, 0.0001)
        assertNull(metrics.maxCategoryByValue)
        assertNull(metrics.maxCategoryByItems)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/CalculateConsumptionMetricsV2UseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class CalculateConsumptionMetricsV2UseCaseTest {

    private val useCase = CalculateConsumptionMetricsV2UseCase(
        calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
        semanticTagger = KeywordProductSemanticTagger()
    )

    @Test
    fun invoke_calculatesTemporalCooccurrenceRecurrenceAndScores() {
        val receipts = listOf(
            Receipt(
                date = LocalDate.of(2026, 5, 1),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA COLA 2L", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "DORITOS 110G", price = 9.0, category = ProductCategory.INDUSTRIALIZED),
                    ProductItem(name = "ARROZ 1KG", price = 8.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 8),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "CHOPP IPA", price = 18.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "DORITOS 120G", price = 10.0, category = ProductCategory.INDUSTRIALIZED),
                    ProductItem(name = "MELANCIA", price = 15.0, category = ProductCategory.PRODUCE)
                )
            ),
            Receipt(
                date = LocalDate.of(2026, 5, 15),
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "COCA COLA 350ML", price = 6.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "SABONETE DOVE", price = 7.0, category = ProductCategory.HYGIENE),
                    ProductItem(name = "OMO 1KG", price = 20.0, category = ProductCategory.CLEANING)
                )
            )
        )

        val result = useCase(receipts)

        assertTrue(result.timeSpanDays >= 14.0)
        assertTrue(result.receiptsPerWeek > 0.0)
        assertTrue(result.averageDaysBetweenReceipts > 0.0)
        assertTrue(result.recurringItemRatio >= 0.0)
        assertTrue(result.topItemRepetitionRate >= 0.0)
        assertTrue(result.beverageSnackCoOccurrenceFrequency > 0.0)
        assertTrue(result.alcoholSnackCoOccurrenceFrequency > 0.0)
        assertTrue(result.hygieneCleaningCoOccurrenceFrequency > 0.0)
        assertTrue(result.essentialRoutineScore in 0.0..1.0)
        assertTrue(result.convenienceRoutineScore in 0.0..1.0)
        assertTrue(result.beverageRoutineScore in 0.0..1.0)
        assertTrue(result.householdRoutineScore in 0.0..1.0)
        assertTrue(result.freshFoodPresenceScore in 0.0..1.0)

        // Basic sanity around tag-derived metrics
        assertTrue(result.softDrinkFrequency > 0.0)
        assertTrue(result.alcoholicBeverageFrequency > 0.0)
        assertEquals(true, result.baseMetrics.totalReceipts == 3)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/ClassifyConsumptionProfileUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.MODEL_V2_FINAL_FEATURES
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ClassifyConsumptionProfileUseCaseTest {

    private val useCase = ClassifyConsumptionProfileUseCase(
        consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
    )

    @Test
    fun invoke_returnsUndefinedWhenFewItemsAreClassified() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.4,
            "category_concentration_index" to 0.8
        )

        val result = useCase(input)

        assertEquals(ConsumptionBehaviorProfile.UNDEFINED, result.mainProfile)
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(1.0, result.confidence, 0.0001)
    }

    @Test
    fun invoke_returnsBeverageRecurrentWhenTop15BeverageSignalsAreHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.3,
            "beverages_frequency" to 0.75,
            "beverage_routine_score" to 0.6
        )

        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsConvenienceOrientedWhenOtherValueIsElevated() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.4,
            "category_stability_score" to 0.30,
            "other_value_pct" to 0.25
        )

        assertEquals(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsHighlyConcentratedWhenConcentrationIsVeryHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.72,
            "category_dominance_gap" to 0.35
        )

        assertEquals(ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED, useCase(input).mainProfile)
    }

    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = MODEL_V2_FINAL_FEATURES
            .associateWith { 0.1 }
            .toMutableMap()
            .apply {
                this["classified_items_percentage"] = 0.90
                this["category_stability_score"] = 0.60
                this["basic_produce_cooccurrence_frequency"] = 0.25
                this["ticket_variation_coefficient"] = 0.20
                this["essential_routine_score"] = 0.40
                this["household_routine_score"] = 0.15
                this["category_concentration_index"] = 0.30
                this["produce_frequency"] = 0.30
                this["hygiene_cleaning_cooccurrence_frequency"] = 0.10
                this["other_value_pct"] = 0.12
                this["beverages_frequency"] = 0.30
                this["essential_score"] = 0.40
                this["category_dominance_gap"] = 0.10
                this["essential_categories_percentage"] = 0.40
                this["beverage_routine_score"] = 0.25
            }

        overrides.forEach { (key, value) ->
            defaults[key] = value
        }

        return ConsumptionModelInput(features = defaults)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/ConsumptionFeatureSanitizerTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionModelInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsumptionFeatureSanitizerTest {

    private val sanitizer = ConsumptionFeatureSanitizer()

    @Test
    fun invoke_clampsProbabilityLikeValuesAndInvalidNumbers() {
        val input = ConsumptionModelInput(
            features = linkedMapOf(
                "beverages_value_pct" to 1.4,
                "classified_items_percentage" to -0.3,
                "diversity_score" to Double.NaN,
                "total_value" to Double.POSITIVE_INFINITY,
                "total_items" to -5.0
            )
        )

        val result = sanitizer(input)

        assertEquals(1.0, result.input.features.getValue("beverages_value_pct"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("classified_items_percentage"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("diversity_score"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("total_value"), 0.0001)
        assertEquals(0.0, result.input.features.getValue("total_items"), 0.0001)
        assertEquals(5, result.notes.size)
    }

    @Test
    fun invoke_keepsValidValuesUntouched() {
        val input = ConsumptionModelInput(
            features = linkedMapOf(
                "beverages_value_pct" to 0.35,
                "total_value" to 120.0,
                "total_items" to 8.0
            )
        )

        val result = sanitizer(input)

        assertEquals(0.35, result.input.features.getValue("beverages_value_pct"), 0.0001)
        assertEquals(120.0, result.input.features.getValue("total_value"), 0.0001)
        assertTrue(result.notes.isEmpty())
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/GenerateOtherItemsReportUseCaseTest.kt

```kotlin
```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/GetStoredReceiptsSummaryUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetStoredReceiptsSummaryUseCaseTest {

    @Test
    fun invoke_returnsAggregatedSummaryFromStoredReceipts() = runBlocking {
        val repository = object : ReceiptRepository {
            override suspend fun saveReceipt(receipt: Receipt) = Unit

            override suspend fun getAllReceipts(): List<Receipt> = listOf(
                Receipt(
                    id = 1L,
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "SUCO", price = 6.5, category = ProductCategory.BEVERAGES),
                        ProductItem(name = "PAO", price = 4.0, category = ProductCategory.BASIC_FOOD)
                    )
                ),
                Receipt(
                    id = 2L,
                    source = ReceiptSource.QR_CODE,
                    items = listOf(
                        ProductItem(name = "LEITE", price = 8.5, category = ProductCategory.BASIC_FOOD)
                    )
                )
            )

            override suspend fun clearReceipts() = Unit

            override suspend fun existsByAccessKeyOrUrl(accessKeyOrUrl: String): Boolean = false
        }

        val result = GetStoredReceiptsSummaryUseCase(repository)()

        assertEquals(2, result.totalReceipts)
        assertEquals(3, result.totalItems)
        assertEquals(19.0, result.totalValue, 0.0001)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/OtherItemsReportGeneratorTest.kt

```kotlin
```

## FILE: app/src/test/java/com/example/consumoai/presentation/home/model/HomeAnalysisPresentationMapperTest.kt

```kotlin
package com.example.consumoai.presentation.home.model

import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.data.classifier.KeywordProductSemanticTagger
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsV2UseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeAnalysisPresentationMapperTest {

    @Test
    fun toHomeAnalysisPresentation_mapsFriendlyLabelsAndTechnicalData() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SUCO", price = 12.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "ARROZ", price = 9.0, category = ProductCategory.BASIC_FOOD)
                )
            ),
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "REFRIGERANTE", price = 8.0, category = ProductCategory.BEVERAGES),
                    ProductItem(name = "BISCOITO", price = 7.0, category = ProductCategory.INDUSTRIALIZED)
                )
            )
        )

        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metricsV2)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.BEVERAGE_RECURRENT,
            confidence = 0.78,
            profileScores = mapOf(
                ConsumptionBehaviorProfile.BEVERAGE_RECURRENT to 0.46,
                ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED to 0.29,
                ConsumptionBehaviorProfile.LOW_FRESH_FOOD to 0.13
            ),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metricsV2, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metricsV2 = metricsV2,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertEquals("Recorrente em bebidas", presentation.profileTitle)
        assertEquals("PadrÃ£o de consumo consistente", presentation.confidenceLabel)
        assertEquals("Modelo treinado", presentation.sourceLabel)
        assertNull(presentation.sourceWarning)
        assertTrue(presentation.technicalItems.any { it.first == "Modelo" && it.second == "XGBoost V2 Top 15" })
        assertTrue(presentation.technicalItems.any { it.first == "Features enviadas ao modelo" && it.second.contains("15") })
        assertTrue(presentation.technicalItems.any { it.first == "VersÃ£o de entrada" && it.second == "v2" })
        assertTrue(presentation.technicalItems.any { it.first == "MÃ©tricas internas" && it.second.contains("64") })
        assertTrue(presentation.technicalItems.any { it.first == "Tipo de interpretaÃ§Ã£o" })
        assertTrue(presentation.technicalItems.any { it.first == "InferÃªncia (ms)" })
        assertTrue(presentation.technicalItems.any { it.first == "Itens classificados" })
        assertTrue(presentation.technicalItems.any { it.first == "OTHER por valor" })
        assertTrue(presentation.technicalItems.none { it.first == "Aviso tÃ©cnico" })
        assertTrue(presentation.technicalItems.any { it.first.startsWith("Feature ") })
    }

    @Test
    fun toHomeAnalysisPresentation_showsFallbackWarningWhenSourceIsLocalFallback() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "SABAO", price = 5.0, category = ProductCategory.CLEANING)
                )
            )
        )
        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = ConsumptionModelInput(features = mapOf("total_receipts" to 1.0))
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
            confidence = 0.4,
            profileScores = mapOf(ConsumptionBehaviorProfile.UNDEFINED to 1.0),
            source = BehaviorClassificationSource.RULE_BASED_FALLBACK
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metricsV2, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metricsV2 = metricsV2,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertNotNull(presentation.sourceWarning)
        assertTrue(presentation.sourceWarning!!.contains("Backend indisponÃ­vel"))
        assertEquals("PadrÃ£o de consumo variado", presentation.confidenceLabel)
    }

    @Test
    fun toHomeAnalysisPresentation_addsTechnicalWarningWhenClassifiedCoverageIsLow() {
        val receipts = listOf(
            Receipt(
                source = ReceiptSource.QR_CODE,
                items = listOf(
                    ProductItem(name = "ITEM DESCONHECIDO", price = 20.0, category = ProductCategory.OTHER),
                    ProductItem(name = "ARROZ", price = 10.0, category = ProductCategory.BASIC_FOOD)
                )
            )
        )

        val metricsV2 = CalculateConsumptionMetricsV2UseCase(
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            semanticTagger = KeywordProductSemanticTagger()
        )(receipts)
        val modelInput = BuildConsumptionModelInputUseCase()(metricsV2)
        val behaviorResult = ConsumptionBehaviorResult(
            mainProfile = ConsumptionBehaviorProfile.UNDEFINED,
            confidence = 0.55,
            profileScores = mapOf(ConsumptionBehaviorProfile.UNDEFINED to 1.0),
            source = BehaviorClassificationSource.TRAINED_MODEL
        )
        val behaviorAnalysis = DefaultConsumptionInsightsEngine().generate(metricsV2, behaviorResult)

        val presentation = StoredConsumptionAnalysis(
            receipts = receipts,
            metricsV2 = metricsV2,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        ).toHomeAnalysisPresentation()

        assertTrue(presentation.technicalItems.any {
            it.first == "Aviso tÃ©cnico" &&
                it.second.contains("muitos itens nÃ£o classificados")
        })
    }
}

```

