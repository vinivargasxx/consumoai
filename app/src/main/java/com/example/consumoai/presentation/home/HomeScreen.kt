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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.consumoai.domain.model.ImportReceiptsResult
import com.example.consumoai.domain.model.StoredReceiptsSummary
import com.example.consumoai.presentation.home.model.HomeAnalysisPresentation

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
            Text("Ações principais", style = MaterialTheme.typography.titleMedium)
            ActionButtons(
                isImporting = uiState.isImporting,
                isAnalyzing = uiState.isAnalyzing,
                onAction = onAction
            )

            StoredReceiptsCard(
                summary = uiState.localSummary,
                importResult = uiState.importResult
            )

            uiState.analysisPresentation?.let { presentation ->
                ProfileCard(presentation)
                MainCharacteristicsCard(presentation.mainCharacteristics)
                ConsumptionSummaryCard(presentation.consumptionSummaryItems)
                TechnicalDetailsCard(presentation.technicalItems)
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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    isImporting: Boolean,
    isAnalyzing: Boolean,
    onAction: (HomeScreenAction) -> Unit
) {
    Button(
        onClick = { onAction(HomeScreenAction.OnImportSampleNfceUrlsClick) },
        enabled = !isImporting,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = if (isImporting) "Importando..." else "Importar notas NFC-e de teste")
    }

    Button(
        onClick = { onAction(HomeScreenAction.OnAnalyzeStoredReceiptsClick) },
        enabled = !isAnalyzing,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = if (isAnalyzing) "Analisando..." else "Analisar notas armazenadas")
    }

    Button(
        onClick = { onAction(HomeScreenAction.OnClearReceiptsClick) },
        enabled = !isImporting && !isAnalyzing,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Limpar notas locais")
    }
}

@Composable
private fun StoredReceiptsCard(
    summary: StoredReceiptsSummary?,
    importResult: ImportReceiptsResult?
) {
    if (summary == null) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Notas armazenadas", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Notas: ${summary.totalReceipts}", style = MaterialTheme.typography.bodyLarge)
            Text("Itens: ${summary.totalItems}", style = MaterialTheme.typography.bodyLarge)
            Text("Valor total: ${summary.totalValue.toCurrencyText()}", style = MaterialTheme.typography.bodyLarge)

            if (importResult != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Última importação: ${importResult.importedCount} novas, ${importResult.skippedCount} duplicadas, ${importResult.failedCount} falhas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileCard(presentation: HomeAnalysisPresentation) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Perfil identificado", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            ProfileBadge(presentation.profileTitle)
            Spacer(modifier = Modifier.height(12.dp))
            Text(presentation.profileDescription, style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(12.dp))
            Text(presentation.confidenceLabel, style = MaterialTheme.typography.titleSmall)
            Text("Origem: ${presentation.sourceLabel}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = presentation.sourceWarning ?: "Resultado gerado pelo modelo treinado",
                style = MaterialTheme.typography.bodySmall,
                color = if (presentation.sourceWarning == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
private fun ProfileBadge(profileTitle: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFFB74D).copy(alpha = 0.45f),
        tonalElevation = 6.dp
    ) {
        Text(
            text = profileTitle.uppercase(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            textAlign = TextAlign.Center,
            color = Color(0xFF4D2600)
        )
    }
}

@Composable
private fun MainCharacteristicsCard(mainCharacteristics: List<String>) {
    if (mainCharacteristics.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Principais características", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            mainCharacteristics.forEach { line ->
                if (line == "Observações:") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(line, style = MaterialTheme.typography.titleSmall)
                } else {
                    Text("- $line", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ConsumptionSummaryCard(items: List<Pair<String, String>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumo de consumo", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            items.forEach { (label, value) ->
                Text("$label: $value", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun TechnicalDetailsCard(items: List<Pair<String, String>>) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Ver detalhes técnicos", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (expanded) "Ocultar detalhes" else "Abrir detalhes")
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                items.forEach { (label, value) ->
                    val isTechnicalWarning = label == "Aviso técnico"
                    Text(
                        text = "$label: $value",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isTechnicalWarning) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

private fun Double.toCurrencyText(): String {
    return "R$ ${"%.2f".format(java.util.Locale.US, this).replace('.', ',')}"
}
