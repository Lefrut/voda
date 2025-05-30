package com.vodovoz.app.util.extensions

import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION
import com.vodovoz.app.BuildConfig


fun Context.isTablet(): Boolean {
    val xlarge =
        this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
    val large =
        this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
    return xlarge || large
}

fun Context.getDeviceInfo(): String {
    val builder = StringBuilder()
    if (isTablet()) {
        builder.append("Планшет Android:")
    } else {
        builder.append("Телефон Android:")
    }
    builder
        .append(" ").append(VERSION.RELEASE)
        .append(" ").append("Версия:").append(" ").append(BuildConfig.VERSION_NAME)
    return builder.toString()
}