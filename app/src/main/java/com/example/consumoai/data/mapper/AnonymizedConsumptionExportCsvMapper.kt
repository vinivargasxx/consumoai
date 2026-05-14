package com.example.consumoai.data.mapper

import com.example.consumoai.domain.model.AnonymizedConsumptionExport

fun AnonymizedConsumptionExport.toCsvHeader(): String {
    val baseHeaders = listOf("input_version", "final_profile", "confidence", "source", "fallback_reason")
    val featureHeaders = featureSnapshot.keys.sorted()
    val scoreHeaders = profileScores.keys.sorted().map { "score_$it" }
    val metricHeaders = aggregatedMetrics.keys.sorted().map { "metric_$it" }
    return (baseHeaders + featureHeaders + scoreHeaders + metricHeaders).joinToString(",")
}

fun AnonymizedConsumptionExport.toCsvRow(): String {
    val baseValues = listOf(
        inputVersion,
        finalProfile,
        confidence.toString(),
        source,
        fallbackReason.orEmpty()
    )
    val featureValues = featureSnapshot.toSortedMap().values.map { it.toString() }
    val scoreValues = profileScores.toSortedMap().values.map { it.toString() }
    val metricValues = aggregatedMetrics.toSortedMap().values.map { it.toString() }
    return (baseValues + featureValues + scoreValues + metricValues)
        .joinToString(",") { it.escapeCsv() }
}

private fun String.escapeCsv(): String {
    return if (contains(',') || contains('"') || contains('\n')) {
        '"' + replace("\"", "\"\"") + '"'
    } else {
        this
    }
}

