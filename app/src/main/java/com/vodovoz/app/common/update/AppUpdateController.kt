package com.vodovoz.app.common.update

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext


class AppUpdateController @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    @Assisted
    private val onDownLoadComplete: () -> Unit,
) {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private val listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            onDownLoadComplete()
        }
    }

    fun checkForUpdate(activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.registerListener(listener)
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    fun completeUpdate() {
        appUpdateManager.completeUpdate()
        appUpdateManager.unregisterListener(listener)
    }

    fun onResumeAction() {
        appUpdateManager
            .appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onDownLoadComplete()
                }
            }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted onDownLoadComplete: () -> Unit): AppUpdateController
    }

    companion object {
        private const val DAYS_FOR_FLEXIBLE_UPDATE = 3
    }


}