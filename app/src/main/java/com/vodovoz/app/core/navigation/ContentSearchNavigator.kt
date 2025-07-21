package com.vodovoz.app.core.navigation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext


class ContentSearchNavigator @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val navController: NavController,
    @Assisted private val resultCaller: ActivityResultCaller
) {

    private val cameraLauncher = resultCaller.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) navController.navigateToQrCode()
    }

    private val audioLauncher = resultCaller.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) navController.navigateToSpeechDialog()
    }

    fun navigateToImageSearch() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            navController.navigateToQrCode()
        } else {
            cameraLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    fun navigateToVoiceSearch() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            navController.navigateToSpeechDialog()
        } else {
            audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(
            navController: NavController,
            resultCaller: ActivityResultCaller
        ): ContentSearchNavigator
    }

}