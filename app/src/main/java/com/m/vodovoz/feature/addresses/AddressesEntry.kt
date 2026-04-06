package com.m.vodovoz.feature.addresses

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToAddAddress
import com.m.vodovoz.core.navigation.navigateToMap
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.addresses.api.AddressesNavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.cart.ordering.OrderingFlowViewModel
import com.m.vodovoz.ui.mvi.collectAsState
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory

@Composable
fun AddressesEntry(navKey: AddressesNavKey) =
    NavigationEntry<AddressesFlowViewModel, AddressesFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
        val viewState by viewModel.collectAsState()
        val lifecycleOwner = LocalLifecycleOwner.current
        val orderingViewModel = if (viewState.screenType == AddressScreenTypeUi.Choose) {
            viewModel(modelClass = OrderingFlowViewModel::class)
        } else {
            null
        }
        val mapKit: MapKit = MapKitFactory.getInstance()

        LifecycleStartEffect(viewState.screenType) {
            when (viewState.screenType) {
                AddressScreenTypeUi.Add -> Unit
                AddressScreenTypeUi.Choose -> {
                    mapKit.onStart()
                    viewModel.tabManager.setTabVisibility(false)
                }
            }
            onStopOrDispose {
                mapKit.onStop()
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.refresh()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        AddressesScreen(
            viewModel = viewModel,
            viewState = viewState
        )

        BackHandler {
            viewModel.navigateBack()
        }

        LifecycleEffect {
            viewModel.events.collect { event ->
                when (event) {
                    is AddressesFlowViewModel.AddressesEvents.GoBack -> {
                        navigator.goBack()
                    }

                    AddressesFlowViewModel.AddressesEvents.GoToMap -> {
                        navigator.navigateToMap(null)
                    }

                    is AddressesFlowViewModel.AddressesEvents.GoToEditAddress -> {
                        navigator.navigateToAddAddress(
                            addressId = event.addressId,
                            addressName = event.addressName
                        )
                    }

                    is AddressesFlowViewModel.AddressesEvents.GoBackToOrdering -> {
                        orderingViewModel?.setAddress(event.address)
                        navigator.goBack()
                    }
                }
            }
        }
    }
