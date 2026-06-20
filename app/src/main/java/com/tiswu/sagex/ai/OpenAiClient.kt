package com.tiswu.sagex.ai

import com.tiswu.sagex.data.prefs.ApiConfig
import com.tiswu.sagex.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/** Thin client for the OpenAI-compatible Chat Completions endpoint. */
@Singleton
class OpenAiClient @Inject constructor(
    private val httpClient: OkHttpClient,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /** Calls `{baseUrl}/chat/completions` and returns the assistant text, or a failure. */
    suspend fun summarize(config: ApiConfig, messages: List<ChatMessage>): Result<String> =
        withContext(ioDispatcher) {
            runCatching {
                val url = config.baseUrl.trimEnd('/') + "/chat/completions"
                val payload = json.encodeToString(
                    ChatCompletionRequest(model = config.model, messages = messages),
                )
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer ${config.apiKey}")
                    .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    val body = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        error("API 请求失败 (${response.code}): ${body.take(300)}")
                    }
                    json.decodeFromString<ChatCompletionResponse>(body).firstContent()
                        ?: error("API 返回内容为空")
                }
            }
        }

    private companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
