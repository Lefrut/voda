package com.m.vodovoz.feature.addresses.add

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToMap
import com.m.vodovoz.core.navigation.slideAnim
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.addresses.add.api.AddAddressNavKey
import com.m.vodovoz.feature.addresses.add.composables.AddAddressScreen
import com.m.vodovoz.feature.addresses.add.model.AddAddressEvent
import com.m.vodovoz.feature.map.model.MapAddressUi
import com.m.vodovoz.ui.mvi.collectAsState
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch

@Composable
fun AddAddressEntry(navKey: AddAddressNavKey? = null) =
    NavigationEntry<AddAddressViewModel, AddAddressViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
        val viewState by viewModel.collectAsState()
        val mainScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        val mapKit: MapKit = remember { MapKitFactory.getInstance() }

        LifecycleStartEffect(Unit) {
            mapKit.onStart()
            onStopOrDispose {
                mapKit.onStop()
            }
        }

        AddAddressScreen(
            viewModel = viewModel,
            viewState = viewState,
            snackbarHostState = snackbarHostState
        )

        LifecycleEffect(snackbarHostState) {
            observeEvents(mainScope, snackbarHostState)
        }

        BackHandler {
            viewModel.navigateBack()
        }
    }

private suspend fun com.m.vodovoz.core.navigation.NavigationEntryScope<AddAddressViewModel>.observeEvents(
    mainScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
) {
    viewModel.events.onSubscription {
    }.collect { event ->
        when (event) {
            AddAddressEvent.GoBackToMap -> {
                navigator.popBackStack(
                    destinationId = R.id.mapFragment,
                    inclusive = false,
                    saveState = true
                )
            }

            is AddAddressEvent.GoToMap -> {
                navigator.navigateToMap(
                    event.addressName,
                    navOptions {
                        slideAnim()
                        launchSingleTop = true
                    }
                )
            }

            is AddAddressEvent.ShowSnackbar -> {
                mainScope.launch {
                    snackbarHostState.showSnackbar(event.message)
                }
            }

            AddAddressEvent.GoBackToAddresses -> {
                navigator.popBackStack(
                    destinationId = R.id.addressesFragment,
                    inclusive = false,
                    saveState = false
                )
            }
        }
    }
}
