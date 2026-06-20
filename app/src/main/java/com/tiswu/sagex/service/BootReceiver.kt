package com.tiswu.sagex.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tiswu.sagex.data.prefs.KeepAlivePreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Restarts the keep-alive foreground service after boot, only when keep-alive is enabled. */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var keepAlivePreferences: KeepAlivePreferences

    @Inject
    lateinit var serviceController: ServiceController

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                if (keepAlivePreferences.isEnabled()) {
                    runCatching { serviceController.start() }
                        .onFailure {
                            // Some OEMs/versions still block boot-time FGS starts. The notification
                            // listener is re-bound by the system anyway, so capture still resumes.
                            Log.w(TAG, "Failed to start keep-alive service on boot", it)
                        }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
