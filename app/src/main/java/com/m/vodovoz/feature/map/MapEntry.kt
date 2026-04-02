package com.m.vodovoz.feature.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.m.vodovoz.core.android.getLocationOrNull
import com.m.vodovoz.core.android.handleLocationAvailability
import com.m.vodovoz.core.android.locationPermissions
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToAddAddress
import com.m.vodovoz.core.navigation.slideAnim
import com.m.vodovoz.feature.addresses.add.AddAddressViewModel
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.toMapPoint
import com.m.vodovoz.design_system.model.toPoint
import com.m.vodovoz.feature.map.api.MapNavKey
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.yandex_map.VodovozUserLocationListener
import com.m.vodovoz.ui.yandex_map.YandexMapUi
import com.m.vodovoz.ui.yandex_map.animMove
import com.m.vodovoz.ui.yandex_map.copy
import com.m.vodovoz.ui.yandex_map.minusZoom
import com.m.vodovoz.ui.yandex_map.plusZoom
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapEntry(navKey: MapNavKey? = null) =
    NavigationEntry<MapFlowViewModel, MapFlowViewModel.Factory>(
        creationCallback = { factory -> factory.create(navKey) }
    ) {
    val addAddressViewModel = when (navKey?.source) {
        MapNavKey.Source.AddAddress -> viewModel(modelClass = AddAddressViewModel::class)
        else -> null
    }
    val context = LocalContext.current
    val activity = context as? Activity
    val viewState by viewModel.collectAsState()

    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    val mapKit: MapKit = remember { MapKitFactory.getInstance() }
    val yandexMap = remember(context) {
        YandexMapUi(
            mapView = MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mapWindow.map.logo.setAlignment(
                    Alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP)
                )
            }
        )
    }

    val mapView: MapView = yandexMap.mapView
    val mapWindow: MapWindow = mapView.mapWindow
    val map: Map = mapWindow.map

    val userLocationListener = remember(context) { VodovozUserLocationListener(context) }
    val userLocationLayer: UserLocationLayer = remember(mapKit, mapWindow) {
        mapKit.createUserLocationLayer(mapWindow)
    }

    val moscowPoint = remember { Point(55.75, 37.62) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.moveToAvailableGeo()
        }
    }

    val anchoredDraggableState = rememberSaveable(saver = AnchoredDraggableState.Saver()) {
        AnchoredDraggableState(initialValue = PartiallyExpanded)
    }

    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    LifecycleStartEffect(Unit) {
        mapView.onStart()
        mapKit.onStart()
        onStopOrDispose {
            mapView.onStop()
            mapKit.onStop()
        }
    }

    DisposableEffect(userLocationLayer) {
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingModeActive = true
        userLocationLayer.setObjectListener(userLocationListener)
        onDispose { }
    }

    MapScreen(
        viewModel = viewModel,
        viewState = viewState,
        yandexMap = yandexMap,
        anchoredDraggableState = anchoredDraggableState
    )

    LifecycleEffect(anchoredDraggableState) {
        observeEvents(
            addAddressViewModel = addAddressViewModel,
            activity = activity,
            context = context,
            map = map,
            moscowPoint = moscowPoint,
            fusedLocationClient = fusedLocationClient,
            locationLauncher = locationPermissionLauncher,
            anchoredDraggableState = anchoredDraggableState,
            mainScope = coroutineScope,
            keyboardController = keyboardController
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun com.m.vodovoz.core.navigation.NavigationEntryScope<MapFlowViewModel>.observeEvents(
    addAddressViewModel: AddAddressViewModel?,
    activity: Activity?,
    context: android.content.Context,
    map: Map,
    moscowPoint: Point,
    fusedLocationClient: com.google.android.gms.location.FusedLocationProviderClient,
    locationLauncher: ActivityResultLauncher<Array<String>>,
    anchoredDraggableState: AnchoredDraggableState<SheetValue>,
    mainScope: CoroutineScope,
    keyboardController: SoftwareKeyboardController?,
): Unit =
    viewModel.events.onSubscription {
        delay(750L)
        viewModel.moveToAvailableGeo()
    }.collect { event ->
        when (event) {
            is MapFlowViewModel.MapFlowEvents.MoveToAddress -> {
                val addressPoint = event.addressPoint.toPoint()
                map.animMove(
                    map.cameraPosition.copy(
                        target = addressPoint,
                        zoom = 16.5f,
                    )
                )
            }

            MapFlowViewModel.MapFlowEvents.MoveToGeoOrMoscow -> {
                val moscowCameraPosition = map.cameraPosition.copy(moscowPoint, 10f, 0f, 0f)

                val location = fusedLocationClient.getLocationOrNull(context)

                val cameraPosition = location?.let {
                    CameraPosition(
                        Point(location.latitude, location.longitude),
                        16.5f,
                        0f,
                        0f
                    )
                } ?: moscowCameraPosition

                map.animMove(cameraPosition)

                viewModel.searchAddress(cameraPosition.target.toMapPoint())
            }

            MapFlowViewModel.MapFlowEvents.CheckGeo -> {
                activity?.handleLocationAvailability(
                    onPermissionHave = {
                        viewModel.moveToUserGeo()
                    },
                    onPermissionNotRational = {
                        viewModel.showSettingDialog()
                    },
                    onPermissionNotHave = {
                        locationLauncher.launch(locationPermissions)
                    },
                    onGpsDisabled = {
                        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also { intent ->
                            context.startActivity(intent)
                        }
                    }
                )
            }

            MapFlowViewModel.MapFlowEvents.MoveCameraMinus -> {
                map.minusZoom()
            }

            MapFlowViewModel.MapFlowEvents.MoveCameraPlus -> {
                map.plusZoom()
            }

            MapFlowViewModel.MapFlowEvents.ShowAddressBottomSheet -> {
                mainScope.launch {
                    anchoredDraggableState.animateTo(
                        PartiallyExpanded,
                        tween(200, 250, LinearEasing)
                    )
                }
            }

            MapFlowViewModel.MapFlowEvents.HideAddressBottomSheet -> {
                mainScope.launch {
                    anchoredDraggableState.animateTo(SheetValue.Hidden)
                }
            }

            MapFlowViewModel.MapFlowEvents.GoBack -> {
                navigator.goBack()
            }

            MapFlowViewModel.MapFlowEvents.HideKeyboard -> {
                keyboardController?.hide()
            }

            MapFlowViewModel.MapFlowEvents.GoToLocationSettings -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                    intent.data = Uri.fromParts("package", context.packageName, null)
                    context.startActivity(intent)
                }
            }

            is MapFlowViewModel.MapFlowEvents.GoToAddAddress -> {
                navigator.navigateToAddAddress(
                    mapAddress = event.mapAddress,
                    navOptions = androidx.navigation.navOptions {
                        launchSingleTop = true
                        restoreState = true
                        slideAnim()
                    }
                )
            }

            is MapFlowViewModel.MapFlowEvents.BackToAddAddress -> {
                addAddressViewModel?.changeMapAddress(event.mapAddress)
                navigator.goBack()
            }
        }
    }
