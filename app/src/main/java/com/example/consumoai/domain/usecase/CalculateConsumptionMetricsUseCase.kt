package com.example.consumoai.domain.usecase

import com.example.consumoai.domain.classifier.ProductSemanticTagger
import com.example.consumoai.domain.model.CategoryMetrics
import com.example.consumoai.domain.model.ConsumptionMetrics
import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductSemanticTag
import com.example.consumoai.domain.model.ProductItem
import com.example.consumoai.domain.model.Receipt
import java.text.Normalizer
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class CalculateConsumptionMetricsUseCase(
    private val semanticTagger: ProductSemanticTagger = object : ProductSemanticTagger {
        override fun tagsFor(item: ProductItem): Set<ProductSemanticTag> = emptySet()
    }
) {

    private companion object {
        const val ALCOHOL_AUDIT_TAG = "ALCOHOL_AUDIT"
    }

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
                diversityScore = 0.0,
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
                softDrinkFrequency = 0.0,
                softDrinkValuePct = 0.0,
                nonAlcoholicBeverageFrequency = 0.0,
                nonAlcoholicBeverageValuePct = 0.0,
                alcoholicBeverageFrequency = 0.0,
                alcoholicBeverageValuePct = 0.0,
                beverageSnackCoOccurrenceFrequency = 0.0,
                nonAlcoholicBeverageSnackCoOccurrenceFrequency = 0.0,
                alcoholSnackCoOccurrenceFrequency = 0.0,
                energyDrinkFrequency = 0.0,
                energyDrinkValuePct = 0.0,
                snackSweetFrequency = 0.0,
                snackSweetValuePct = 0.0,
                frozenConvenienceValuePct = 0.0,
                frozenConvenienceFrequency = 0.0,
                dairyValuePct = 0.0,
                meatProteinValuePct = 0.0,
                freshProduceValuePct = 0.0,
                convenienceMealValuePct = 0.0,
                convenienceMealFrequency = 0.0,
                hygieneCleaningCoOccurrenceFrequency = 0.0,
                basicProduceCoOccurrenceFrequency = 0.0,
                essentialRoutineScore = 0.0,
                convenienceRoutineScore = 0.0,
                householdRoutineScore = 0.0,
                freshFoodPresenceScore = 0.0
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

        // === TEMPORAL/TEMPORAL PATTERN ===
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

        val highTicketReceiptsPercentageTemporal = receipts.count { it.totalValue > averageTicket }.toDouble() / receipts.size
        val lowTicketReceiptsPercentageTemporal = receipts.count { it.totalValue < averageTicket }.toDouble() / receipts.size

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

        val alcoholicReceiptItems = receipts.mapNotNull { receipt ->
            val alcoholicItems = receipt.items.mapNotNull { item ->
                val tags = semanticTagger.tagsFor(item)
                if (tags.contains(ProductSemanticTag.ALCOHOLIC_BEVERAGE)) {
                    item to tags
                } else {
                    null
                }
            }
            if (alcoholicItems.isEmpty()) null else receipt to alcoholicItems
        }
        safeAuditLog("receipts_with_alcohol=${alcoholicReceiptItems.size}/${receipts.size}")
        alcoholicReceiptItems.forEach { (receipt, alcoholicItems) ->
            safeAuditLog("receipt_id=${receipt.id} alcoholic_items=${alcoholicItems.size}")
            alcoholicItems.forEach { (item, tags) ->
                val formattedValue = String.format(Locale.US, "%.2f", item.price)
                val tagNames = tags.map { it.name }.sorted().joinToString(",")
                safeAuditLog(
                    "receipt_id=${receipt.id} product=\"${item.name}\" value=$formattedValue tags=[$tagNames]"
                )
            }
        }

        val receiptTagSets = receipts.map { receipt -> receipt.items.flatMap { semanticTagger.tagsFor(it) }.toSet() }

        val beverageSnackCoOccurrenceFrequency = safeDivide(
            receiptTagSets.count { tags ->
                (tags.contains(ProductSemanticTag.ALCOHOLIC_BEVERAGE) || tags.contains(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)) &&
                    tags.contains(ProductSemanticTag.SNACK_OR_SWEET)
            }.toDouble(),
            receiptTagSets.size.toDouble()
        )
        val alcoholSnackCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.ALCOHOLIC_BEVERAGE, ProductSemanticTag.SNACK_OR_SWEET)
        val hygieneCleaningCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.PERSONAL_CARE, ProductSemanticTag.HOUSEHOLD_CLEANING)
        val basicProduceCoOccurrenceFrequency = receipts.count { receipt ->
            val categories = receipt.items.map { it.category }.toSet()
            categories.contains(ProductCategory.BASIC_FOOD) && categories.contains(ProductCategory.PRODUCE)
        }.toDouble() / receipts.size

        val totalValueForTags = receipts.sumOf { it.totalValue }.coerceAtLeast(0.00001)

        fun valuePctByTag(tag: ProductSemanticTag): Double {
            val value = receipts.flatMap { it.items }
                .filter { semanticTagger.tagsFor(it).contains(tag) }
                .sumOf { it.price }
            return safeDivide(value, totalValueForTags)
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
        val nonAlcoholicBeverageFrequency = freqByTag(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        val nonAlcoholicBeverageValuePct = valuePctByTag(ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE)
        val nonAlcoholicBeverageSnackCoOccurrenceFrequency = coOccurrence(receiptTagSets, ProductSemanticTag.NON_ALCOHOLIC_BEVERAGE, ProductSemanticTag.SNACK_OR_SWEET)
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
            frequencyByCategory.valueOf(ProductCategory.BASIC_FOOD),
            frequencyByCategory.valueOf(ProductCategory.PRODUCE),
            essentialCategoriesPercentage,
            basicProduceCoOccurrenceFrequency
        )

        val convenienceRoutineScore = averageOf(
            frequencyByCategory.valueOf(ProductCategory.INDUSTRIALIZED),
            snackSweetFrequency,
            frozenConvenienceFrequency,
            convenienceMealValuePct
        )


        val householdRoutineScore = averageOf(
            frequencyByCategory.valueOf(ProductCategory.HYGIENE),
            frequencyByCategory.valueOf(ProductCategory.CLEANING),
            hygieneCleaningCoOccurrenceFrequency
        )

        val freshFoodPresenceScore = averageOf(
            valuePercentageByCategory.valueOf(ProductCategory.PRODUCE),
            frequencyByCategory.valueOf(ProductCategory.PRODUCE),
            freshProduceValuePct,
            basicProduceCoOccurrenceFrequency
        )

        return ConsumptionMetrics(
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
            diversityScore = diversityScore,
            timeSpanDays = timeSpanDays,
            receiptsPerWeek = receiptsPerWeek,
            averageDaysBetweenReceipts = averageDaysBetweenReceipts,
            purchaseRegularityScore = purchaseRegularityScore,
            ticketStandardDeviation = ticketStandardDeviation,
            ticketVariationCoefficient = ticketVariationCoefficient,
            itemCountVariationCoefficient = itemCountVariationCoefficient,
            highTicketReceiptsPercentage = highTicketReceiptsPercentageTemporal,
            lowTicketReceiptsPercentage = lowTicketReceiptsPercentageTemporal,
            categoryStabilityScore = categoryStabilityScore,
            averageCategoryOverlapBetweenReceipts = averageCategoryOverlapBetweenReceipts,
            recurringItemRatio = recurringItemRatio,
            topItemRepetitionRate = topItemRepetitionRate,
            softDrinkFrequency = softDrinkFrequency,
            softDrinkValuePct = softDrinkValuePct,
            nonAlcoholicBeverageFrequency = nonAlcoholicBeverageFrequency,
            nonAlcoholicBeverageValuePct = nonAlcoholicBeverageValuePct,
            alcoholicBeverageFrequency = alcoholicBeverageFrequency,
            alcoholicBeverageValuePct = alcoholicBeverageValuePct,
            beverageSnackCoOccurrenceFrequency = beverageSnackCoOccurrenceFrequency,
            nonAlcoholicBeverageSnackCoOccurrenceFrequency = nonAlcoholicBeverageSnackCoOccurrenceFrequency,
            alcoholSnackCoOccurrenceFrequency = alcoholSnackCoOccurrenceFrequency,
            energyDrinkFrequency = energyDrinkFrequency,
            energyDrinkValuePct = energyDrinkValuePct,
            snackSweetFrequency = snackSweetFrequency,
            snackSweetValuePct = snackSweetValuePct,
            frozenConvenienceValuePct = frozenConvenienceValuePct,
            frozenConvenienceFrequency = frozenConvenienceFrequency,
            dairyValuePct = dairyValuePct,
            meatProteinValuePct = meatProteinValuePct,
            freshProduceValuePct = freshProduceValuePct,
            convenienceMealValuePct = convenienceMealValuePct,
            convenienceMealFrequency = convenienceMealFrequency,
            hygieneCleaningCoOccurrenceFrequency = hygieneCleaningCoOccurrenceFrequency,
            basicProduceCoOccurrenceFrequency = basicProduceCoOccurrenceFrequency,
            essentialRoutineScore = essentialRoutineScore,
            convenienceRoutineScore = convenienceRoutineScore,
            householdRoutineScore = householdRoutineScore,
            freshFoodPresenceScore = freshFoodPresenceScore
        )
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

    private fun List<Double>.averageOrZero(): Double = if (isEmpty()) 0.0 else average()

    private fun averageOf(vararg values: Double): Double {
        if (values.isEmpty()) return 0.0
        return values.map { if (it.isFinite()) it else 0.0 }.average().coerceIn(0.0, 1.0)
    }

    private fun safeAuditLog(message: String) {
        runCatching { println("$ALCOHOL_AUDIT_TAG $message") }
    }
}

