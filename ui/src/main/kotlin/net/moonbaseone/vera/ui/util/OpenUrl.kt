package net.moonbaseone.vera.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

private const val TAG = "VeraOpenUrl"

/** Opens [url] in the default browser / handler. No-op if nothing can handle it. */
fun Context.openExternalUrl(url: String) {
    val trimmed = url.trim()
    if (trimmed.isEmpty()) return
    try {
        startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
        )
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "No app to open $trimmed", e)
    } catch (e: Exception) {
        Log.w(TAG, "Failed to open $trimmed", e)
    }
}
