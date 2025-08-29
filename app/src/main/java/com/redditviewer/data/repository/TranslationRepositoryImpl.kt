package com.redditviewer.data.repository

import com.redditviewer.BuildConfig
import com.redditviewer.data.remote.TranslationApi
import com.redditviewer.data.remote.TranslationRequest
import com.redditviewer.domain.repository.TranslationRepository
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranslationRepositoryImpl @Inject constructor(
    private val translationApi: TranslationApi
) : TranslationRepository {

    // メモリキャッシュ（簡易実装）
    private val translationCache = ConcurrentHashMap<String, String>()

    override suspend fun translateText(text: String, source: String, target: String): Result<String> {
        // 空文字や短いテキストはそのまま返す
        if (text.isBlank() || text.length < 3) {
            return Result.success(text)
        }

        // キャッシュから検索
        getCachedTranslation(text)?.let { cached ->
            return Result.success(cached)
        }

        return try {
            val response = translationApi.translateText(
                apiKey = "Bearer ${BuildConfig.TRANSLATION_API_KEY}",
                request = TranslationRequest(
                    text = text,
                    source = source,
                    target = target
                )
            )

            if (response.isSuccessful) {
                val translatedText = response.body()?.translatedText
                if (!translatedText.isNullOrBlank()) {
                    // キャッシュに保存
                    cacheTranslation(text, translatedText)
                    Result.success(translatedText)
                } else {
                    Result.success(text) // 翻訳に失敗した場合は元のテキストを返す
                }
            } else {
                // API呼び出しに失敗した場合は元のテキストを返す
                Result.success(text)
            }
        } catch (e: Exception) {
            // エラーが発生した場合も元のテキストを返す（翻訳は必須機能ではないため）
            Result.success(text)
        }
    }

    override fun getCachedTranslation(text: String): String? {
        return translationCache[text.trim()]
    }

    override suspend fun cacheTranslation(originalText: String, translatedText: String) {
        translationCache[originalText.trim()] = translatedText
        
        // キャッシュサイズ制限（1000エントリまで）
        if (translationCache.size > 1000) {
            val keysToRemove = translationCache.keys.take(200) // 古いエントリを200個削除
            keysToRemove.forEach { translationCache.remove(it) }
        }
    }
} 