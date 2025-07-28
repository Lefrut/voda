package com.vodovoz.app.util.extensions

import android.os.Build
import android.view.Window
import androidx.core.view.WindowCompat

fun Window.setSystemBarColors(
    statusColor: Int,
    navigationColor: Int,
    lightStatusBarIcons: Boolean = true,
    lightNavigationBarIcons: Boolean = true,
) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.isAppearanceLightStatusBars = !lightStatusBarIcons
    insetsController.isAppearanceLightNavigationBars = !lightNavigationBarIcons
    statusBarColor = statusColor
    navigationBarColor = navigationColor
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
}
