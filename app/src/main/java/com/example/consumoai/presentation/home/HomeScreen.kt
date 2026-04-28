package com.example.consumoai.presentation.home
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.unit.dp
import com.example.consumoai.domain.model.ConsumptionPeriod
import com.example.consumoai.domain.model.ProductCategory
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
                .padding(16.dp)
        ) {
            Text(
                text = "Analise de consumo",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(16.dp))
            PeriodSelector(
                selectedPeriod = uiState.selectedPeriod,
                onPeriodSelected = { onAction(HomeScreenAction.OnPeriodSelected(it)) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            when {
                uiState.isLoading -> {
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
                uiState.analysis != null -> {
                    AnalysisContent(uiState = uiState)
                }
            }
        }
    }
}
@Composable
private fun PeriodSelector(
    selectedPeriod: ConsumptionPeriod,
    onPeriodSelected: (ConsumptionPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = { onPeriodSelected(ConsumptionPeriod.LAST_30_DAYS) },
            label = { Text("30 dias") },
            enabled = selectedPeriod != ConsumptionPeriod.LAST_30_DAYS
        )
        AssistChip(
            onClick = { onPeriodSelected(ConsumptionPeriod.LAST_3_MONTHS) },
            label = { Text("3 meses") },
            enabled = selectedPeriod != ConsumptionPeriod.LAST_3_MONTHS
        )
        AssistChip(
            onClick = { onPeriodSelected(ConsumptionPeriod.LAST_6_MONTHS) },
            label = { Text("6 meses") },
            enabled = selectedPeriod != ConsumptionPeriod.LAST_6_MONTHS
        )
    }
}
@Composable
private fun AnalysisContent(uiState: HomeUiState) {
    val analysis = uiState.analysis ?: return
    val metrics = analysis.metrics
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Perfil identificado",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = analysis.profile.name,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Resumo financeiro",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notas analisadas: ${metrics.totalReceipts}")
                Text("Total gasto: R$ ${"%.2f".format(metrics.totalValue)}")
                Text("Ticket medio: R$ ${"%.2f".format(metrics.averageTicket)}")
                Text("Periodo analisado: ${metrics.periodDays} dias")
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Percentual por valor",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ProductCategory.entries.forEach { category ->
                    val percentage = metrics.valuePercentageByCategory[category] ?: 0.0
                    Text(text = "${category.name}: ${"%.1f".format(percentage * 100)}%")
                }
            }
        }
    }
}
