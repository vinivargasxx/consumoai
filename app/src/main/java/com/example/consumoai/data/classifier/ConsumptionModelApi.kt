package com.example.consumoai.data.classifier
import retrofit2.http.Body
import retrofit2.http.POST
interface ConsumptionModelApi {
    @POST("predict")
    suspend fun predict(
        @Body request: ModelPredictionRequestDto
    ): ModelPredictionResponseDto
}
