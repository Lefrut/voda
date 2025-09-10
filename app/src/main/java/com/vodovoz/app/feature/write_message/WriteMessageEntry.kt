package com.vodovoz.app.feature.write_message

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.vodovoz.app.core.navigation.NavigationEntry
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.write_message.model.WriteMessageEvent
import com.vodovoz.app.ui.mvi.collectAsState

@Composable
fun WriteMessageEntry(
) = NavigationEntry { viewModel: WriteMessageViewModel, navController ->
    val viewState by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }


    WriteMessageScreen(
        viewModel = viewModel,
        viewState = viewState,
        snackbarHostState = snackbarHostState
    )

    LifecycleEffect {
        viewModel.events.collect { event ->
            when (event) {
                WriteMessageEvent.GoBack -> {
                    navController.popBackStack()
                }

                is WriteMessageEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is WriteMessageEvent.GoToWebView -> {
                    navController.navigateToWebView(
                        event.url,
                        event.title
                    )
                }
            }
        }
    }
}