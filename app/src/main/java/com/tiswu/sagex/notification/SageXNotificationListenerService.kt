package com.tiswu.sagex.notification

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.tiswu.sagex.BuildConfig
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.domain.NotificationParser
import com.tiswu.sagex.domain.ParseResult
import com.tiswu.sagex.domain.RawNotification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Receives system notifications, filters to supported source apps via the injected
 * parser set, and persists parsed messages. The system binds and keeps this service
 * alive while notification access is granted (process-persistence hardening is a
 * separate change).
 */
@AndroidEntryPoint
class SageXNotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var parsers: Set<@JvmSuppressWildcards NotificationParser>

    @Inject
    lateinit var repository: MessageRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val raw = sbn?.toRawNotification() ?: return
        if (BuildConfig.DEBUG && raw.sourcePackage == "com.tencent.mm") {
            logWeChatNotification(sbn, raw)
        }
        val parser = parsers.firstOrNull { it.supports(raw.sourcePackage) } ?: return
        when (val result = parser.parse(raw)) {
            is ParseResult.Parsed -> scope.launch { repository.save(result.message) }
            is ParseResult.ContentUnavailable -> Unit // aggregated/no content: skip persistence (see design)
            ParseResult.Ignored -> Unit
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /** Debug-only: dump the raw WeChat notification fields so any device/version format can be confirmed. */
    private fun logWeChatNotification(sbn: StatusBarNotification, raw: RawNotification) {
        val extras = sbn.notification?.extras
        val lines = extras?.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" | ") { it.toString() }
        val hasMessages = extras?.containsKey(Notification.EXTRA_MESSAGES) == true
        val subText = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        Log.d(
            TAG,
            "WeChat notif: title=[${raw.title}] text=[${raw.text}] subText=[$subText] " +
                "lines=[$lines] hasMessages=$hasMessages ticker=[${raw.tickerText}]",
        )
    }

    private fun StatusBarNotification.toRawNotification(): RawNotification? {
        val extras = notification?.extras ?: return null
        return RawNotification(
            sourcePackage = packageName,
            title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
            text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
            lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                ?.map { it.toString() }
                .orEmpty(),
            tickerText = notification?.tickerText?.toString(),
            postedAt = postTime,
        )
    }

    private companion object {
        const val TAG = "SageXCapture"
    }
}
