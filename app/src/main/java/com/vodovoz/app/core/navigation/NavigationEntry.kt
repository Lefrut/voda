package com.vodovoz.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.ui.mvi.MviViewModel
import kotlin.properties.ReadOnlyProperty

@Composable
inline fun <reified VM : MviViewModel<*, *>> NavigationEntry(
    crossinline screen: @Composable (
        viewModel: VM,
        navController: NavController,
    ) -> Unit,
) {
    VodovozTheme {
        val viewModel = hiltViewModel<VM>()
        val navController: NavController = LocalView.current.findNavController()

        screen(viewModel, navController)
    }
}