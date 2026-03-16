package com.m.vodovoz.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.hilt.navigation.compose.hiltViewModel
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.feature.main.Navigator
import com.m.vodovoz.ui.mvi.MviViewModel

@Composable
inline fun <reified VM : MviViewModel<*, *>> NavigationEntry(
    crossinline screen: @Composable NavigationEntryScope<VM>.() -> Unit,
) {
    NavigationEntryContent(
        viewModel = hiltViewModel<VM>(),
        screen = screen
    )
}

@Composable
inline fun <reified VM : MviViewModel<*, *>, reified VMF> NavigationEntry(
    noinline creationCallback: (VMF) -> VM,
    crossinline screen: @Composable NavigationEntryScope<VM>.() -> Unit,
) {
    NavigationEntryContent(
        viewModel = hiltViewModel<VM, VMF>(creationCallback = creationCallback),
        screen = screen
    )
}

@Composable
inline fun <reified VM : MviViewModel<*, *>> NavigationEntryContent(
    viewModel: VM,
    crossinline screen: @Composable NavigationEntryScope<VM>.() -> Unit,
) {
    VodovozTheme {
        val navigator = LocalNavigator.current
        NavigationEntryScope(viewModel, navigator).screen()
    }
}

@Immutable
data class NavigationEntryScope<VM : MviViewModel<*, *>>(
    val viewModel: VM,
    val navigator: Navigator,
)
