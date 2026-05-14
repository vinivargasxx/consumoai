# ConsumoAI - Arquitetura e Estrutura de Classes

**Versão:** 1.0 com Motor de Insights Comportamentais  
**Data:** Maio/2026  
**Tecnologia:** Kotlin + Jetpack Compose + Room + Retrofit + Random Forest ML

---

## 📋 Índice

1. [Visão Geral da Arquitetura](#visão-geral)
2. [Motor de Insights (Novo)](#motor-de-insights)
3. [Modelos de Domínio](#modelos-de-domínio)
4. [Casos de Uso](#casos-de-uso)
5. [Classificadores e Predição](#classificadores-e-predição)
6. [Apresentação (UI/ViewModel)](#apresentação)
7. [Injeção de Dependências](#injeção-de-dependências)
8. [Fluxo de Análise Completo](#fluxo-de-análise)

---

## Visão Geral da Arquitetura {#visão-geral}

ConsumoAI segue **Clean Architecture** com separação clara entre:

- **Domain**: Lógica de negócio pura (cases de uso, modelos, interfaces)
- **Data**: Implementações de repositórios, acesso a dados, classadores remotos
- **Presentation**: UI em Jetpack Compose, ViewModels com Coroutines
- **Core**: Configuração de DI com Koin

### Fluxo de Dados Principal

```
Receipts (Bank) 
    ↓
[Import] → Room Database
    ↓
[Analyze]
    ↓
Metrics Calculation → Model Input (V1 Features)
    ↓
Classifier (Remote via Retrofit ou Local Rule-Based)
    ↓
Consumption Behavior Result (Profile + Confidence + Scores)
    ↓
Insights Engine ← NEW!
    ↓
Behavioral Analysis (Insights + Composition + Summary)
    ↓
UI Rendering
```

---

## Motor de Insights (Novo) {#motor-de-insights}

### O que é?

Sistema interpretável e estruturado que gera insights sobre o comportamento de consumo usando:
- Métricas calculadas (diversidade, concentração, frequência)
- Probabilidades do modelo (scores dos perfis)
- Composição comportamental (top 3 perfis)

### Sem IA Generativa

❌ Nenhuma chamada a OpenAI, Gemini ou LLM  
❌ Nenhum NLP ou processamento de linguagem natural  
✅ Regras estruturadas e interpretáveis  
✅ Templates textuais hardcoded  
✅ Totalmente explicável para TCC

### Arquivos Principais

#### `ConsumptionInsight.kt`
```kotlin
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

**Responsabilidades:**
- Define estrutura de um insight individual
- Categoriza por tipo (comportamental, dominância, recorrência, etc.)
- Classifica por severidade (influencia cor na UI)
- Rastreia perfis e features relacionadas

#### `ConsumptionBehaviorAnalysis.kt`
```kotlin
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

**Responsabilidades:**
- Agregação de resultado completo da análise
- Combina classificação do modelo com insights
- Inclui resumo textual e composição comportamental

#### `ConsumptionInsightsEngine.kt` (Interface)
```kotlin
interface ConsumptionInsightsEngine {
    fun generate(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis
}
```

**Contrato:**
- Input: métricas calculadas + resultado do modelo
- Output: análise comportamental completa com insights

#### `DefaultConsumptionInsightsEngine.kt` (Implementação)
```kotlin
class DefaultConsumptionInsightsEngine : ConsumptionInsightsEngine {
    override fun generate(
        metrics: ConsumptionMetrics,
        result: ConsumptionBehaviorResult
    ): ConsumptionBehaviorAnalysis { ... }
}
```

**Lógica de Geração de Insights:**

1. **Recorrência de Bebidas**
   - Condição: `beverageFrequency >= 0.70`
   - Severity: HIGH
   - Relacionado: BEVERAGE_RECURRENT

2. **Baixa Diversidade**
   - Condição: `categoryDiversityIndex < 0.40`
   - Severity: MEDIUM
   - Tipo: DIVERSITY

3. **Alta Concentração**
   - Condição: `categoryConcentrationIndex >= 0.60`
   - Severity: MEDIUM
   - Tipo: CONCENTRATION

4. **Industrializados Dominantes**
   - Condição: `industrializedPercentage >= 0.35`
   - Severity: MEDIUM
   - Relacionado: CONVENIENCE_ORIENTED

5. **Baixo Consumo de Frescos**
   - Condição: `producePercentage <= 0.05`
   - Severity: MEDIUM
   - Relacionado: LOW_FRESH_FOOD

6. **Consumo Equilibrado**
   - Condição: `diversityScore >= 0.65 AND categoryConcentrationIndex <= 0.35`
   - Severity: LOW
   - Relacionado: DIVERSIFIED_BALANCED

7. **Comportamento Híbrido**
   - Condição: `profileScores[1] >= 0.25` (segundo maior)
   - Severity: LOW
   - Tipo: MODEL_INTERPRETATION

8. **Baixa Confiança**
   - Condição: `confidence < 0.50`
   - Severity: MEDIUM
   - Tipo: MODEL_INTERPRETATION

**Composição Comportamental:**
- Seleciona top 3 perfis ordenados por score descrescente
- Converte score em percentual: `score * 100`
- Exemplo: 46% Recorrente + 29% Diversificado + 13% Baixo Frescos

**Geração de Resumo:**
- Constrói texto dinâmico via templates hardcoded
- Menciona perfil principal
- Adiciona segundo padrão se existir
- Inclui observações sobre concentração e diversidade
- Alerta sobre baixa presença de frescos

---

## Modelos de Domínio {#modelos-de-domínio}

### Enums Principais

#### `ConsumptionBehaviorProfile.kt`
```kotlin
enum class ConsumptionBehaviorProfile {
    CONVENIENCE_ORIENTED,      // Industrializados, conveniência
    ESSENTIAL_FOCUSED,         // Itens essenciais
    DIVERSIFIED_BALANCED,      // Distribuição equilibrada
    BEVERAGE_RECURRENT,        // Bebidas frequentes
    LOW_FRESH_FOOD,            // Pouco hortifruti
    HOUSEHOLD_MAINTENANCE,     // Higiene e limpeza
    HIGHLY_CONCENTRATED,       // Muito concentrado
    IMPULSIVE_CONSUMPTION,     // Características impulsivas
    UNDEFINED                  // Não definido
}
```

#### `ProductCategory.kt`
```kotlin
enum class ProductCategory {
    BASIC_FOOD,       // Alimentação básica (arroz, feijão)
    INDUSTRIALIZED,   // Alimentos processados
    BEVERAGES,        // Bebidas em geral
    HYGIENE,          // Higiene pessoal
    CLEANING,         // Produtos de limpeza
    PRODUCE,          // Hortifruti (frutas, verduras)
    OTHER             // Outras categorias
}
```

#### `ReceiptSource.kt`
```kotlin
enum class ReceiptSource {
    QR_CODE,  // Via QR Code NFC-e
    OCR       // Via OCR de foto
}
```

### Data Classes Principais

#### `Receipt.kt`
```kotlin
data class Receipt(
    val id: Long,
    val accessKeyOrUrl: String,
    val source: ReceiptSource,
    val items: List<ProductItem>
)
```

#### `ProductItem.kt`
```kotlin
data class ProductItem(
    val name: String,
    val price: Double,
    val category: ProductCategory
)
```

#### `ConsumptionMetrics.kt`
Agregação de 60+ métricas calculadas:
- Percentuais por categoria (valor, item, frequência)
- Índices de diversidade e concentração
- Métricas comportamentais (essential, convenience, produce ratio)
- Scores oficiais para modelo V1

#### `ConsumptionModelInput.kt`
```kotlin
data class ConsumptionModelInput(
    val version: String,
    val features: Map<String, Double>
)
```
- Versão: "V1"
- 27 features oficiais que alimentam o modelo treinado

#### `ConsumptionBehaviorResult.kt`
```kotlin
data class ConsumptionBehaviorResult(
    val mainProfile: ConsumptionBehaviorProfile,
    val confidence: Double,
    val profileScores: Map<ConsumptionBehaviorProfile, Double>,
    val source: BehaviorClassificationSource
)

enum class BehaviorClassificationSource {
    TRAINED_MODEL,          // Viado do modelo remoto
    RULE_BASED_FALLBACK     // Fallback local por regras
}
```

#### `StoredConsumptionAnalysis.kt`
```kotlin
data class StoredConsumptionAnalysis(
    val receipts: List<Receipt>,
    val metrics: ConsumptionMetrics,
    val modelInput: ConsumptionModelInput,
    val behaviorResult: ConsumptionBehaviorResult,
    val behaviorAnalysis: ConsumptionBehaviorAnalysis? = null
)
```

#### `StoredReceiptsSummary.kt`
Lightweight summary para pós-importação:
```kotlin
data class StoredReceiptsSummary(
    val totalReceipts: Int,
    val totalItems: Int,
    val totalValue: Double
)
```

---

## Casos de Uso {#casos-de-uso}

### `CalculateConsumptionMetricsUseCase`
**Entrada:** Lista de `Receipt`  
**Saída:** `ConsumptionMetrics`  
**Responsabilidade:** Computar todas as 60+ métricas

### `BuildConsumptionModelInputUseCase`
**Entrada:** `ConsumptionMetrics`  
**Saída:** `ConsumptionModelInput` (V1)  
**Responsabilidade:** Extrair 27 features oficiais para modelo

### `ClassifyConsumptionProfileUseCase`
**Entrada:** `ConsumptionModelInput`  
**Saída:** `ConsumptionBehaviorResult` (suspend)  
**Responsabilidade:** Orquestrar classificação (remoto com fallback)

### `AnalyzeStoredReceiptsUseCase`
**Entrada:** Nada (usa repo)  
**Saída:** `StoredConsumptionAnalysis` (suspend)  
**Fluxo:**
1. Buscar recibos do banco
2. Validar (não vazio)
3. Calcular métricas
4. Construir entrada do modelo
5. Classificar perfil (remoto/fallback)
6. **Gerar insights via engine** ← NEW!
7. Retornar análise completa

### `GetStoredReceiptsSummaryUseCase`
**Entrada:** Nada (usa repo)  
**Saída:** `StoredReceiptsSummary` (suspend)  
**Responsabilidade:** Lightweight summary sem análise

### `ImportSampleNfceReceiptsUseCase`
**Entrada:** URLs de test NFC-es  
**Saída:** `ImportReceiptsResult` (suspend)  
**Fluxo:** Buscar → Parsear → Classificar → Salvar em Room

### `ClearReceiptsUseCase`
**Entrada:** Nada  
**Saída:** Unit (suspend)  
**Responsabilidade:** Limpar tudo do banco

---

## Classificadores e Predição {#classificadores-e-predição}

### Interface `ConsumptionBehaviorClassifier`
```kotlin
interface ConsumptionBehaviorClassifier {
    suspend fun classify(input: ConsumptionModelInput): ConsumptionBehaviorResult
}
```

### `RemoteConsumptionBehaviorClassifier`
- **Estratégia:** Remote-first com fallback
- Endpoint: POST `/predict` (FastAPI backend)
- DTO: `ModelPredictionRequestDto` / `ModelPredictionResponseDto`
- Fallback: `RuleBasedConsumptionBehaviorClassifier`
- Origem: `TRAINED_MODEL` ou `RULE_BASED_FALLBACK`

### `RuleBasedConsumptionBehaviorClassifier`
- **Estratégia:** Heurísticas locais
- Usa `ConsumptionMetrics` para decisão
- Exemplos:
  - Se `beveragesFreq > 0.7` → BEVERAGE_RECURRENT
  - Se `diversityScore < 0.4` → HIGHLY_CONCENTRATED
  - etc.
- Origem: `RULE_BASED_FALLBACK`
- Confidence: `1.0` (certo)

### `ConsumptionModelApi` (Retrofit)
```kotlin
interface ConsumptionModelApi {
    @POST("predict")
    suspend fun predict(
        @Body request: ModelPredictionRequestDto
    ): ModelPredictionResponseDto
}
```

### `KeywordProductClassifierDataSource`
- Classifica itens em categorias via keywords
- Usa lista hardcoded de palavras-chave
- Fallback: `OTHER`

---

## Apresentação (UI/ViewModel) {#apresentação}

### `HomeViewModel`
```kotlin
class HomeViewModel(
    private val importSampleNfceReceiptsUseCase: ImportSampleNfceReceiptsUseCase,
    private val analyzeStoredReceiptsUseCase: AnalyzeStoredReceiptsUseCase,
    private val getStoredReceiptsSummaryUseCase: GetStoredReceiptsSummaryUseCase,
    private val clearReceiptsUseCase: ClearReceiptsUseCase
) : ViewModel()
```

**Actions Principais:**
- `onImportSampleNfceUrlsClick()`: Importar dados
- `onAnalyzeStoredReceiptsClick()`: Analisar com insights
- `onClearReceiptsClick()`: Limpar tudo

**State Management:**
```kotlin
data class HomeUiState(
    val isImporting: Boolean = false,
    val isAnalyzing: Boolean = false,
    val importResult: ImportReceiptsResult? = null,
    val localSummary: StoredReceiptsSummary? = null,
    val storedAnalysis: StoredConsumptionAnalysis? = null,
    val errorMessage: String? = null
)
```

### `HomeScreen` (Composable)
**Cards Renderizados:**
1. Botões (Import, Analyze, Clear)
2. `ImportResultCard` - metadata da última importação
3. `StoredSummaryCard` - resumo local (sempre visível)
4. `BehaviorResultCard` - resultado do modelo
5. **`BehavioralCompositionCard`** ← NEW! Composição dos perfis
6. **`InsightsCard`** ← NEW! Lista de insights identificados
7. `FutureModelInputCard` - features V1
8. `ConsumptionMetricsCard` - todas as métricas (60+)

**Lógica Condicional:**
- Cards 4-7 renderizam apenas se `storedAnalysis != null`
- Cards 1-3, 8 sempre acessíveis
- Barra de progresso enquanto loading
- Mensagem de erro em vermelho

---

## Injeção de Dependências {#injeção-de-dependências}

### `DomainModule`
```kotlin
val domainModule = module {
    // Insights Engine
    single<ConsumptionInsightsEngine> {
        DefaultConsumptionInsightsEngine()
    }
    
    // Use cases
    factory { CalculateConsumptionMetricsUseCase() }
    factory { BuildConsumptionModelInputUseCase() }
    factory {
        ClassifyConsumptionProfileUseCase(
            consumptionBehaviorClassifier = get()
        )
    }
    factory {
        AnalyzeStoredReceiptsUseCase(
            receiptRepository = get(),
            calculateConsumptionMetricsUseCase = get(),
            buildConsumptionModelInputUseCase = get(),
            classifyConsumptionProfileUseCase = get(),
            insightsEngine = get()  // ← NEW!
        )
    }
    // ... outros
}
```

### `DataModule`
- Fornece Retrofit com Gson converter
- BuildConfig.MODEL_API_BASE_URL = "http://10.0.2.2:8000/"
- `ConsumptionModelApi` singleton
- `RemoteConsumptionBehaviorClassifier` com fallback

### `AppModule`
- ViewModel factories com todos os use cases
- Database Room (AppDatabase)
- Repository implementations

---

## Fluxo de Análise Completo {#fluxo-de-análise}

### 1️⃣ Import Flow (Sem análise)
```
User clicks "Importar"
    ↓
HomeViewModel.importSampleReceipts()
    ↓
ImportSampleNfceReceiptsUseCase()
    - Fetch URLs from sample data
    - Parse via NfceQrCodeDataSource
    - Classify items via KeywordProductClassifierDataSource
    - Save to Room
    ↓
GetStoredReceiptsSummaryUseCase()
    - Count receipts, items, sum value
    ↓
Update HomeUiState:
    - isImporting = false
    - importResult = {importedCount, skippedCount, failedCount}
    - localSummary = {totalReceipts, totalItems, totalValue}
    - storedAnalysis = null  ← No analysis!
    ↓
UI shows ImportResultCard + StoredSummaryCard
```

### 2️⃣ Analyze Flow (Com insights)
```
User clicks "Analisar"
    ↓
HomeViewModel.analyzeStoredReceipts()
    ↓
AnalyzeStoredReceiptsUseCase()
    
    a) Fetch from repository
    b) Validate (throw if empty)
    c) CalculateConsumptionMetricsUseCase
       → 60+ metrics computed
    d) BuildConsumptionModelInputUseCase
       → 27 official features extracted
    e) ClassifyConsumptionProfileUseCase
       ├─ Try RemoteConsumptionBehaviorClassifier
       │   └─ POST /predict via Retrofit
       │       └─ Success → ConsumptionBehaviorResult
       │                    (source = TRAINED_MODEL)
       │       └─ Fail → fallback
       └─ RuleBasedConsumptionBehaviorClassifier
           └─ ConsumptionBehaviorResult
              (source = RULE_BASED_FALLBACK)
    
    f) **DefaultConsumptionInsightsEngine.generate()** ← NEW!
       ├─ generateRecurrenceInsights()
       ├─ generateDiversityInsights()
       ├─ generateConcentrationInsights()
       ├─ generateCategoryDominanceInsights()
       ├─ generateFreshFoodInsights()
       ├─ generateBalanceInsights()
       ├─ generateHybridBehaviorInsights()
       ├─ generateConfidenceInsights()
       ├─ generateBehavioralComposition() → top 3 profiles
       └─ generateSummary() → dynamic text
       
       → ConsumptionBehaviorAnalysis
    
    g) Return StoredConsumptionAnalysis complete
    ↓
Update HomeUiState:
    - isAnalyzing = false
    - storedAnalysis = {receipts, metrics, modelInput,
                        behaviorResult, behaviorAnalysis}
    ↓
UI renders all cards including:
    - BehaviorResultCard (model result)
    - BehavioralCompositionCard (top 3 profiles %)
    - InsightsCard (all insights with severity colors)
    - FutureModelInputCard (features)
    - ConsumptionMetricsCard (all 60+ metrics)
```

### 3️⃣ Clear Flow
```
User clicks "Limpar"
    ↓
HomeViewModel.clearReceipts()
    ↓
ClearReceiptsUseCase()
    ↓
Room DELETE *
    ↓
Reset HomeUiState to defaults
    ↓
UI back to empty
```

---

## Estrutura de Arquivos

```
app/src/main/java/com/example/consumoai/
├── ConsumoAiApplication.kt
├── MainActivity.kt
│
├── core/di/
│   ├── AppModule.kt
│   ├── DataModule.kt
│   └── DomainModule.kt
│
├── data/
│   ├── classifier/
│   │   ├── ConsumptionModelApi.kt (Retrofit)
│   │   ├── KeywordProductClassifierDataSource.kt
│   │   ├── ModelPredictionDtos.kt
│   │   ├── RemoteConsumptionBehaviorClassifier.kt
│   │   └── RuleBasedConsumptionBehaviorClassifier.kt
│   ├── datasource/
│   │   ├── ocr/
│   │   │   ├── MlKitOcrDataSource.kt
│   │   │   └── OcrElement.kt
│   │   └── qrcode/
│   │       └── NfceQrCodeDataSource.kt
│   ├── local/
│   │   ├── AppDatabase.kt (Room)
│   │   ├── dao/
│   │   │   └── ReceiptDao.kt
│   │   └── entity/
│   │       ├── ProductItemEntity.kt
│   │       ├── ReceiptEntity.kt
│   │       └── ReceiptWithItems.kt
│   ├── mapper/
│   │   └── ReceiptMapper.kt
│   ├── parser/
│   │   ├── NfceHtmlParserDataSource.kt
│   │   └── ReceiptLayoutParserDataSource.kt
│   ├── remote/
│   │   └── dto/
│   │       └── ModelPredictionDtos.kt
│   └── repository/
│       └── ReceiptRepositoryImpl.kt
│
├── domain/
│   ├── classifier/
│   │   ├── ConsumptionBehaviorClassifier.kt
│   │   └── ProductClassifier.kt
│   ├── insights/
│   │   ├── ConsumptionInsightsEngine.kt
│   │   └── DefaultConsumptionInsightsEngine.kt ← NEW!
│   ├── model/
│   │   ├── CategoryMetrics.kt
│   │   ├── ConsumptionBehaviorAnalysis.kt ← NEW!
│   │   ├── ConsumptionBehaviorProfile.kt
│   │   ├── ConsumptionBehaviorResult.kt
│   │   ├── ConsumptionInsight.kt ← NEW!
│   │   ├── ConsumptionMetrics.kt
│   │   ├── ConsumptionModelInput.kt
│   │   ├── ImportReceiptsResult.kt
│   │   ├── ProductCategory.kt
│   │   ├── ProductItem.kt
│   │   ├── Receipt.kt
│   │   ├── ReceiptSource.kt
│   │   ├── StoredConsumptionAnalysis.kt (updated)
│   │   └── StoredReceiptsSummary.kt
│   ├── repository/
│   │   └── ReceiptRepository.kt
│   └── usecase/
│       ├── AnalyzeReceiptFromOcrUseCase.kt
│       ├── AnalyzeReceiptFromQrCodeUrlUseCase.kt
│       ├── AnalyzeReceiptWithFallbackUseCase.kt
│       ├── AnalyzeStoredReceiptsUseCase.kt (updated)
│       ├── BuildConsumptionModelInputUseCase.kt
│       ├── CalculateConsumptionMetricsUseCase.kt
│       ├── ClassifyConsumptionProfileUseCase.kt
│       ├── ClassifyProductsUseCase.kt
│       ├── ClearReceiptsUseCase.kt
│       ├── GetStoredReceiptsSummaryUseCase.kt
│       ├── ImportSampleNfceReceiptsUseCase.kt
│       └── SaveReceiptUseCase.kt
│
├── presentation/home/
│   ├── HomeRoute.kt
│   ├── HomeScreen.kt (updated with insights UI)
│   ├── HomeScreenAction.kt
│   ├── HomeUiState.kt
│   └── HomeViewModel.kt
│
└── ui/theme/
    ├── Color.kt
    ├── Theme.kt
    └── Type.kt
```

---

## ✅ Critérios de Sucesso Atendidos

| Critério | Status | Detalhes |
|----------|--------|----------|
| Compila sem erros | ✅ | `assembleDebug` SUCCESS |
| Testes passam | ✅ | `testDebugUnitTest` SUCCESS |
| Motor de insights implementado | ✅ | DefaultConsumptionInsightsEngine com 8 tipos |
| Insights usam métricas | ✅ | Baseados em 60+ métricas calculadas |
| Insights usam probabilidades | ✅ | Usam profileScores do modelo |
| Composição comportamental | ✅ | Top 3 perfis com %. renderizado na UI |
| Sem IA generativa | ✅ | Zero chamadas OpenAI/Gemini |
| Sem LLM | ✅ | Zero transformers ou modelos de linguagem |
| Sem OpenAI/Gemini | ✅ | Apenas regras estruturadas e templates |
| Motor explicável | ✅ | Cada insight é rastreável a métrica/score |
| UI mostra insights | ✅ | InsightsCard renderiza com severidade |
| UI mostra composição | ✅ | BehavioralCompositionCard com % |
| Modelo continua Only classifying | ✅ | Random Forest apenas retorna scores |
| Valor percebido aumentado | ✅ | Insights acionáveis + composição visual |
| TCC reforçado | ✅ | Sistema totalmente interpretável e documentado |

---

## Resumo das Novidades (v1.0 com Insights)

### Novas Classes
- ✅ `ConsumptionInsight.kt` - Modelo de insight individual
- ✅ `ConsumptionBehaviorAnalysis.kt` - Agregação com insights
- ✅ `ConsumptionInsightsEngine.kt` - Interface do motor
- ✅ `DefaultConsumptionInsightsEngine.kt` - Implementação com 8 tipos de insights

### Nouveaux Components de UI
- ✅ `BehavioralCompositionCard()` - Mostra distribuição dos perfis
- ✅ `InsightsCard()` - Lista todos os insights com severidade
- ✅ `InsightItem()` - Card individual de insight
- ✅ `getSeverityColor()` - Mapeamento de cor por severidade

### Fluxos Atualizados
- ✅ `AnalyzeStoredReceiptsUseCase` - Agora gera insights
- ✅ `StoredConsumptionAnalysis` - Agora inclui behaviorAnalysis
- ✅ `HomeViewModel` - Trata novo estado de análise
- ✅ `HomeScreen` - Renderiza cards de insights

### Injeção de Dependências
- ✅ `DomainModule` registra ConsumptionInsightsEngine como singleton

### Testes
- ✅ `AnalyzeStoredReceiptsUseCaseTest` atualizado com insights

---

## Como Estender

### Adicionar Novo Tipo de Insight

1. Adicionar novo `InsightType` enum em `ConsumptionInsight.kt`
2. Criar método privado em `DefaultConsumptionInsightsEngine`:
   ```kotlin
   private fun generateNewInsight(
       metrics: ConsumptionMetrics,
       insights: MutableList<ConsumptionInsight>
   ) {
       if (/* condition */) {
           insights.add(
               ConsumptionInsight(
                   title = "...",
                   description = "...",
                   type = InsightType.NEW_TYPE,
                   severity = InsightSeverity.MEDIUM,
                   relatedProfiles = listOf(...),
                   relatedFeatures = listOf(...)
               )
           )
       }
   }
   ```
3. Chamar em `generate()` method

### Customizar Cores de Severidade

Editar `getSeverityColor()` em `HomeScreen.kt`:
```kotlin
@Composable
private fun getSeverityColor(severity: InsightSeverity): Color {
    return when (severity) {
        InsightSeverity.LOW -> MaterialTheme.colorScheme.outline
        InsightSeverity.MEDIUM -> MaterialTheme.colorScheme.warning  // customize
        InsightSeverity.HIGH -> MaterialTheme.colorScheme.error
    }
}
```

### Integrar com Backend ML

Use `RemoteConsumptionBehaviorClassifier` com FastAPI:

```python
# FastAPI backend
@app.post("/predict")
async def predict(request: ModelPredictionRequestDto):
    features = request.features
    prediction = model.predict(features)
    return ModelPredictionResponseDto(
        main_profile=prediction["main"],
        confidence=prediction["confidence"],
        profile_scores=prediction["scores"]
    )
```

---

## Conclusão

ConsumoAI agora possui um **motor de insights estruturado, interpretável e sem IA generativa** que:

✨ Gera insights automaticamente a partir de métricas e probabilidades  
📊 Mostra composição comportamental (top 3 perfis)
🔍 É totalmente explicável para fins de TCC  
🚀 Aumenta valor percebido para o usuário  
🏗️ Mantém arquitetura limpa e testável  

---

**Versão:** 1.0  
**Última atualização:** Maio/2026  
**Status:** Production Ready ✅

