package com.vodovoz.app.core.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


suspend fun FusedLocationProviderClient.getLocationOrNull(context: Context): Location? {
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        return null
    }
    return suspendCancellableCoroutine { continuation ->
        lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }
}


fun Activity.handleLocationAvailability(
    onPermissionHave: () -> Unit,
    onPermissionNotHave: () -> Unit,
    onPermissionNotRational: () -> Unit,
    onGpsDisabled: () -> Unit,
) {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    val locationPermissionGranted = locationPermissionGranted

    val canRequestPermission =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || locationPermissions.any { perm ->
            shouldShowRequestPermissionRationale(perm)
        }


    when {
        locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true -> onGpsDisabled()

        locationPermissionGranted -> onPermissionHave()

        canRequestPermission && !locationPermissionGranted-> onPermissionNotHave()

        else -> onPermissionNotRational()
    }

}


