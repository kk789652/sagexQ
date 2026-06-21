package com.tiswu.sagex.data.sync

import com.tiswu.sagex.di.IoDispatcher
import com.tiswu.sagex.domain.model.CapturedMessage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FeishuSyncClient(
    private val httpClient: OkHttpClient,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val appId: String,
    private val appSecret: String,
    private val appToken: String,
    private val messagesTableId: String,
    private val tokenUrl: String = TOKEN_URL,
    private val recordBaseUrl: String = RECORD_BASE_URL,
) {

    private val tokenMutex = Mutex()
    private var cachedToken: String? = null
    private var tokenExpiresAtMillis: Long = 0L

    suspend fun syncMessage(message: CapturedMessage) {
        if (appId.isBlank()) return

        withContext(ioDispatcher) {
            val token = getToken()
            val payload = json.encodeToString(
                RecordCreateRequest(
                    fields = RecordFields(
                        app = message.sourcePackage,
                        conversationTitle = message.conversationTitle,
                        sender = message.sender.orEmpty(),
                        body = message.body,
                        postedAt = message.postedAt,
                        processed = false,
                    ),
                ),
            )
            val request = Request.Builder()
                .url("$recordBaseUrl/$appToken/tables/$messagesTableId/records")
                .addHeader("Authorization", "Bearer $token")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("Feishu record create failed (${response.code}): ${body.take(300)}")
                }
            }
        }
    }

    internal suspend fun getToken(): String = withContext(ioDispatcher) {
        cachedToken?.takeIf { System.currentTimeMillis() < tokenExpiresAtMillis }?.let { return@withContext it }

        tokenMutex.withLock {
            cachedToken?.takeIf { System.currentTimeMillis() < tokenExpiresAtMillis }?.let { return@withLock it }

            val payload = json.encodeToString(TokenRequest(appId = appId, appSecret = appSecret))
            val request = Request.Builder()
                .url(tokenUrl)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    error("Feishu token request failed (${response.code}): ${body.take(300)}")
                }
                val tokenResponse = json.decodeFromString<TokenResponse>(body)
                cachedToken = tokenResponse.tenantAccessToken
                tokenExpiresAtMillis = System.currentTimeMillis() + (tokenResponse.expire - 60) * 1_000L
                tokenResponse.tenantAccessToken
            }
        }
    }

    private companion object {
        const val TOKEN_URL = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal"
        const val RECORD_BASE_URL = "https://open.feishu.cn/open-apis/bitable/v1/apps"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}

@Serializable
private data class TokenRequest(
    @SerialName("app_id") val appId: String,
    @SerialName("app_secret") val appSecret: String,
)

@Serializable
private data class TokenResponse(
    @SerialName("tenant_access_token") val tenantAccessToken: String,
    val expire: Long,
)

@Serializable
private data class RecordCreateRequest(
    val fields: RecordFields,
)

@Serializable
private data class RecordFields(
    val app: String,
    @SerialName("conversation_title") val conversationTitle: String,
    val sender: String,
    val body: String,
    @SerialName("posted_at") val postedAt: Long,
    val processed: Boolean,
)
