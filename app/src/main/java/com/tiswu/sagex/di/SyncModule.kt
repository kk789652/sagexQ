package com.tiswu.sagex.di

import com.tiswu.sagex.BuildConfig
import com.tiswu.sagex.data.sync.FeishuSyncClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    @Provides
    @Singleton
    fun provideFeishuSyncClient(
        httpClient: OkHttpClient,
        json: Json,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): FeishuSyncClient = FeishuSyncClient(
        httpClient = httpClient,
        json = json,
        ioDispatcher = ioDispatcher,
        appId = BuildConfig.FEISHU_APP_ID,
        appSecret = BuildConfig.FEISHU_APP_SECRET,
        appToken = BuildConfig.FEISHU_APP_TOKEN,
        messagesTableId = BuildConfig.FEISHU_MESSAGES_TABLE_ID,
    )
}
