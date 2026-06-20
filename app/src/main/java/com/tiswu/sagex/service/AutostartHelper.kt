package com.tiswu.sagex.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Resolves a best-effort intent to the device's OEM autostart settings screen. */
class AutostartHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    sealed interface Result {
        /** A resolvable autostart settings intent was found. */
        data class Available(val intent: Intent) : Result

        /** No known/resolvable autostart screen on this device. */
        data object None : Result
    }

    fun resolve(manufacturer: String = Build.MANUFACTURER): Result {
        for (candidate in AutostartIntents.candidatesFor(manufacturer)) {
            val intent = Intent()
                .setComponent(ComponentName(candidate.packageName, candidate.className))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context.packageManager.resolveActivity(intent, 0) != null) {
                return Result.Available(intent)
            }
        }
        return Result.None
    }
}
