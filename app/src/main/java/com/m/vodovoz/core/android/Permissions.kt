package com.m.vodovoz.core.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)


 val Context.locationPermissionGranted
    get() = locationPermissions.any { perm ->
        ContextCompat.checkSelfPermission(
            this,
            perm
        ) == PackageManager.PERMISSION_GRANTED
    }


val Context.notificationPermissionGranted: Boolean
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        get() = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED