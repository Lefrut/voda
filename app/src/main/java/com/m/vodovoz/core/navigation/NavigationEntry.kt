package com.m.vodovoz.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.platform.LocalView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.ui.mvi.MviViewModel

@Composable
inline fun <reified VM : MviViewModel<*, *>> NavigationEntry(
    crossinline screen: @Composable NavigationEntryScope<VM>.() -> Unit,
) {
    VodovozTheme {
        val viewModel = hiltViewModel<VM>()
        val navController: NavController = LocalView.current.findNavController()
        NavigationEntryScope(viewModel, navController).screen()
    }
}

@Immutable
data class NavigationEntryScope<VM: MviViewModel<*, *>>(
    val viewModel: VM,
    val navController: NavController,
)