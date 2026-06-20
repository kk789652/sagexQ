package com.tiswu.sagex.service

/** A candidate autostart/background-launch settings screen, as (package, activity class). */
data class AutostartComponent(val packageName: String, val className: String)

/**
 * Pure mapping from `Build.MANUFACTURER` to candidate OEM autostart settings screens.
 *
 * These component names are undocumented and vary by version, so callers MUST verify
 * resolvability at runtime and fall back to guidance if none resolve. Kept pure so the
 * mapping is unit-testable without a device.
 */
object AutostartIntents {

    fun candidatesFor(manufacturer: String): List<AutostartComponent> =
        when (manufacturer.trim().lowercase()) {
            "xiaomi", "redmi", "poco" -> listOf(
                AutostartComponent(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity",
                ),
            )
            "huawei", "honor" -> listOf(
                AutostartComponent(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                ),
                AutostartComponent(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity",
                ),
            )
            "oppo", "realme" -> listOf(
                AutostartComponent(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                ),
                AutostartComponent(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity",
                ),
            )
            "vivo", "iqoo" -> listOf(
                AutostartComponent(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                ),
                AutostartComponent(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
                ),
            )
            "samsung" -> listOf(
                AutostartComponent(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity",
                ),
            )
            "oneplus" -> listOf(
                AutostartComponent(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
                ),
            )
            else -> emptyList()
        }
}
