package com.tiswu.sagex.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Starts/stops the keep-alive foreground service. */
@Singleton
class ServiceController @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun start() {
        ContextCompat.startForegroundService(context, intent())
    }

    fun stop() {
        context.stopService(intent())
    }

    private fun intent() = Intent(context, SageXForegroundService::class.java)
}
