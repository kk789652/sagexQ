package com.tiswu.sagex.di

import com.tiswu.sagex.domain.NotificationParser
import com.tiswu.sagex.notification.WeChatNotificationParser
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds notification parsers into a set so additional source apps can be added later
 * (multi-source extension point) without touching the listener service.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @IntoSet
    abstract fun bindWeChatParser(impl: WeChatNotificationParser): NotificationParser
}
