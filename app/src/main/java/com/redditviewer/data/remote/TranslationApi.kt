package com.redditviewer.data.remote

import retrofit2.Response
import retrofit2.http.*

interface TranslationApi {
    
    @POST("translate")
    suspend fun translateText(
        @Header("Authorization") apiKey: String,
        @Body request: TranslationRequest
    ): Response<TranslationResponse>
}

data class TranslationRequest(
    val text: String,
    val source: String = "en",
    val target: String = "ja"
)

data class TranslationResponse(
    val translatedText: String,
    val sourceLanguage: String? = null,
    val confidence: Float? = null
) 