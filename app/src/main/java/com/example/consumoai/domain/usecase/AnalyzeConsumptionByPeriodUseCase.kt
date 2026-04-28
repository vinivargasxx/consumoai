package com.example.consumoai.domain.usecase
import com.example.consumoai.domain.model.ConsumptionPeriod
import com.example.consumoai.domain.model.ReceiptAnalysis
import com.example.consumoai.domain.repository.ReceiptRepository
class AnalyzeConsumptionByPeriodUseCase(
    private val receiptRepository: ReceiptRepository,
    private val getPeriodDateRangeUseCase: GetPeriodDateRangeUseCase,
    private val calculateConsumptionMetricsUseCase: CalculateConsumptionMetricsUseCase,
    private val classifyConsumptionProfileUseCase: ClassifyConsumptionProfileUseCase
) {
    suspend operator fun invoke(period: ConsumptionPeriod): ReceiptAnalysis {
        val (startDate, endDate) = getPeriodDateRangeUseCase(period)
        val receipts = receiptRepository.getReceiptsByPeriod(
            startDate = startDate,
            endDate = endDate
        )
        val metrics = calculateConsumptionMetricsUseCase(receipts)
        val profile = classifyConsumptionProfileUseCase(metrics)
        return ReceiptAnalysis(
            receipts = receipts,
            metrics = metrics,
            profile = profile
        )
    }
}
