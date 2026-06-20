package com.tiswu.sagex.ui.util

import android.content.Context
import android.content.Intent

/** Shares plain text via the Android system share sheet. */
fun shareText(context: Context, text: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(send, "分享到"))
}
