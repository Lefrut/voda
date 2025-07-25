package com.vodovoz.app.design_system.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.vodovoz.app.util.extensions.window

@Suppress("NonSkippableComposable")
@Composable
fun AppearanceSystemBarsEffect(
    vararg keys: Any?,
    lightStatusBar: Boolean = true,
    lightNavigationBar: Boolean = true
) {
    val context = LocalContext.current
    val window = context.window() ?: return
    val windowInsetsController= remember {
        WindowCompat.getInsetsController(window, window.decorView)
    }

    LifecycleStartEffect(keys) {
        val prevAppearanceLightStateBars = windowInsetsController.isAppearanceLightStatusBars
        val prevAppearanceLightNavigationBars = windowInsetsController.isAppearanceLightNavigationBars

        windowInsetsController.isAppearanceLightStatusBars = lightStatusBar
        windowInsetsController.isAppearanceLightNavigationBars = lightNavigationBar

        onStopOrDispose {
            windowInsetsController.isAppearanceLightStatusBars = prevAppearanceLightStateBars
            windowInsetsController.isAppearanceLightNavigationBars = prevAppearanceLightNavigationBars
        }
    }
}