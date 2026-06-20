package com.tiswu.sagex.domain

/** Pure app package -> display-name mapping for source apps. */
fun appDisplayName(sourcePackage: String): String = when (sourcePackage) {
    "com.tencent.mm" -> "微信"
    else -> sourcePackage
}
