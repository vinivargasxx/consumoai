# ConsumoAI source export

Generated: 2026-05-14 01:27:12 (V1.1 with improved classifier)  

Include tests: True  

Total files: 71 (Updated after cleanup)

**RECENT UPDATES (2026-05-14):**
- KeywordProductClassifierDataSource: Expanded to 250+ keywords with 10 special rule functions (alcoholic, energy, hygiene, cleaning, etc.)
- NfceHtmlParserDataSource: Removed debug logging
- GenerateOtherItemsReportUseCase: Removed from DomainModule, AppModule, and HomeViewModel
- Classification V1.1: Improved from ~75% to ~98% expected coverage with reduced OTHER items

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
import com.example.consumoai.data.classifier.RemoteConsumptionBehaviorClassifier
import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.data.datasource.ocr.MlKitOcrDataSource
import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.data.local.AppDatabase
import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.data.parser.ReceiptLayoutParserDataSource
import com.example.consumoai.data.repository.ReceiptRepositoryImpl
import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.classifier.ProductClassifier
import com.example.consumoai.domain.repository.ReceiptRepository
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
        MlKitOcrDataSource()
    }

    single {
        ReceiptLayoutParserDataSource()
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

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.MODEL_API_BASE_URL)
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
import com.example.consumoai.domain.usecase.AnalyzeReceiptFromOcrUseCase
import com.example.consumoai.domain.usecase.AnalyzeReceiptFromQrCodeUrlUseCase
import com.example.consumoai.domain.usecase.AnalyzeReceiptWithFallbackUseCase
import com.example.consumoai.domain.usecase.AnalyzeStoredReceiptsUseCase
import com.example.consumoai.domain.usecase.BuildConsumptionModelInputUseCase
import com.example.consumoai.domain.usecase.CalculateConsumptionMetricsUseCase
import com.example.consumoai.domain.usecase.ClearReceiptsUseCase
import com.example.consumoai.domain.usecase.ClassifyConsumptionProfileUseCase
import com.example.consumoai.domain.usecase.ClassifyProductsUseCase
import com.example.consumoai.domain.usecase.GetStoredReceiptsSummaryUseCase
import com.example.consumoai.domain.usecase.ImportSampleNfceReceiptsUseCase
import com.example.consumoai.domain.usecase.SaveReceiptUseCase
import org.koin.dsl.module

val domainModule = module {

    factory {
        AnalyzeReceiptFromOcrUseCase(
            mlKitOcrDataSource = get(),
            receiptLayoutParserDataSource = get(),
            classifyProductsUseCase = get()
        )
    }

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

    factory {
        ClassifyConsumptionProfileUseCase(
            consumptionBehaviorClassifier = get()
        )
    }

    factory {
        AnalyzeReceiptWithFallbackUseCase(
            analyzeReceiptFromQrCodeUrlUseCase = get(),
            analyzeReceiptFromOcrUseCase = get()
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
            calculateConsumptionMetricsUseCase = get(),
            buildConsumptionModelInputUseCase = get(),
            classifyConsumptionProfileUseCase = get(),
            insightsEngine = get()
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
        "ENERGETICO", "ENERG", "MONSTER",
        // Alcoholic beverages
        "CERVEJA", "CHOPP", "IPA", "PALE ALE", "ALE", "LAGER", "PILSEN", "PILSNER",
        "BADEN BADEN", "BLUE MOON", "KAISERDOM", "TUPINIQUIM", "ROLETA RUSSA",
        "VINHO", "VH", "CONCHA Y TORO", "AURORA", "C SAUV", "CARM", "RESERVADO",
        // Generic
        "BEB", "BEBIDA"
    )

    private val industrializedKeywords = listOf(
        // Chocolates, biscuits, snacks, etc.
        "CHOCOLATE", "CHOC", "TRENTO", "LACTA", "KITKAT", "KINDER", "OREO",
        "BISCOITO", "BISC", "BOLACHA", "COOKIE", "WAFER",
        "TORTIN", "MOUSSE", "ANA MARIA", "BAUDUCCO", "ISABELA",
        "AVELA", "AVELAS", "CACAU",
        "SALGADINHO", "SALG", "SNACK",
        "DORITOS", "FANDANGOS", "CHEETOS", "SALTLETTS", "BREZEL",
        "CONGELADO", "LASANHA", "PIZZA", "WRAP", "TORTILHA",
        "NUGGET", "BATATA PALHA",
        "TEMPERO", "SAZON", "MAIONESE", "KETCHUP", "CATCHUP", "MOSTARDA",
        "ACHOCOLATADO", "CEREAL", "GRANOLA", "PIPOCA", "PIPOCA MIC",
        "NISSIN", "MAC NISSIN", "LAMEN", "MIOJO",
        "BARRA", "DOCE", "BALA", "BALAS", "HALLS", "GOMA", "SORVETE", "PICOLE",
        "BOLO", "BOLO MARMORE", "ROSCA", "ROSCA POLV"
    )

    private val basicFoodKeywords = listOf(
        "ARROZ", "FEIJAO", "MASSA", "MACARRAO", "ESPAGUETE", "FARINHA",
        "ACUCAR", "OLEO", "SAL", "SAL REFINADO", "SAL IODADO",
        "CAFE",
        "LEITE", "IOGURTE", "IOG", "BATIDO",
        "QUEIJO", "QJO", "MUSSARELA", "MUCARELA", "MOZZARELLA",
        "REQUEIJAO", "MANTEIGA", "MARGARINA",
        "PAO", "CACETINHO", "BISNAGA",
        "CARNE", "FRANGO", "PEITO", "PATINHO", "COXAO", "COXAO DENTRO",
        "CARNE MOIDA", "MOIDA", "SALSICHA", "LINGUICA",
        "OVO", "OVOS", "LOMBO", "LOMBO COZ", "PRESUNTO", "MORTADELA", "MORT",
        "ATUM", "SARDINHA",
        "MOLHO", "MOL", "MOLHO TOMATE", "PASSATA", "EXTRATO",
        "TOMATE S PELE", "TOMATE PELADO",
        "MEL", "AVEIA", "MILHO VERDE", "AMENDOIM"
    )

    private val produceKeywords = listOf(
        "BANANA", "MACA", "MAMAO", "LARANJA", "MORANGO", "UVA", "LIMAO", "ABACAXI",
        "MELANCIA", "MELAO", "PERA",
        "TOMATE", "BATATA", "CENOURA", "CEBOLA", "ALFACE", "PIMENTAO",
        "VERDURA", "LEGUME",
        "BROCOLIS", "COUVE", "REPOLHO", "CEBOLINHA", "ALHO",
        "PEPINO", "ABOBRINHA", "BERINJELA", "MANDIOCA", "AIPIM", "INHAME",
        "GRANEL"
    )

    private val hygieneKeywords = listOf(
        "PAPEL HIGIENICO", "P H", "HIGIENICO",
        "SABONETE", "SAB", "DOVE",
        "SHAMPOO", "SH", "HEAD", "SHOULDERS", "HEAD SHOULDERS", "CLEAR", "CONDICIONADOR",
        "CREME DENTAL", "CR D", "PASTA DENTAL", "COLGATE",
        "ESCOVA DENTAL", "FIO DENTAL",
        "DESODORANTE", "DES", "REXONA",
        "ABSORVENTE", "FRALDA", "HUGGIES",
        "ALGODAO", "COTONETE", "BARBEAR", "GILLETTE", "CARGA GILLETTE",
        "ENXAGUANTE",
        "LENCO UMEDECIDO", "LENCO UMED", "TOALHA UMED", "TOALHA UMEDECIDA",
        "PRESERV", "PRESERVATIVO", "OLLA"
    )

    private val cleaningKeywords = listOf(
        "DETERGENTE", "DET LQ", "LAV LOUCA", "LAVA LOUCA",
        "DESINFETANTE", "DESINF", "PINHO SOL", "KALIPTO",
        "AGUA SANITARIA", "SANITARIA", "CLORO",
        "LAVA ROUPAS", "L ROUP", "ROUP PO", "OMO",
        "SABAO", "SABAO PO",
        "AMACIANTE", "AMAC", "COMFORT", "SPLENDO",
        "ALVEJANTE",
        "LIMPADOR", "LIMP", "LIMP PISO", "AJAX", "DESTAC",
        "MULTIUSO", "VEJA", "YPE",
        "ESPONJA", "ESP ESFREBOM", "ESFREBOM", "BOMBRIL",
        "SAPONACEO", "DESENGORDURANTE", "LIMPEZA",
        "SACO LIXO", "LIXO",
        "P TOALHA", "PAPEL TOALHA", "TOALHA PAPEL", "FILTRO PAPEL"
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
            matchesAny(normalized, produceKeywords) -> ProductCategory.PRODUCE
            else -> ProductCategory.OTHER
        }

        return item.copy(category = category)
    }

    override fun classifyAll(items: List<ProductItem>): List<ProductItem> {
        val classified = items.map(::classify)
        logClassificationSummary(classified)
        return classified
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
        return keywords.any { keyword -> normalized.contains(keyword) }
    }

    // Special rule functions
    private fun isAlcoholicBeverage(normalized: String): Boolean {
        return matchesAny(normalized, listOf(
            "CHOPP", "IPA", "PALE ALE", "ALE", "LAGER", "PILSEN", "PILSNER",
            "BLUE MOON", "BADEN BADEN", "KAISERDOM", "TUPINIQUIM", "ROLETA RUSSA",
            "VINHO", "VH", "AURORA", "CONCHA Y TORO", "C SAUV", "CARM", "RESERVADO"
        ))
    }

    private fun isEnergyDrink(normalized: String): Boolean {
        return matchesAny(normalized, listOf("ENERG", "MONSTER"))
    }

    private fun isPersonalHygiene(normalized: String): Boolean {
        if (normalized.contains("P TOALHA") || (normalized.contains("TOALHA PAPEL") && !normalized.contains("UMED"))) {
            return false
        }

        return matchesAny(normalized, listOf(
            "SH", "SHAMPOO", "HEAD", "CLEAR", "DOVE", "REXONA", "GILLETTE", "COLGATE",
            "CR D", "SAB", "DES", "PRESERV", "OLLA", "LENCO UMED", "TOALHA UMED",
            "HUGGIES", "P H"
        ))
    }

    private fun isHouseCleaning(normalized: String): Boolean {
        return matchesAny(normalized, listOf(
            "DET LQ", "LAV LOUCA", "LAVA LOUCA", "DESINF", "L ROUP", "OMO",
            "AMAC", "COMFORT", "SPLENDO", "LIMP", "AJAX", "PINHO SOL", "KALIPTO",
            "ESFREBOM", "SACO LIXO", "P TOALHA", "PAPEL TOALHA", "TOALHA PAPEL", "FILTRO PAPEL"
        ))
    }

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

    private fun isFreshProduce(normalized: String): Boolean {
        return matchesAny(normalized, listOf(
            "PIMENTAO", "CEBOLINHA", "ALHO", "MELANCIA",
            "BANANA", "MACA", "MORANGO", "UVA", "LARANJA"
        )) || (normalized.contains("TOMATE") && !isTomatoProcessed(normalized))
    }

    private fun isMeat(normalized: String): Boolean {
        if (normalized.contains("SAZON") || normalized.contains("TEMPERO") || normalized.contains("TEMP")) {
            return false
        }

        return matchesAny(normalized, listOf(
            "PATINHO", "COXAO", "CARNE", "FRANGO", "LOMBO", "MORT", "MORTADELA",
            "PRESUNTO", "SALSICHA", "LINGUICA"
        ))
    }

    private fun isDairy(normalized: String): Boolean {
        return matchesAny(normalized, listOf(
            "LEITE", "IOGURTE", "IOG", "QUEIJO", "QJO", "MUSSARELA",
            "MUCARELA", "MOZZARELLA", "REQUEIJAO", "MANTEIGA", "MARGARINA"
        ))
    }

    private fun isFrozenOrConvenienceFood(normalized: String): Boolean {
        return matchesAny(normalized, listOf("PIZZA", "LASANHA", "NISSIN", "MAC NISSIN", "PIPOCA MIC", "WRAP", "TORTILHA"))
    }

    private fun isSnackOrSweet(normalized: String): Boolean {
        return matchesAny(normalized, listOf(
            "SALG", "DORITOS", "FANDANGOS", "CHEETOS", "TRENTO",
            "BALA", "HALLS", "CHOC", "BOLO", "ROSCA"
        ))
    }

    // Legacy functions
    private fun isMilkOrDairy(normalized: String): Boolean = isDairy(normalized)
    private fun isShelfStableTomato(normalized: String): Boolean = isTomatoProcessed(normalized)
    private fun isFreshTomato(normalized: String): Boolean = normalized.contains("TOMATE") && !isTomatoProcessed(normalized)
    private fun isBread(normalized: String): Boolean = matchesAny(normalized, listOf("PAO", "CACETINHO", "BISNAGA"))
    private fun isMeatOrProtein(normalized: String): Boolean = isMeat(normalized) || matchesAny(normalized, listOf("OVO", "OVOS"))
    private fun isSoftDrinkOrJuice(normalized: String): Boolean = matchesAny(normalized, listOf("COCA", "COCA COLA", "REFRIGERANTE", "GUARANA", "FANTA", "SPRITE", "PEPSI", "SUCO", "NECTAR", "NATURALE", "DEL VALLE"))
    private fun isChocolateOrSnack(normalized: String): Boolean = isSnackOrSweet(normalized) || matchesAny(normalized, listOf("CHOCOLATE", "BISC", "COOKIE", "WAFER", "KITKAT", "KINDER", "LACTA", "OREO", "BAUDUCCO", "ISABELA", "ANA MARIA", "SNACK", "SALGADINHO"))

    private fun logClassificationSummary(items: List<ProductItem>) {
        // Logging desabilitado por enquanto
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
data class ModelPredictionResponseDto(
    val main_profile: String,
    val confidence: Double,
    val profile_scores: Map<String, Double>
)
```

## FILE: app/src/main/java/com/example/consumoai/data/classifier/RemoteConsumptionBehaviorClassifier.kt

```kotlin
package com.example.consumoai.data.classifier

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

class RemoteConsumptionBehaviorClassifier(
    private val api: ConsumptionModelApi,
    private val fallbackClassifier: RuleBasedConsumptionBehaviorClassifier
) : ConsumptionBehaviorClassifier {

    override suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult {
        return try {
            val response = api.predict(
                ModelPredictionRequestDto(
                    version = input.version,
                    features = input.features
                )
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
                source = BehaviorClassificationSource.TRAINED_MODEL
            )
        } catch (_: Exception) {
            fallbackClassifier.classify(input)
        }
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
 * Temporary rule-based implementation until a trained model replaces it.
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
        val totalReceipts = input.feature("total_receipts")
        val classifiedItemsPercentage = input.feature("classified_items_percentage")
        val categoryConcentrationIndex = input.feature("category_concentration_index")
        val convenienceScore = input.feature("convenience_score")
        val essentialScore = input.feature("essential_score")
        val diversityScore = input.feature("diversity_score")
        val nonEssentialPercentage = input.feature("non_essential_categories_percentage")
        val beveragesValuePercentage = input.feature("beverages_value_pct")
        val beveragesFrequency = input.feature("beverages_frequency")
        val produceValuePercentage = input.feature("produce_value_pct")
        val produceFrequency = input.feature("produce_frequency")
        val householdMaintenanceValue = input.feature("hygiene_value_pct") + input.feature("cleaning_value_pct")

        return when {
            totalReceipts <= 0.0 -> ConsumptionBehaviorProfile.UNDEFINED
            classifiedItemsPercentage < 0.50 -> ConsumptionBehaviorProfile.UNDEFINED
            categoryConcentrationIndex >= 0.70 -> ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED
            nonEssentialPercentage >= 0.75 && convenienceScore >= 0.55 -> ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION
            convenienceScore >= 0.60 -> ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED
            beveragesValuePercentage >= 0.25 && beveragesFrequency >= 0.50 -> ConsumptionBehaviorProfile.BEVERAGE_RECURRENT
            essentialScore >= 0.60 -> ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED
            householdMaintenanceValue >= 0.25 -> ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE
            produceValuePercentage <= 0.05 && produceFrequency <= 0.20 -> ConsumptionBehaviorProfile.LOW_FRESH_FOOD
            diversityScore >= 0.55 && categoryConcentrationIndex < 0.45 -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
            else -> ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED
        }
    }

    private fun ConsumptionModelInput.feature(name: String): Double {
        return features[name] ?: 0.0
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/data/datasource/ocr/MlKitOcrDataSource.kt

```kotlin
package com.example.consumoai.data.datasource.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlKitOcrDataSource {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)


    suspend fun extractElements(bitmap: Bitmap): List<OcrElement> {
        val image = InputImage.fromBitmap(bitmap, 0)
        val result = recognizer.process(image).await()

        Log.d("OCR_RAW_TEXT", result.text)

        val elements = result.textBlocks
            .flatMap { block -> block.lines }
            .flatMap { line -> line.elements }
            .mapNotNull { element ->
                val box = element.boundingBox ?: return@mapNotNull null
                OcrElement(
                    text = element.text,
                    left = box.left,
                    top = box.top,
                    right = box.right,
                    bottom = box.bottom,
                    centerX = (box.left + box.right) / 2,
                    centerY = (box.top + box.bottom) / 2
                )
            }
            .sortedWith(compareBy({ it.top }, { it.left }))

        Log.d(
            "OCR_ELEMENTS",
            elements.joinToString("\n") {
                "${it.text} | x=${it.left}-${it.right}, y=${it.top}-${it.bottom}, cx=${it.centerX}, cy=${it.centerY}"
            }
        )

        return elements
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/data/datasource/ocr/OcrElement.kt

```kotlin
package com.example.consumoai.data.datasource.ocr

data class OcrElement(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val centerX: Int,
    val centerY: Int
)

```

## FILE: app/src/main/java/com/example/consumoai/data/datasource/qrcode/NfceQrCodeDataSource.kt

```kotlin
package com.example.consumoai.data.datasource.qrcode

import android.util.Log
import com.example.consumoai.data.parser.NfceHtmlParserDataSource
import com.example.consumoai.domain.model.ProductItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class NfceQrCodeDataSource(
    private val nfceHtmlParserDataSource: NfceHtmlParserDataSource
) {

    suspend fun extractProducts(url: String): List<ProductItem> = withContext(Dispatchers.IO) {
        val document = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android)")
            .timeout(20_000)
            .get()

        Log.d("NFCE_HTML", document.html().take(3000))
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
import org.jsoup.nodes.Document
import java.util.Locale

class NfceHtmlParserDataSource {

    private val moneyRegex = Regex("(\\d{1,4}[,.]\\d{2})")
    private val rowRegex = Regex("^(\\d{1,3})\\s+(.+?)\\s+(\\d{1,4}[,.]\\d{2})$")
    private val forbiddenLineTokens = setOf(
        "QTD TOTAL", "VALOR TOTAL", "VALOR PAGO", "FORMA DE PAGAMENTO", "CONSUMIDOR", "CHAVE", "PROTOCOLO"
    )

    fun parse(document: Document): List<ProductItem> {
        val products = parseTabResultTable(document)
            .ifEmpty { parseByRows(document) }
            .ifEmpty {
            parseFallbackFromText(document.body()?.text().orEmpty())
            }
            .distinctBy { "${it.itemNumber}|${it.name}|${formatPrice(it.price)}" }
            .sortedBy { it.itemNumber ?: Int.MAX_VALUE }


        return products
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

## FILE: app/src/main/java/com/example/consumoai/data/parser/ReceiptLayoutParserDataSource.kt

```kotlin
package com.example.consumoai.data.parser

import android.util.Log
import com.example.consumoai.data.datasource.ocr.OcrElement
import com.example.consumoai.domain.model.ProductItem
import java.text.Normalizer
import java.util.Locale

class ReceiptLayoutParserDataSource {

    private companion object {
        const val ITEM_BLOCK_TOLERANCE = 20
        const val ABSOLUTE_ITEM_NUMBER_MAX_X = 500
    }

    private val itemNumberRegex = Regex("^\\d{3}$")
    private val priceRegex = Regex("^\\d{1,4}[,.]\\d{1,2}$")
    private val quantityRegex = Regex("^\\d+[,.]\\d{3}$")
    private val longCodeRegex = Regex("^\\d{5,}$")
    private val ignoredDescriptionTokens = setOf("UN", "UNX", "KG", "G", "ML", "L", "X", "QTD", "TOTAL", "VALOR", "RS")
    private val footerMarkers = setOf(
        "QTD", "TOTAL", "VALOR", "PAGAMENTO", "CONSUMIDOR", "CHAVE",
        "PROTOCOLO", "AUTORIZACAO", "AUTORIZAÃ‡ÃƒO", "SERIE", "SÃ‰RIE", "EMISSAO", "EMISSÃƒO"
    )
    private val ocrFixMap = mapOf(
        "T03" to "IOG",
        "8ISC" to "BISC",
        "HOLHO" to "MOLHO",
        "A0" to "AO"
    )

    private data class ItemAnchor(
        val itemNumber: Int,
        val centerY: Int,
        val element: OcrElement
    )

    private data class ItemBlock(
        val itemNumber: Int,
        val startY: Int,
        val endY: Int,
        val elements: List<OcrElement>
    )

    private data class NormalizedPrice(
        val token: String,
        val precision: Int
    )

    private data class PriceCandidate(
        val token: String,
        val precision: Int,
        val centerY: Int,
        val centerX: Int
    )

    fun parseProducts(elements: List<OcrElement>): List<ProductItem> {
        if (elements.isEmpty()) return emptyList()

        val sorted = elements.sortedWith(compareBy({ it.centerY }, { it.centerX }))
        val tableStartY = findTableStartY(sorted)
        val tableEndY = findTableEndY(sorted, tableStartY)
        val footerFound = hasFooterMarkers(sorted, tableStartY)
        debugLog("TABLE_AREA", "startY=$tableStartY endY=$tableEndY")

        if (tableStartY >= tableEndY) return emptyList()

        val tableElements = sorted.filter { it.centerY >= tableStartY && it.centerY < tableEndY }
        val receiptWidth = tableElements.maxOfOrNull { it.right } ?: sorted.maxOfOrNull { it.right } ?: 1
        val anchors = findItemAnchors(tableElements)

        debugLog(
            "ITEM_NUMBERS_FOUND",
            anchors.joinToString("\n") { anchor ->
                "${anchor.itemNumber.toString().padStart(3, '0')} | x=${anchor.element.centerX} y=${anchor.centerY}"
            }
        )

        if (anchors.isEmpty()) return emptyList()

        val blocks = buildItemBlocks(anchors, tableEndY, tableElements, footerFound)
        val products = blocks.mapNotNull { block ->
            debugLog(
                "ITEM_BLOCK",
                "item=${block.itemNumber} y=${block.startY}-${block.endY} elements=${block.elements.size}"
            )
            buildProduct(block, receiptWidth)
        }.sortedBy { it.itemNumber }

        debugLog("PARSER_FINAL_ITEMS", products.joinToString("\n") { formatItem(it) })
        return products
    }

    private fun findTableStartY(elements: List<OcrElement>): Int {
        val first001 = elements
            .filter { isItemNumberElement(it) }
            .filter { normalizeToken(it.text) == "001" }
            .minByOrNull { it.centerY }

        return first001?.centerY
            ?: elements.filter { isItemNumberElement(it) }.minOfOrNull { it.centerY }
            ?: elements.minOf { it.centerY }
    }

    private fun findTableEndY(elements: List<OcrElement>, tableStartY: Int): Int {
        return elements
            .filter { element ->
                element.centerY > tableStartY && isFooterMarker(element.text)
            }
            .minOfOrNull { it.centerY }
            ?: (elements.maxOfOrNull { it.centerY }?.plus(1) ?: Int.MAX_VALUE)
    }

    private fun hasFooterMarkers(elements: List<OcrElement>, tableStartY: Int): Boolean {
        return elements.any { it.centerY > tableStartY && isFooterMarker(it.text) }
    }

    private fun isFooterMarker(text: String): Boolean {
        val token = normalizeWordOnly(text)
        return footerMarkers.any { marker -> token == marker || token.contains(marker) }
    }

    private fun findItemAnchors(elements: List<OcrElement>): List<ItemAnchor> {
        return elements
            .filter { isItemNumberElement(it) }
            .mapNotNull { element ->
                val itemNumber = normalizeToken(element.text).toIntOrNull() ?: return@mapNotNull null
                ItemAnchor(itemNumber = itemNumber, centerY = element.centerY, element = element)
            }
            .groupBy { it.itemNumber }
            .mapNotNull { (_, anchors) -> anchors.minByOrNull { it.centerY } }
            .sortedWith(compareBy({ it.centerY }, { it.element.centerX }))
    }

    private fun buildItemBlocks(
        anchors: List<ItemAnchor>,
        tableEndY: Int,
        tableElements: List<OcrElement>,
        footerFound: Boolean
    ): List<ItemBlock> {
        return anchors.mapIndexed { index, anchor ->
            val nextStartY = anchors.getOrNull(index + 1)?.centerY
            val upperBound = when {
                nextStartY != null -> nextStartY - ITEM_BLOCK_TOLERANCE
                footerFound -> tableEndY - ITEM_BLOCK_TOLERANCE
                else -> tableEndY
            }
            val blockElements = tableElements.filter { element ->
                element.centerY >= anchor.centerY - ITEM_BLOCK_TOLERANCE &&
                    element.centerY < upperBound
            }

            ItemBlock(
                itemNumber = anchor.itemNumber,
                startY = anchor.centerY,
                endY = nextStartY ?: tableEndY,
                elements = blockElements
            )
        }
    }

    private fun isItemNumberElement(element: OcrElement): Boolean {
        if (element.centerX >= ABSOLUTE_ITEM_NUMBER_MAX_X) return false
        val token = normalizeToken(element.text)
        if (!itemNumberRegex.matches(token)) return false
        return token.toIntOrNull() in 1..999
    }

    private fun buildProduct(block: ItemBlock, receiptWidth: Int): ProductItem? {
        val price = extractPrice(block.elements, receiptWidth) ?: return null
        val name = extractName(block.elements, receiptWidth)
        if (name.isBlank()) return null

        return ProductItem(
            itemNumber = block.itemNumber,
            name = name,
            price = price
        )
    }

    private fun extractPrice(elements: List<OcrElement>, receiptWidth: Int): Double? {
        val candidates = elements.mapNotNull { element ->
            normalizePrice(element.text)?.let { price ->
                PriceCandidate(
                    token = price.token,
                    precision = price.precision,
                    centerY = element.centerY,
                    centerX = element.centerX
                )
            }
        }

        if (candidates.isEmpty()) return null

        val totalPriceMinX = (receiptWidth * 0.90).toInt().coerceAtLeast((receiptWidth * 0.82).toInt())
        val rightColumnCandidates = candidates
            .filter { it.centerX >= totalPriceMinX }
            .sortedWith(compareBy<PriceCandidate> { it.centerY }.thenBy { it.centerX }.thenByDescending { it.precision })

        val selected = rightColumnCandidates.firstOrNull()
            ?: candidates.maxWithOrNull(compareBy<PriceCandidate> { it.centerX }.thenByDescending { it.precision })
            ?: return null
        return selected.token.replace(',', '.').toDoubleOrNull()
    }

    private fun normalizePrice(value: String): NormalizedPrice? {
        var token = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .uppercase(Locale.ROOT)
            .replace("R$", "")
            .replace(Regex("[^0-9,.)]"), "")
            .trim()

        if (token.isBlank()) return null

        if (token.endsWith(")")) {
            token = token.removeSuffix(")")
            if (token.isNotEmpty() && token.last().isDigit()) {
                token += "0"
            }
        }

        val separatorIndex = token.indexOfFirst { it == ',' || it == '.' }
        if (separatorIndex <= 0 || separatorIndex >= token.lastIndex) return null

        val separator = token[separatorIndex]
        val wholePart = token.substring(0, separatorIndex)
        val decimalPart = token.substring(separatorIndex + 1)
        if (!priceRegex.matches("$wholePart$separator$decimalPart")) return null

        val normalizedDecimal = when (decimalPart.length) {
            1 -> decimalPart + "0"
            2 -> decimalPart
            else -> return null
        }

        return NormalizedPrice(token = "$wholePart$separator$normalizedDecimal", precision = normalizedDecimal.length)
    }

    private fun extractName(elements: List<OcrElement>, receiptWidth: Int): String {
        val descriptionMinX = (receiptWidth * 0.40).toInt().coerceAtMost((receiptWidth * 0.55).toInt())
        val descriptionMaxX = (receiptWidth * 0.90).toInt()

        return elements
            .asSequence()
            .filter { it.centerX in descriptionMinX until descriptionMaxX }
            .sortedWith(compareBy({ it.centerY }, { it.centerX }))
            .mapNotNull { element ->
                val token = normalizeDescriptionToken(element.text)
                if (shouldKeepDescriptionToken(token)) token else null
            }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun shouldKeepDescriptionToken(token: String): Boolean {
        if (token.isBlank()) return false
        if (itemNumberRegex.matches(token)) return false
        if (priceRegex.matches(token)) return false
        if (longCodeRegex.matches(token)) return false
        if (quantityRegex.matches(token)) return false
        if (ignoredDescriptionTokens.contains(token)) return false
        if (!token.any { it.isLetterOrDigit() }) return false
        return true
    }

    private fun normalizeDescriptionToken(value: String): String {
        val normalized = normalizeToken(value).trim('.', ',')
        return ocrFixMap[normalized] ?: normalized
    }

    private fun normalizeWordOnly(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return normalized.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9]"), "")
    }

    private fun normalizeToken(value: String): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return normalized.uppercase(Locale.ROOT).replace(Regex("[^A-Z0-9,.-]"), "")
    }

    private fun formatItem(item: ProductItem): String {
        return "${item.itemNumber?.toString()?.padStart(3, '0')}. ${item.name} - R$ ${"%.2f".format(item.price)}"
    }

    private fun debugLog(tag: String, message: String) {
        runCatching { Log.d(tag, message) }
    }
}
```

## FILE: app/src/main/java/com/example/consumoai/data/remote/dto/ModelPredictionDtos.kt

```kotlin
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

## FILE: app/src/main/java/com/example/consumoai/domain/insights/ConsumptionInsightsEngine.kt

```kotlin
package com.example.consumoai.domain.insights

import com.example.consumoai.domain.model.ConsumptionBehaviorAnalysis
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionMetrics

interface ConsumptionInsightsEngine {
    fun generate(
        metrics: ConsumptionMetrics,
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
import com.example.consumoai.domain.model.InsightSeverity
import com.example.consumoai.domain.model.InsightType
import com.example.consumoai.domain.model.ProductCategory

class DefaultConsumptionInsightsEngine : ConsumptionInsightsEngine {

    override fun generate(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis {
        val insights = mutableListOf<ConsumptionInsight>()

        // Generate primary insights based on metrics and model scores
        generateRecurrenceInsights(metrics, insights)
        generateDiversityInsights(metrics, insights)
        generateConcentrationInsights(metrics, insights)
        generateCategoryDominanceInsights(metrics, insights)
        generateFreshFoodInsights(metrics, insights)
        generateBalanceInsights(metrics, insights)
        generateHybridBehaviorInsights(result, insights)
        generateConfidenceInsights(result, insights)

        // Generate behavioral composition from profile scores
        val composition = generateBehavioralComposition(result)

        // Generate text summary
        val summary = generateSummary(result, metrics, composition)

        return ConsumptionBehaviorAnalysis(
            behaviorResult = result,
            insights = insights,
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
                    title = "Consumo concentrado",
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
                    description = "Os produtos industrializados representam parcela relevante das compras.",
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
                    description = "As compras possuem baixa recorrÃªncia de hortifruti e alimentos frescos.",
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
                    description = "O consumo apresenta caracterÃ­sticas relevantes de mÃºltiplos perfis comportamentais.",
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
                append(" O padrÃ£o geral demonstra recorrÃªncia de itens em poucas categorias.")
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
 * Temporary app-facing output until the trained model replaces the rule-based classifier.
 */
enum class ConsumptionBehaviorProfile {
    CONVENIENCE_ORIENTED,
    ESSENTIAL_FOCUSED,
    DIVERSIFIED_BALANCED,
    BEVERAGE_RECURRENT,
    LOW_FRESH_FOOD,
    HOUSEHOLD_MAINTENANCE,
    HIGHLY_CONCENTRATED,
    IMPULSIVE_CONSUMPTION,
    UNDEFINED
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionBehaviorResult.kt

```kotlin
package com.example.consumoai.domain.model

data class ConsumptionBehaviorResult(
    val mainProfile: ConsumptionBehaviorProfile,
    val confidence: Double,
    val profileScores: Map<ConsumptionBehaviorProfile, Double>,
    val source: BehaviorClassificationSource
)

enum class BehaviorClassificationSource {
    TRAINED_MODEL,
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

    // Features oficiais aproveitadas pela IA V1
    val convenienceScore: Double,
    val essentialScore: Double,
    val diversityScore: Double
)
```

## FILE: app/src/main/java/com/example/consumoai/domain/model/ConsumptionModelInput.kt

```kotlin
package com.example.consumoai.domain.model

const val MODEL_INPUT_VERSION = "v1"

/**
 * Official feature payload consumed by the behavior classifier and, in the future,
 * by the trained model integrated into the app.
 */
data class ConsumptionModelInput(
    val version: String = MODEL_INPUT_VERSION,
    val features: Map<String, Double>
)

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
    OCR,
    QR_CODE
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/model/StoredConsumptionAnalysis.kt

```kotlin
package com.example.consumoai.domain.model

data class StoredConsumptionAnalysis(
    val receipts: List<Receipt>,
    val metrics: ConsumptionMetrics,
    val modelInput: ConsumptionModelInput,
    val behaviorResult: ConsumptionBehaviorResult,
    val behaviorAnalysis: ConsumptionBehaviorAnalysis? = null
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

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/AnalyzeReceiptFromOcrUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.example.consumoai.data.datasource.ocr.MlKitOcrDataSource
import com.example.consumoai.data.parser.ReceiptLayoutParserDataSource
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource

class AnalyzeReceiptFromOcrUseCase(
    private val mlKitOcrDataSource: MlKitOcrDataSource,
    private val receiptLayoutParserDataSource: ReceiptLayoutParserDataSource,
    private val classifyProductsUseCase: ClassifyProductsUseCase
) {

    suspend operator fun invoke(bitmap: Bitmap): Receipt {
        val elements = mlKitOcrDataSource.extractElements(bitmap)
        val products = classifyProductsUseCase(receiptLayoutParserDataSource.parseProducts(elements))

        Log.d(
            "OCR_RESULT_PRODUCTS",
            products.joinToString("\n") {
                "${it.itemNumber?.toString()?.padStart(3, '0')}. ${it.name} - ${it.price}"
            }
        )

        return Receipt(
            source = ReceiptSource.OCR,
            items = products
        )
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/AnalyzeReceiptFromQrCodeUrlUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.datasource.qrcode.NfceQrCodeDataSource
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource

class AnalyzeReceiptFromQrCodeUrlUseCase(
    private val nfceQrCodeDataSource: NfceQrCodeDataSource,
    private val classifyProductsUseCase: ClassifyProductsUseCase
) {

    suspend operator fun invoke(url: String): Receipt {
        val products = classifyProductsUseCase(nfceQrCodeDataSource.extractProducts(url))
        return Receipt(
            accessKeyOrUrl = url,
            source = ReceiptSource.QR_CODE,
            items = products
        )
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/AnalyzeReceiptWithFallbackUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import android.graphics.Bitmap
import android.util.Log
import com.example.consumoai.domain.model.Receipt

class AnalyzeReceiptWithFallbackUseCase(
    private val analyzeReceiptFromQrCodeUrlUseCase: AnalyzeReceiptFromQrCodeUrlUseCase,
    private val analyzeReceiptFromOcrUseCase: AnalyzeReceiptFromOcrUseCase
) {

    suspend operator fun invoke(qrCodeUrl: String, fallbackBitmap: Bitmap): Receipt {
        return runCatching {
            analyzeReceiptFromQrCodeUrlUseCase(qrCodeUrl)
        }.getOrElse { qrError ->
            Log.d("QR_FALLBACK", "QR extraction failed, switching to OCR fallback: ${qrError.message}")
            analyzeReceiptFromOcrUseCase(fallbackBitmap)
        }.let { receipt ->
            if (receipt.items.isNotEmpty()) {
                receipt
            } else {
                Log.d("QR_FALLBACK", "QR extraction returned no items, switching to OCR fallback")
                analyzeReceiptFromOcrUseCase(fallbackBitmap)
            }
        }
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
    private val calculateConsumptionMetricsUseCase: CalculateConsumptionMetricsUseCase,
    private val buildConsumptionModelInputUseCase: BuildConsumptionModelInputUseCase,
    private val classifyConsumptionProfileUseCase: ClassifyConsumptionProfileUseCase,
    private val insightsEngine: ConsumptionInsightsEngine
) {

    suspend operator fun invoke(): StoredConsumptionAnalysis {
        val receipts = receiptRepository.getAllReceipts()
        if (receipts.isEmpty()) {
            throw IllegalStateException("Nenhuma nota armazenada para anÃ¡lise.")
        }
        val metrics = calculateConsumptionMetricsUseCase(receipts)
        val modelInput = buildConsumptionModelInputUseCase(metrics)
        val behaviorResult = classifyConsumptionProfileUseCase(modelInput)
        val behaviorAnalysis = insightsEngine.generate(metrics, behaviorResult)

        return StoredConsumptionAnalysis(
            receipts = receipts,
            metrics = metrics,
            modelInput = modelInput,
            behaviorResult = behaviorResult,
            behaviorAnalysis = behaviorAnalysis
        )
    }
}

```

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/BuildConsumptionModelInputUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ConsumptionModelInput
import com.example.consumoai.domain.model.ProductCategory

class BuildConsumptionModelInputUseCase {

    operator fun invoke(metrics: ConsumptionMetrics): ConsumptionModelInput {
        val valuePercentages = metrics.valuePercentageByCategory
        val frequencies = metrics.frequencyByCategory

        return ConsumptionModelInput(
            features = linkedMapOf(
                "total_receipts" to metrics.totalReceipts.toDouble(),
                "total_items" to metrics.totalItems.toDouble(),
                "total_value" to metrics.totalValue,
                "average_ticket" to metrics.averageTicket,
                "average_items_per_receipt" to metrics.averageItemsPerReceipt,

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

                "category_concentration_index" to metrics.categoryConcentrationIndex,
                "category_dominance_gap" to metrics.categoryDominanceGap,
                "category_diversity_index" to metrics.categoryDiversityIndex,
                "essential_categories_percentage" to metrics.essentialCategoriesPercentage,
                "non_essential_categories_percentage" to metrics.nonEssentialCategoriesPercentage,
                "convenience_score" to metrics.convenienceScore,
                "essential_score" to metrics.essentialScore,
                "diversity_score" to metrics.diversityScore,
                "classified_items_percentage" to metrics.classifiedItemsPercentage
            )
        )
    }
}

private fun Double?.orZero(): Double = this ?: 0.0

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

## FILE: app/src/main/java/com/example/consumoai/domain/usecase/ClassifyConsumptionProfileUseCase.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.ConsumptionBehaviorResult
import com.example.consumoai.domain.model.ConsumptionModelInput

/**
 * Classification temporÃ¡ria atÃ© integraÃ§Ã£o do modelo treinado.
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
            "https://dfe-portal.svrs.rs.gov.br/Dfe/QrCodeNFce?p=43251093015006000890651100007626041550452981%7C2%7C1%7C1%7C8FA68A81F0078C53F44444C8744E663DF5F90EB3"
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

    HomeScreen(
        uiState = uiState,
        onAction = viewModel::onAction
    )
}
```

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeScreen.kt

```kotlin
package com.example.consumoai.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.InsightSeverity
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.StoredConsumptionAnalysis
import com.example.consumoai.domain.model.StoredReceiptsSummary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAction: (HomeScreenAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ConsumoAI") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { onAction(HomeScreenAction.OnImportSampleNfceUrlsClick) },
                enabled = !uiState.isImporting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (uiState.isImporting) "Importando..." else "Importar notas NFC-e de teste")
            }

            Button(
                onClick = { onAction(HomeScreenAction.OnAnalyzeStoredReceiptsClick) },
                enabled = !uiState.isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (uiState.isAnalyzing) "Analisando..." else "Analisar notas armazenadas")
            }

            Button(
                onClick = { onAction(HomeScreenAction.OnClearReceiptsClick) },
                enabled = !uiState.isImporting && !uiState.isAnalyzing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Limpar notas locais")
            }

            ImportResultCard(uiState.importResult)
            StoredSummaryCard(uiState.localSummary)

            if (uiState.storedAnalysis != null) {
                BehaviorResultCard(uiState.storedAnalysis)
                BehavioralCompositionCard(uiState.storedAnalysis)
                InsightsCard(uiState.storedAnalysis)
                FutureModelInputCard(uiState.storedAnalysis)
                ConsumptionMetricsCard(uiState.storedAnalysis)
            }

            when {
                uiState.isImporting || uiState.isAnalyzing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportResultCard(importResult: ImportReceiptsResult?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Importação NFC-e", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (importResult == null) {
                Text("Nenhuma importação executada ainda")
                return@Column
            }

            Text("Importadas: ${importResult.importedCount}")
            Text("Ignoradas por duplicidade: ${importResult.skippedCount}")
            Text("Falhas: ${importResult.failedCount}")
        }
    }
}

@Composable
private fun StoredSummaryCard(summary: StoredReceiptsSummary?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumo local", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (summary == null) {
                Text("Sem resumo ainda")
                return@Column
            }

            Text("Total de notas armazenadas: ${summary.totalReceipts}")
            Text("Total de itens: ${summary.totalItems}")
            Text("Valor total: ${summary.totalValue.toCurrencyText()}")
        }
    }
}


@Composable
private fun BehaviorResultCard(analysis: StoredConsumptionAnalysis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resultado do modelo", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis == null) {
                Text("Nenhuma análise executada")
                return@Column
            }

            val result = analysis.behaviorResult
            Text("Perfil principal: ${result.mainProfile.toDisplayName()}")
            Text("Confiança: ${result.confidence.toPercentageText()}")
            Text("Origem: ${result.source.toDisplayName()}")
            Text("Descrição: ${result.mainProfile.toSimpleDescription()}")
            Text("Resumo técnico: ${buildTechnicalSummary(analysis)}")
            Spacer(modifier = Modifier.height(4.dp))
            if (result.source == BehaviorClassificationSource.RULE_BASED_FALLBACK) {
                Text("Resultado gerado por fallback local.")
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text("Probabilidades por perfil", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            result.profileScores
                .toList()
                .sortedByDescending { (_, score) -> score }
                .forEach { (profile, score) ->
                    Text("- ${profile.toDisplayName()}: ${score.toPercentageText()}")
                }
        }
    }
}

@Composable
private fun ConsumptionMetricsCard(analysis: StoredConsumptionAnalysis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Métricas gerais do app", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis == null) {
                Text("Nenhuma análise executada")
                return@Column
            }

            val metrics = analysis.metrics
            Text("Nem todas as métricas abaixo fazem parte da entrada oficial do modelo.")
            Spacer(modifier = Modifier.height(8.dp))
            MetricSection(
                title = "Resumo geral",
                items = listOf(
                    "Total de notas" to metrics.totalReceipts.toString(),
                    "Total de itens" to metrics.totalItems.toString(),
                    "Valor total" to metrics.totalValue.toCurrencyText(),
                    "Ticket médio" to metrics.averageTicket.toCurrencyText(),
                    "Média de itens por nota" to metrics.averageItemsPerReceipt.toCompactNumberText(),
                    "Valor médio por item" to metrics.averageValuePerItem.toCurrencyText()
                )
            )

            MetricSection(
                title = "Categorias dominantes",
                items = listOf(
                    "Categoria dominante por valor" to metrics.maxCategoryByValue.toDisplayName(),
                    "Categoria dominante por quantidade" to metrics.maxCategoryByItems.toDisplayName(),
                    "Índice de concentração" to metrics.categoryConcentrationIndex.toPercentageText(),
                    "Diferença entre 1ª e 2ª categoria" to metrics.categoryDominanceGap.toPercentageText(),
                    "Top 3 categorias por valor" to metrics.topThreeCategoriesByValue.joinToString(" • ") { it.toDisplayName() }.ifBlank { "Indefinido" }
                )
            )

            MetricSection(
                title = "Qualidade da classificação",
                items = listOf(
                    "Percentual de itens classificados" to metrics.classifiedItemsPercentage.toPercentageText(),
                    "Percentual OTHER por valor" to metrics.otherPercentageByValue.toPercentageText(),
                    "Percentual OTHER por quantidade" to metrics.otherPercentageByItems.toPercentageText()
                )
            )

            MetricSection(
                title = "Diversidade",
                items = listOf(
                    "Média de categorias por nota" to metrics.averageCategoriesPerReceipt.toCompactNumberText(),
                    "Índice de diversidade" to metrics.categoryDiversityIndex.toPercentageText(),
                    "Diversity score" to metrics.diversityScore.toPercentageText()
                )
            )

            MetricSection(
                title = "Comportamento alimentar",
                items = listOf(
                    "Percentual essencial" to metrics.essentialCategoriesPercentage.toPercentageText(),
                    "Percentual não essencial" to metrics.nonEssentialCategoriesPercentage.toPercentageText(),
                    "Industrializados / alimentação básica" to metrics.industrializedToBasicFoodRatio.toCompactRatioText(),
                    "Bebidas / alimentação básica" to metrics.beveragesToBasicFoodRatio.toCompactRatioText(),
                    "Bebidas / total" to metrics.beveragesToTotalRatio.toPercentageText(),
                    "Hortifruti / total" to metrics.produceToTotalRatio.toPercentageText(),
                    "Convenience score" to metrics.convenienceScore.toPercentageText(),
                    "Essential score" to metrics.essentialScore.toPercentageText()
                )
            )

            MetricSection(
                title = "Frequência por categoria",
                items = listOf(
                    "Industrializados" to metrics.receiptsWithIndustrializedPercentage.toReceiptFrequencyText(),
                    "Bebidas" to metrics.receiptsWithBeveragesPercentage.toReceiptFrequencyText(),
                    "Alimentação básica" to metrics.receiptsWithBasicFoodPercentage.toReceiptFrequencyText(),
                    "Hortifruti" to metrics.receiptsWithProducePercentage.toReceiptFrequencyText(),
                    "Higiene" to metrics.receiptsWithHygienePercentage.toReceiptFrequencyText(),
                    "Limpeza" to metrics.receiptsWithCleaningPercentage.toReceiptFrequencyText()
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Métricas por categoria", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            ProductCategory.entries.forEach { category ->
                val categoryMetrics = metrics.categoryMetrics[category]
                CategoryMetricsItem(
                    category = category,
                    valueTotal = categoryMetrics?.totalValue ?: 0.0,
                    itemTotal = categoryMetrics?.totalItems ?: 0,
                    valuePercentage = categoryMetrics?.valuePercentage ?: 0.0,
                    itemPercentage = categoryMetrics?.itemPercentage ?: 0.0,
                    frequency = categoryMetrics?.frequency ?: 0.0,
                    averageValuePerReceipt = categoryMetrics?.averageValuePerReceipt ?: 0.0,
                    averageItemsPerReceipt = categoryMetrics?.averageItemsPerReceipt ?: 0.0
                )
            }
        }
    }
}

@Composable
private fun FutureModelInputCard(analysis: StoredConsumptionAnalysis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Features oficiais do modelo V1", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis == null) {
                Text("Nenhuma análise executada")
                return@Column
            }

            val features = analysis.modelInput.features.toSortedMap()
            Text("Essas features compõem a entrada oficial da IA nesta V1 e serão usadas futuramente pelo modelo treinado offline.")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Versão do input: ${analysis.modelInput.version}")
            Text("Quantidade de features oficiais: ${features.size}")
            Spacer(modifier = Modifier.height(8.dp))
            features.forEach { (name, value) ->
                Text("$name: ${value.toCompactNumberText()}")
            }
        }
    }
}

@Composable
private fun BehavioralCompositionCard(analysis: StoredConsumptionAnalysis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Composição comportamental", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis == null) {
                Text("Nenhuma análise executada")
                return@Column
            }

            val composition = analysis.behaviorAnalysis?.behavioralComposition
            if (composition == null || composition.isEmpty()) {
                Text("Sem dados de composição")
                return@Column
            }

            Text("Distribuição dos perfis comportamentais identificados:")
            Spacer(modifier = Modifier.height(8.dp))
            composition.forEach { item ->
                Text("${item.profile.toDisplayName()} — ${"%.1f".format(Locale.US, item.percentage)}%")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Resumo geral", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(analysis.behaviorAnalysis?.summary ?: "Sem resumo disponível")
        }
    }
}

@Composable
private fun InsightsCard(analysis: StoredConsumptionAnalysis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Insights identificados", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (analysis == null) {
                Text("Nenhuma análise executada")
                return@Column
            }

            val insights = analysis.behaviorAnalysis?.insights
            if (insights == null || insights.isEmpty()) {
                Text("Sem insights identificados")
                return@Column
            }

            insights.forEach { insight ->
                InsightItem(
                    title = insight.title,
                    description = insight.description,
                    severity = insight.severity
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun InsightItem(
    title: String,
    description: String,
    severity: InsightSeverity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = getSeverityColor(severity)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun getSeverityColor(severity: InsightSeverity): Color {
    return when (severity) {
        InsightSeverity.LOW -> MaterialTheme.colorScheme.outline
        InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.onSurfaceVariant
        InsightSeverity.HIGH -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun MetricSection(
    title: String,
    items: List<Pair<String, String>>
) {
    Spacer(modifier = Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleSmall)
    Spacer(modifier = Modifier.height(4.dp))
    items.forEach { (label, value) ->
        Text("$label: $value")
    }
}

@Composable
private fun CategoryMetricsItem(
    category: ProductCategory,
    valueTotal: Double,
    itemTotal: Int,
    valuePercentage: Double,
    itemPercentage: Double,
    frequency: Double,
    averageValuePerReceipt: Double,
    averageItemsPerReceipt: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(category.toDisplayName(), style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Valor total: ${valueTotal.toCurrencyText()}")
            Text("Quantidade de itens: $itemTotal")
            Text("Percentual por valor: ${valuePercentage.toPercentageText()}")
            Text("Percentual por quantidade: ${itemPercentage.toPercentageText()}")
            Text("Frequência: ${frequency.toPercentageText()}")
            Text("Valor médio por nota: ${averageValuePerReceipt.toCurrencyText()}")
            Text("Itens médios por nota: ${averageItemsPerReceipt.toCompactNumberText()}")
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private fun Double.toPercentageText(): String = "${"%.1f".format(Locale.US, this * 100)}%"

private fun Double.toCurrencyText(): String = "R$ ${"%.2f".format(Locale.US, this).replace('.', ',')}"

private fun Double.toCompactNumberText(): String = "%.2f".format(Locale.US, this).replace('.', ',')

private fun Double.toCompactRatioText(): String = "%.2f".format(Locale.US, this).replace('.', ',')

private fun Double.toReceiptFrequencyText(): String = "presente em ${this.toPercentageText()} das notas"

private fun ProductCategory?.toDisplayName(): String {
    return when (this) {
        ProductCategory.BASIC_FOOD -> "Alimentação básica"
        ProductCategory.INDUSTRIALIZED -> "Industrializados"
        ProductCategory.BEVERAGES -> "Bebidas"
        ProductCategory.HYGIENE -> "Higiene"
        ProductCategory.CLEANING -> "Limpeza"
        ProductCategory.PRODUCE -> "Hortifruti"
        ProductCategory.OTHER -> "Outros"
        null -> "Indefinido"
    }
}

private fun ConsumptionBehaviorProfile.toDisplayName(): String {
    return when (this) {
        ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Orientado à conveniência"
        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Focado no essencial"
        ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Diversificado e equilibrado"
        ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Recorrente em bebidas"
        ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixo consumo de hortifruti"
        ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Foco em manutenção doméstica"
        ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Altamente concentrado"
        ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Consumo impulsivo"
        ConsumptionBehaviorProfile.UNDEFINED -> "Indefinido"
    }
}

private fun BehaviorClassificationSource.toDisplayName(): String {
    return when (this) {
        BehaviorClassificationSource.TRAINED_MODEL -> "Modelo treinado"
        BehaviorClassificationSource.RULE_BASED_FALLBACK -> "Fallback por regra simples"
    }
}

private fun ConsumptionBehaviorProfile.toSimpleDescription(): String {
    return when (this) {
        ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED -> "Maior presença de industrializados e compras práticas."
        ConsumptionBehaviorProfile.ESSENTIAL_FOCUSED -> "Consumo mais focado em itens essenciais do dia a dia."
        ConsumptionBehaviorProfile.DIVERSIFIED_BALANCED -> "Distribuição mais equilibrada entre categorias de compra."
        ConsumptionBehaviorProfile.BEVERAGE_RECURRENT -> "Recorrência relevante de bebidas nas notas analisadas."
        ConsumptionBehaviorProfile.LOW_FRESH_FOOD -> "Baixa presença de hortifruti no padrão de consumo atual."
        ConsumptionBehaviorProfile.HOUSEHOLD_MAINTENANCE -> "Foco maior em itens de higiene e limpeza doméstica."
        ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED -> "Concentração forte do gasto em poucas categorias."
        ConsumptionBehaviorProfile.IMPULSIVE_CONSUMPTION -> "Maior peso em categorias não essenciais e conveniência."
        ConsumptionBehaviorProfile.UNDEFINED -> "Padrão ainda indefinido com os dados disponíveis."
    }
}

private fun buildTechnicalSummary(analysis: StoredConsumptionAnalysis): String {
    val modelInput = analysis.modelInput
    return "input=${modelInput.version}, features=${modelInput.features.size}, notas=${analysis.metrics.totalReceipts}, itens=${analysis.metrics.totalItems}"
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

## FILE: app/src/main/java/com/example/consumoai/presentation/home/HomeUiState.kt

```kotlin
package com.example.consumoai.presentation.home

import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.domain.model.StoredConsumptionAnalysis

data class HomeUiState(
    val isImporting: Boolean = false,
    val isAnalyzing: Boolean = false,
    val importResult: ImportReceiptsResult? = null,
    val localSummary: StoredReceiptsSummary? = null,
    val storedAnalysis: StoredConsumptionAnalysis? = null,
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
                val result = importSampleNfceReceiptsUseCase()
                val summary = getStoredReceiptsSummaryUseCase()
                result to summary
            }.onSuccess { (result, summary) ->
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importResult = result,
                    localSummary = summary,
                    storedAnalysis = null
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
                val analysis = analyzeStoredReceiptsUseCase()
                val summary = getStoredReceiptsSummaryUseCase()
                analysis to summary
            }.onSuccess { (analysis, summary) ->
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    localSummary = summary,
                    storedAnalysis = analysis
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
                clearReceiptsUseCase()
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

class KeywordProductClassifierDataSourceTest {

    private val classifier = KeywordProductClassifierDataSource()

    @Test
    fun classify_all_coversMainCategories() {
        val input = listOf(
            ProductItem(name = "SUCO NATURALE LARANJA", price = 13.9),
            ProductItem(name = "BISC TORTIN ISABELA", price = 2.69),
            ProductItem(name = "PAPEL HIGIENICO NEVE", price = 18.0),
            ProductItem(name = "DETERGENTE YPE", price = 3.5),
            ProductItem(name = "SHAMPOO PANTENE", price = 19.9),
            ProductItem(name = "SABAO YPE", price = 8.5),
            ProductItem(name = "BANANA CATURRA", price = 7.0),
            ProductItem(name = "ARROZ TIPO 1", price = 25.0),
            ProductItem(name = "TOMATE", price = 6.0),
            ProductItem(name = "TOMATE S/PELE", price = 4.5)
        )

        val output = classifier.classifyAll(input)

        assertEquals(ProductCategory.BEVERAGES, output[0].category)
        assertEquals(ProductCategory.INDUSTRIALIZED, output[1].category)
        assertEquals(ProductCategory.HYGIENE, output[2].category)
        assertEquals(ProductCategory.CLEANING, output[3].category)
        assertEquals(ProductCategory.HYGIENE, output[4].category)
        assertEquals(ProductCategory.CLEANING, output[5].category)
        assertEquals(ProductCategory.PRODUCE, output[6].category)
        assertEquals(ProductCategory.BASIC_FOOD, output[7].category)
        assertEquals(ProductCategory.PRODUCE, output[8].category)
        assertEquals(ProductCategory.BASIC_FOOD, output[9].category)
    }

    @Test
    fun classify_appliesCommonOcrFixesBeforeMatching() {
        val item = ProductItem(name = "HOLHO TOMATE T03", price = 10.0)

        val classified = classifier.classify(item)

        assertEquals(ProductCategory.BASIC_FOOD, classified.category)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/data/classifier/RemoteConsumptionBehaviorClassifierTest.kt

```kotlin
package com.example.consumoai.data.classifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
class RemoteConsumptionBehaviorClassifierTest {
    @Test
    fun classify_returnsRemotePredictionWhenApiSucceeds() = runBlocking {
        val classifier = RemoteConsumptionBehaviorClassifier(
            api = object : ConsumptionModelApi {
                override suspend fun predict(request: ModelPredictionRequestDto): ModelPredictionResponseDto {
                    assertEquals("v1", request.version)
                    assertEquals(27, request.features.size)
                    return ModelPredictionResponseDto(
                        main_profile = "BEVERAGE_RECURRENT",
                        confidence = 0.465,
                        profile_scores = mapOf(
                            "BEVERAGE_RECURRENT" to 0.465,
                            "DIVERSIFIED_BALANCED" to 0.295,
                            "LOW_FRESH_FOOD" to 0.13
                        )
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
                "beverages_value_pct" to 0.30,
                "beverages_frequency" to 0.75
            )
        )
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, result.source)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, result.mainProfile)
        assertEquals(1.0, result.confidence, 0.0001)
    }
    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = linkedMapOf(
            "total_receipts" to 5.0,
            "total_items" to 20.0,
            "total_value" to 200.0,
            "average_ticket" to 40.0,
            "average_items_per_receipt" to 4.0,
            "basic_food_value_pct" to 0.20,
            "industrialized_value_pct" to 0.20,
            "beverages_value_pct" to 0.10,
            "hygiene_value_pct" to 0.05,
            "cleaning_value_pct" to 0.05,
            "produce_value_pct" to 0.10,
            "other_value_pct" to 0.30,
            "basic_food_frequency" to 0.60,
            "industrialized_frequency" to 0.60,
            "beverages_frequency" to 0.30,
            "produce_frequency" to 0.30,
            "hygiene_frequency" to 0.20,
            "cleaning_frequency" to 0.20,
            "category_concentration_index" to 0.30,
            "category_dominance_gap" to 0.10,
            "category_diversity_index" to 0.60,
            "essential_categories_percentage" to 0.40,
            "non_essential_categories_percentage" to 0.60,
            "convenience_score" to 0.30,
            "essential_score" to 0.40,
            "diversity_score" to 0.50,
            "classified_items_percentage" to 0.90
        )
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
import org.junit.Test

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

        val products = parser.parse(Jsoup.parse(html))

        assertEquals(2, products.size)
        assertEquals(1, products[0].itemNumber)
        assertEquals("SUCO NATURALE", products[0].name)
        assertEquals(13.90, products[0].price, 0.0001)
        assertEquals(2, products[1].itemNumber)
        assertEquals("COCA-COLA ORIG 2L", products[1].name)
        assertEquals(10.93, products[1].price, 0.0001)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/data/parser/ReceiptLayoutParserDataSourceTest.kt

```kotlin
package com.example.consumoai.data.parser

import com.example.consumoai.data.datasource.ocr.OcrElement
import org.junit.Assert.assertEquals
import org.junit.Test

class ReceiptLayoutParserDataSourceTest {

    private val parser = ReceiptLayoutParserDataSource()

    @Test
    fun parseProducts_reconstructsItemsByColumnsAndStopsAtFooter() {
        val elements = listOf(
            e("001", 100, 100, 160, 140),
            e("7891234567890", 520, 100, 760, 140),
            e("SUCO", 900, 100, 1030, 140),
            e("NATURALE", 1040, 100, 1270, 140),
            e("13,90", 1770, 100, 1880, 140),
            e("LARANJA", 900, 145, 1120, 185),
            e("INT", 1130, 145, 1210, 185),
            e("1L", 1220, 145, 1280, 185),
            e("002", 100, 210, 160, 250),
            e("7890000000002", 520, 210, 760, 250),
            e("IOG", 900, 210, 980, 250),
            e("BATIDO", 990, 210, 1170, 250),
            e("NESTLE", 1180, 210, 1360, 250),
            e("TRAD", 1370, 210, 1490, 250),
            e("ZR", 1500, 210, 1560, 250),
            e("1.15KG", 1570, 210, 1680, 250),
            e("18,63", 1770, 210, 1880, 250),
            e("003", 100, 280, 160, 320),
            e("7890000000003", 520, 280, 760, 320),
            e("COCA-COLA", 900, 280, 1170, 320),
            e("ORIG", 1180, 280, 1300, 320),
            e("2L", 1310, 280, 1370, 320),
            e("10,93", 1770, 280, 1880, 320),
            e("004", 100, 350, 160, 390),
            e("7890000000004", 520, 350, 760, 390),
            e("PAO", 900, 350, 990, 390),
            e("CACETINHO", 1000, 350, 1270, 390),
            e("8,40", 1770, 350, 1860, 390),
            e("QTD.", 100, 430, 190, 470),
            e("TOTAL", 210, 430, 340, 470),
            e("DE", 350, 430, 410, 470),
            e("ITENS", 420, 430, 530, 470),
            e("005", 100, 500, 160, 540),
            e("ITEM", 900, 500, 1010, 540),
            e("IGNORADO", 1020, 500, 1260, 540),
            e("99,99", 1770, 500, 1880, 540)
        )

        val products = parser.parseProducts(elements)

        assertEquals(4, products.size)
        assertEquals(1, products[0].itemNumber)
        assertEquals("SUCO NATURALE LARANJA INT 1L", products[0].name)
        assertEquals(13.90, products[0].price, 0.0001)
        assertEquals(2, products[1].itemNumber)
        assertEquals("IOG BATIDO NESTLE TRAD ZR 1.15KG", products[1].name)
        assertEquals(18.63, products[1].price, 0.0001)
        assertEquals(3, products[2].itemNumber)
        assertEquals("COCA-COLA ORIG 2L", products[2].name)
        assertEquals(10.93, products[2].price, 0.0001)
        assertEquals(4, products[3].itemNumber)
        assertEquals("PAO CACETINHO", products[3].name)
        assertEquals(8.40, products[3].price, 0.0001)
    }

    @Test
    fun parseProducts_appliesCommonOcrDescriptionFixes() {
        val elements = listOf(
            e("001", 100, 100, 160, 140),
            e("1234567890123", 520, 100, 760, 140),
            e("T03", 900, 100, 980, 140),
            e("8ISC", 990, 100, 1090, 140),
            e("HOLHO", 1100, 100, 1280, 140),
            e("A0", 1290, 100, 1360, 140),
            e("5,00", 1770, 100, 1860, 140)
        )

        val products = parser.parseProducts(elements)

        assertEquals(1, products.size)
        assertEquals(1, products.first().itemNumber)
        assertEquals("IOG BISC MOLHO AO", products.first().name)
        assertEquals(5.00, products.first().price, 0.0001)
    }

    @Test
    fun parseProducts_stopsBeforeFooterOnLastItem020() {
        val elements = listOf(
            e("020", 100, 100, 160, 140),
            e("7890000000020", 520, 100, 760, 140),
            e("CARNE", 900, 100, 1020, 140),
            e("MOIDA", 1030, 100, 1160, 140),
            e("TOP", 1170, 100, 1250, 140),
            e("QUALITY", 1260, 100, 1420, 140),
            e("CG", 1430, 100, 1490, 140),
            e("400G", 1500, 100, 1600, 140),
            e("16,89", 1770, 100, 1860, 140),
            e("QTD", 100, 180, 160, 220),
            e("TOTAL", 170, 180, 280, 220),
            e("VALOR", 290, 180, 410, 220),
            e("PAGAMENTO", 420, 180, 620, 220),
            e("185,43", 1770, 180, 1860, 220)
        )

        val products = parser.parseProducts(elements)

        assertEquals(1, products.size)
        assertEquals(20, products.first().itemNumber)
        assertEquals("CARNE MOIDA TOP QUALITY CG 400G", products.first().name)
        assertEquals(16.89, products.first().price, 0.0001)
    }

    private fun e(text: String, left: Int, top: Int, right: Int, bottom: Int): OcrElement {
        return OcrElement(
            text = text,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
            centerX = (left + right) / 2,
            centerY = (top + bottom) / 2
        )
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

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/AnalyzeStoredReceiptsUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.insights.DefaultConsumptionInsightsEngine
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.Receipt
import com.example.consumoai.domain.model.ReceiptSource
import com.example.consumoai.domain.repository.ReceiptRepository
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
            calculateConsumptionMetricsUseCase = CalculateConsumptionMetricsUseCase(),
            buildConsumptionModelInputUseCase = BuildConsumptionModelInputUseCase(),
            classifyConsumptionProfileUseCase = ClassifyConsumptionProfileUseCase(
                consumptionBehaviorClassifier = RuleBasedConsumptionBehaviorClassifier()
            ),
            insightsEngine = DefaultConsumptionInsightsEngine()
        )()

        assertEquals(2, analysis.receipts.size)
        assertEquals(2, analysis.metrics.totalReceipts)
        assertEquals(3, analysis.metrics.totalItems)
        assertEquals(22.5, analysis.metrics.totalValue, 0.0001)
        assertEquals(MODEL_INPUT_VERSION, analysis.modelInput.version)
        assertEquals(analysis.metrics.totalReceipts.toDouble(), analysis.modelInput.features["total_receipts"] ?: -1.0, 0.0001)
        assertEquals(analysis.metrics.averageTicket, analysis.modelInput.features["average_ticket"] ?: -1.0, 0.0001)
        assertEquals(27, analysis.modelInput.features.size)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, analysis.behaviorResult.mainProfile)
        assertEquals(BehaviorClassificationSource.RULE_BASED_FALLBACK, analysis.behaviorResult.source)
        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, analysis.behaviorAnalysis?.behaviorResult?.mainProfile)
        assertEquals(true, analysis.behaviorAnalysis?.insights?.isNotEmpty() ?: false)
        assertEquals(true, analysis.behaviorAnalysis?.behavioralComposition?.isNotEmpty() ?: false)
        assertEquals(true, analysis.behaviorAnalysis?.summary?.isNotEmpty() ?: false)
    }
}

```

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/BuildConsumptionModelInputUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.CategoryMetrics
import com.example.consumoai.domain.model.MODEL_INPUT_VERSION
import com.example.consumoai.domain.model.ProductCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildConsumptionModelInputUseCaseTest {

    private val useCase = BuildConsumptionModelInputUseCase()

    @Test
    fun invoke_buildsStableFeatureMapWithoutNaNOrInfinity() {
        val expectedKeys = setOf(
            "total_receipts",
            "total_items",
            "total_value",
            "average_ticket",
            "average_items_per_receipt",
            "basic_food_value_pct",
            "industrialized_value_pct",
            "beverages_value_pct",
            "hygiene_value_pct",
            "cleaning_value_pct",
            "produce_value_pct",
            "other_value_pct",
            "basic_food_frequency",
            "industrialized_frequency",
            "beverages_frequency",
            "produce_frequency",
            "hygiene_frequency",
            "cleaning_frequency",
            "category_concentration_index",
            "category_dominance_gap",
            "category_diversity_index",
            "essential_categories_percentage",
            "non_essential_categories_percentage",
            "convenience_score",
            "essential_score",
            "diversity_score",
            "classified_items_percentage"
        )

        val zeroDoubleMap = ProductCategory.entries.associateWith { 0.0 }
        val zeroIntMap = ProductCategory.entries.associateWith { 0 }
        val categoryMetrics = ProductCategory.entries.associateWith { category ->
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

        val metrics = ConsumptionMetrics(
            valuePercentageByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.4) + (ProductCategory.INDUSTRIALIZED to 0.2),
            itemPercentageByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.3) + (ProductCategory.OTHER to 0.1),
            frequencyByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 0.8),
            categoryMetrics = categoryMetrics,
            categoryValueTotals = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 120.0),
            categoryItemTotals = zeroIntMap + (ProductCategory.BASIC_FOOD to 12),
            totalReceipts = 10,
            totalItems = 30,
            totalValue = 300.0,
            averageTicket = 30.0,
            averageItemsPerReceipt = 3.0,
            maxCategoryByValue = ProductCategory.BASIC_FOOD,
            maxCategoryByItems = ProductCategory.BASIC_FOOD,
            receiptAverageValueByCategory = zeroDoubleMap + (ProductCategory.BASIC_FOOD to 12.0),
            categoryConcentrationIndex = 0.4,
            topThreeCategoriesByValue = listOf(ProductCategory.BASIC_FOOD, ProductCategory.INDUSTRIALIZED, ProductCategory.BEVERAGES),
            averageValuePerItem = 10.0,
            highestReceiptValue = 80.0,
            lowestReceiptValue = 10.0,
            receiptValueAmplitude = 70.0,
            highValueReceiptsPercentage = 0.4,
            lowValueReceiptsPercentage = 0.6,
            categoryDominanceGap = 0.2,
            topThreeCategoriesValuePercentage = 0.85,
            otherPercentageByValue = 0.05,
            otherPercentageByItems = 0.1,
            classifiedItemsPercentage = 0.9,
            averageCategoriesPerReceipt = 3.2,
            categoryDiversityIndex = 0.7,
            essentialCategoriesPercentage = 0.6,
            nonEssentialCategoriesPercentage = 0.4,
            industrializedToBasicFoodRatio = 0.5,
            beveragesToBasicFoodRatio = 0.3,
            beveragesToTotalRatio = 0.12,
            produceToTotalRatio = 0.15,
            receiptsWithIndustrializedPercentage = 0.6,
            receiptsWithBeveragesPercentage = 0.5,
            receiptsWithBasicFoodPercentage = 0.9,
            receiptsWithProducePercentage = 0.4,
            receiptsWithHygienePercentage = 0.2,
            receiptsWithCleaningPercentage = 0.3,
            averageIndustrializedItemsPerReceipt = 1.0,
            averageBeveragesItemsPerReceipt = 0.7,
            averageBasicFoodItemsPerReceipt = 1.8,
            averageProduceItemsPerReceipt = 0.5,
            convenienceScore = 0.45,
            essentialScore = 0.63,
            diversityScore = 0.72
        )

        val result = useCase(metrics)

        assertEquals(MODEL_INPUT_VERSION, result.version)
        assertEquals(expectedKeys, result.features.keys)
        assertEquals(10.0, result.features["total_receipts"] ?: -1.0, 0.0001)
        assertEquals(0.4, result.features["basic_food_value_pct"] ?: -1.0, 0.0001)
        assertEquals(0.8, result.features["basic_food_frequency"] ?: -1.0, 0.0001)
        assertEquals(27, result.features.size)
        assertFalse(result.features.containsKey("highest_receipt_value"))
        assertFalse(result.features.containsKey("basic_food_item_pct"))
        assertFalse(result.features.containsKey("top_three_categories_value_percentage"))
        assertFalse(result.features.values.any { it.isNaN() })
        assertFalse(result.features.values.any { it.isInfinite() })
        assertTrue(result.features.values.all { it in Double.NEGATIVE_INFINITY..Double.POSITIVE_INFINITY })
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

## FILE: app/src/test/java/com/example/consumoai/domain/usecase/ClassifyConsumptionProfileUseCaseTest.kt

```kotlin
package com.example.consumoai.domain.usecase

import com.example.consumoai.data.classifier.RuleBasedConsumptionBehaviorClassifier
import com.example.consumoai.domain.model.BehaviorClassificationSource
import com.example.consumoai.domain.model.ConsumptionBehaviorProfile
import com.example.consumoai.domain.model.ConsumptionModelInput
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
    fun invoke_returnsBeverageRecurrentWhenValueAndFrequencyThresholdsMatch() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.3,
            "beverages_value_pct" to 0.3,
            "beverages_frequency" to 0.75
        )

        assertEquals(ConsumptionBehaviorProfile.BEVERAGE_RECURRENT, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsConvenienceOrientedWhenConvenienceScoreIsHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.4,
            "convenience_score" to 0.6
        )

        assertEquals(ConsumptionBehaviorProfile.CONVENIENCE_ORIENTED, useCase(input).mainProfile)
    }

    @Test
    fun invoke_returnsHighlyConcentratedWhenConcentrationIsVeryHigh() = runBlocking {
        val input = input(
            "classified_items_percentage" to 0.9,
            "category_concentration_index" to 0.72
        )

        assertEquals(ConsumptionBehaviorProfile.HIGHLY_CONCENTRATED, useCase(input).mainProfile)
    }

    private fun input(vararg overrides: Pair<String, Double>): ConsumptionModelInput {
        val defaults = mutableMapOf(
            "total_receipts" to 5.0,
            "total_items" to 20.0,
            "total_value" to 200.0,
            "average_ticket" to 40.0,
            "average_items_per_receipt" to 4.0,
            "basic_food_value_pct" to 0.20,
            "industrialized_value_pct" to 0.20,
            "beverages_value_pct" to 0.10,
            "hygiene_value_pct" to 0.05,
            "cleaning_value_pct" to 0.05,
            "produce_value_pct" to 0.10,
            "other_value_pct" to 0.30,
            "basic_food_frequency" to 0.60,
            "industrialized_frequency" to 0.60,
            "beverages_frequency" to 0.30,
            "produce_frequency" to 0.30,
            "hygiene_frequency" to 0.20,
            "cleaning_frequency" to 0.20,
            "category_concentration_index" to 0.30,
            "category_dominance_gap" to 0.10,
            "category_diversity_index" to 0.60,
            "essential_categories_percentage" to 0.40,
            "non_essential_categories_percentage" to 0.60,
            "convenience_score" to 0.30,
            "essential_score" to 0.40,
            "diversity_score" to 0.50,
            "classified_items_percentage" to 0.90
        )

        overrides.forEach { (key, value) ->
            defaults[key] = value
        }

        return ConsumptionModelInput(features = defaults)
    }
}

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

