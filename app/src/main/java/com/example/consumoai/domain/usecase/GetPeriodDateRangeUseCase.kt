package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.model.ConsumptionPeriod
import java.time.LocalDate

class GetPeriodDateRangeUseCase {

    operator fun invoke(period: ConsumptionPeriod): Pair<LocalDate, LocalDate> {
        val endDate = LocalDate.now()

        val startDate = when (period) {
            ConsumptionPeriod.LAST_30_DAYS -> endDate.minusDays(30)
            ConsumptionPeriod.LAST_3_MONTHS -> endDate.minusMonths(3)
            ConsumptionPeriod.LAST_6_MONTHS -> endDate.minusMonths(6)
        }

        return startDate to endDate
    }
}

