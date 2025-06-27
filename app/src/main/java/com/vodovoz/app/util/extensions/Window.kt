package com.vodovoz.app.util.extensions

import android.view.Window
import androidx.core.view.WindowCompat

fun Window.setSystemBarIconColors(
    lightStatusBarIcons: Boolean = true,
    lightNavigationBarIcons: Boolean = true
) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)

    insetsController.isAppearanceLightStatusBars = lightStatusBarIcons
    insetsController.isAppearanceLightNavigationBars = lightNavigationBarIcons
}
