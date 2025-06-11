package com.vodovoz.app.common.permissions

import android.Manifest
import androidx.fragment.app.FragmentActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class PermissionsController @AssistedInject constructor(
    private val permissionsManager: PermissionsManager,
    @Assisted private val activity: FragmentActivity,
) {

    @AfterPermissionGranted(PermissionsConstants.REQUEST_LOCATION_PERMISSION)
    fun methodRequiresLocationsPermission(failure: () -> Unit = {}, success: () -> Unit = {}) {
        val perms = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (EasyPermissions.hasPermissions(activity, *perms)) {
            success.invoke()
        } else if (!permissionsManager.getPermissionChecked(PermissionsConstants.REQUEST_LOCATION_PERMISSION)) {
            EasyPermissions.requestPermissions(
                activity, "Необходимы разрешения, чтобы использовать приложение.",
                PermissionsConstants.REQUEST_LOCATION_PERMISSION, *perms
            )
            failure.invoke()
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted activity: FragmentActivity): PermissionsController
    }
}