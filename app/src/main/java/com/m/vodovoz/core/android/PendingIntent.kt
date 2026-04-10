package com.m.vodovoz.core.android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.m.vodovoz.data.parser.common.safeString
import com.m.vodovoz.ui.base.MainActivity
import org.json.JSONObject

fun Context.getNotificationPendingIntent(
    data: JSONObject
): PendingIntent {
    val intent = Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        data.keys().forEach { key -> putExtra(key, data.safeString(key)) }
    }

    return PendingIntent.getActivity(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
