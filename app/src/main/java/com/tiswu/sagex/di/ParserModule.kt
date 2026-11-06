package com.tiswu.sagex.di

import com.tiswu.sagex.domain.NotificationParser
import com.tiswu.sagex.notification.WeChatNotificationParser
import com.tiswu.sagex.notification.WhatsAppNotificationParser
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds notification parsers into a set so multiple source apps can be captured
 * without changing the notification listener service.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @IntoSet
    abstract fun bindWeChatParser(impl: WeChatNotificationParser): NotificationParser

    @Binds
    @IntoSet
    abstract fun bindWhatsAppParser(impl: WhatsAppNotificationParser): NotificationParser
}
