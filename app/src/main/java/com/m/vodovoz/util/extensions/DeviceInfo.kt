package com.m.vodovoz.util.extensions

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Build.VERSION
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.R


fun Context.isTablet(): Boolean {
    val xlarge =
        resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_XLARGE
    val large =
        resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK == Configuration.SCREENLAYOUT_SIZE_LARGE
    return xlarge || large
}

fun Context.deviceInfo(): String{
    val deviceTypeRes = if (isTablet()) R.string.device_type_tablet else R.string.device_type_phone
    val deviceType = getString(deviceTypeRes)

    val androidVersion = VERSION.RELEASE
    val appVersion = BuildConfig.VERSION_NAME

    return getString(R.string.device_info, deviceType, androidVersion, appVersion)
}
