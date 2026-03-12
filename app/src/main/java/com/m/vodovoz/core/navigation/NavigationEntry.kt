package com.m.vodovoz.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.feature.main.Navigator
import com.m.vodovoz.ui.mvi.MviViewModel

@Composable
inline fun <reified VM : MviViewModel<*, *>> NavigationEntry(
    crossinline screen: @Composable NavigationEntryScope<VM>.() -> Unit,
) {
    VodovozTheme {
        val viewModel = hiltViewModel<VM>()
        val navigator = LocalNavigator.current
        NavigationEntryScope(viewModel, navigator).screen()
    }
}

@Immutable
data class NavigationEntryScope<VM : MviViewModel<*, *>>(
    val viewModel: VM,
    val navigator: Navigator,
) {
    val navController: NavController
        get() = navigator.navController
}
