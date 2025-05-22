package com.vodovoz.app.ui.extensions

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Build.VERSION
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.R


object ContextExtensions {

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

}