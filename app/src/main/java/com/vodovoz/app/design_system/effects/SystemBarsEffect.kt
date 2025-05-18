package com.vodovoz.app.design_system.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.vodovoz.app.util.extensions.window


@Composable
fun SystemBarsEffect(
    statusBarColor: Color,
    navigationBarColor: Color,
    darkIcons: Boolean = false,
) {
    val context = LocalContext.current
    val window = context.window() ?: return


    val systemUiController = rememberSystemUiController()

    val prevStatusBarColor = window.statusBarColor
    val prevNavBarColor    = window.navigationBarColor

    DisposableEffect(window, statusBarColor, navigationBarColor, darkIcons) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = darkIcons
        )
        systemUiController.setNavigationBarColor(
            color = navigationBarColor,
            darkIcons = darkIcons
        )

        onDispose {
            systemUiController.setStatusBarColor(
                color = Color(prevStatusBarColor),
                darkIcons = !darkIcons
            )
            systemUiController.setNavigationBarColor(
                color = Color(prevNavBarColor),
                darkIcons = !darkIcons
            )

            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }
}