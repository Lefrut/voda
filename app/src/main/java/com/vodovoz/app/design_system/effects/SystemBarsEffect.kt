package com.vodovoz.app.design_system.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vodovoz.app.util.extensions.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun SystemBarsEffect(
    statusBarColor: Color,
    navigationBarColor: Color,
    navigationBarContrastEnforced: Boolean = true,
    darkIcons: Boolean = false,
    handleDecorFitsSystemWindows: Boolean = true,
    delayTimeMillis: Long = 0L,
) {
    val context = LocalContext.current
    val window = context.window() ?: return
    val coroutineScope = rememberCoroutineScope()


    val systemUiController = rememberSystemUiController()

    val prevStatusBarColor = window.statusBarColor
    val prevNavBarColor = window.navigationBarColor

    LifecycleStartEffect(statusBarColor, navigationBarColor, darkIcons) {

        if (handleDecorFitsSystemWindows) {
            coroutineScope.launch {
                delay(delayTimeMillis)
                WindowCompat.setDecorFitsSystemWindows(window, false)
            }

        }

        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = darkIcons
        )
        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = darkIcons
        )

        onStopOrDispose {
            val prevStatusBarColorsCompose = Color(prevStatusBarColor)
            systemUiController.setStatusBarColor(
                color = prevStatusBarColorsCompose,
                darkIcons = prevStatusBarColorsCompose.luminance() > 0.5f,
            )

            val prevNavigationBarColorCompose = Color(prevNavBarColor)
            systemUiController.setNavigationBarColor(
                color = prevNavigationBarColorCompose,
                darkIcons = prevNavigationBarColorCompose.luminance() > 0.5f,
                navigationBarContrastEnforced = navigationBarContrastEnforced
            )

            if (handleDecorFitsSystemWindows) {
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }
}