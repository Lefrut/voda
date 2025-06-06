package com.vodovoz.app.core.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

val locationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)


 val Context.locationPermissionsGranted
    get() = locationPermissions.any { perm ->
        ContextCompat.checkSelfPermission(
            this,
            perm
        ) == PackageManager.PERMISSION_GRANTED
    }
