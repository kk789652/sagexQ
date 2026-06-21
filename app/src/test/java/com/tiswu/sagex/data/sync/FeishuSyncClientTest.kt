package com.tiswu.sagex.data.sync

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class FeishuSyncClientTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    @Test
    fun `syncMessage is no-op when app id is blank`() = runTest {
        val calls = AtomicInteger(0)
        val client = newClient(
            httpClient = OkHttpClient.Builder()
                .addInterceptor {
                    calls.incrementAndGet()
                    response(it, """{"tenant_access_token":"token","expire":7200}""")
                }
                .build(),
            appId = "",
        )

        client.syncMessage(message())

        assertEquals(0, calls.get())
    }

    @Test
    fun `getToken returns cached value without a second HTTP call`() = runTest {
        val tokenCalls = AtomicInteger(0)
        val client = newClient(
            httpClient = OkHttpClient.Builder()
                .addInterceptor {
                    tokenCalls.incrementAndGet()
                    response(it, """{"tenant_access_token":"cached-token","expire":7200}""")
                }
                .build(),
        )

        assertEquals("cached-token", client.getToken())
        assertEquals("cached-token", client.getToken())
        assertEquals(1, tokenCalls.get())
    }

    private fun newClient(
        httpClient: OkHttpClient,
        appId: String = "app-id",
    ): FeishuSyncClient = FeishuSyncClient(
        httpClient = httpClient,
        json = json,
        ioDispatcher = Dispatchers.Unconfined,
        appId = appId,
        appSecret = "app-secret",
        appToken = "app-token",
        messagesTableId = "table-id",
        tokenUrl = "https://example.test/token",
        recordBaseUrl = "https://example.test/apps",
    )

    private fun response(chain: Interceptor.Chain, body: String): Response = Response.Builder()
        .request(chain.request())
        .protocol(Protocol.HTTP_1_1)
        .code(200)
        .message("OK")
        .body(body.toResponseBody())
        .build()

    private fun message(): CapturedMessage = CapturedMessage(
        sourcePackage = "com.example.chat",
        conversationKey = "com.example.chat:DIRECT:Alice",
        conversationType = ConversationType.DIRECT,
        conversationTitle = "Alice",
        sender = "Alice",
        body = "hello",
        postedAt = 1_718_000_000_000L,
    )
}
