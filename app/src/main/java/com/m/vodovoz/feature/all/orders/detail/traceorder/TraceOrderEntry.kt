package com.m.vodovoz.feature.all.orders.detail.traceorder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue.Hidden
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import com.google.android.gms.location.LocationServices
import com.m.vodovoz.core.android.handleLocationAvailability
import com.m.vodovoz.core.android.locationPermissionGranted
import com.m.vodovoz.core.android.locationPermissions
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.toPoint
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.yandex_map.VodovozUserLocationListener
import com.m.vodovoz.ui.yandex_map.YandexMapUi
import com.m.vodovoz.ui.yandex_map.calculateBounds
import com.m.vodovoz.ui.yandex_map.minusZoom
import com.m.vodovoz.ui.yandex_map.plusZoom
import com.m.vodovoz.util.extensions.dialPhoneNumber
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.logo.Alignment
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraceOrderEntry() = NavigationEntry<TraceOrderViewModel> {
    val context = LocalContext.current
    val activity = context as? Activity
    val viewState by viewModel.collectAsState()

    val fusedLocationClient = remember(activity) {
        LocationServices.getFusedLocationProviderClient(activity ?: context)
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

    val userLocationLayer = remember(mapKit, mapView) {
        mapKit.createUserLocationLayer(mapView.mapWindow)
    }
    val userLocationListener = remember(context) {
        VodovozUserLocationListener(context)
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.moveToUserGeo()
        }
    }

    val anchoredDraggableState = remember {
        AnchoredDraggableState(initialValue = PartiallyExpanded)
    }

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

    TraceOrderScreen(
        yandexMap = yandexMap,
        viewModel = viewModel,
        viewState = viewState,
        anchoredDraggableState = anchoredDraggableState
    )

    LaunchedEffect(Unit) {
        if (context.locationPermissionGranted) return@LaunchedEffect
        locationPermissionLauncher.launch(locationPermissions)
    }

    LifecycleEffect {
        withContext(Dispatchers.Default) {
            viewModel.orderDetailsCallbackFlow().collect()
        }
    }

    LifecycleEffect(anchoredDraggableState) {
        viewModel.events.collect { event ->
            when (event) {
                TraceOrderViewModel.TraceOrderEvents.GoBack -> {
                    navController.popBackStack()
                }

                TraceOrderViewModel.TraceOrderEvents.MoveCameraMinus -> {
                    map.minusZoom()
                }

                TraceOrderViewModel.TraceOrderEvents.MoveCameraPlus -> {
                    map.plusZoom()
                }

                TraceOrderViewModel.TraceOrderEvents.CheckUserGeo -> {
                    activity?.handleLocationAvailability(
                        onPermissionHave = {
                            viewModel.moveToUserGeo()
                        },
                        onPermissionNotRational = {
                            viewModel.showSettingDialog()
                        },
                        onPermissionNotHave = {
                            locationPermissionLauncher.launch(locationPermissions)
                        },
                        onGpsDisabled = {
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also { intent ->
                                context.startActivity(intent)
                            }
                        }
                    )
                }

                is TraceOrderViewModel.TraceOrderEvents.MoveToDeliveryGeo -> {
                    val bounds = calculateBounds(
                        event.finishPoint?.toPoint() ?: return@collect,
                        event.driverPoint?.toPoint() ?: return@collect
                    )

                    val (tilt, azimuth) = map.cameraPosition.let { it.tilt to it.azimuth }
                    val cameraPositionWithBounds = map.cameraPosition(Geometry.fromBoundingBox(bounds))

                    val cameraPosition = CameraPosition(
                        cameraPositionWithBounds.target,
                        (cameraPositionWithBounds.zoom - 1f).coerceAtMost(17f),
                        azimuth,
                        tilt
                    )

                    map.move(
                        cameraPosition,
                        Animation(Animation.Type.LINEAR, 0.25f),
                        null
                    )
                }

                TraceOrderViewModel.TraceOrderEvents.GoToLocationSettings -> {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                }

                TraceOrderViewModel.TraceOrderEvents.MoveToUserGeo -> {
                    if (
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.checkGeo()
                        return@collect
                    }

                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location == null) return@addOnSuccessListener

                        map.move(
                            CameraPosition(
                                Point(location.latitude, location.longitude),
                                14f,
                                0f,
                                0f
                            ),
                            Animation(Animation.Type.LINEAR, 0.25f),
                            null
                        )
                    }
                }

                TraceOrderViewModel.TraceOrderEvents.HideBottomSheet -> {
                    launch {
                        anchoredDraggableState.animateTo(
                            Hidden,
                            tween(150, 0, LinearEasing)
                        )
                    }
                }

                TraceOrderViewModel.TraceOrderEvents.ShowBottomSheet -> {
                    launch {
                        if (anchoredDraggableState.isAnimationRunning) return@launch

                        anchoredDraggableState.animateTo(
                            PartiallyExpanded,
                            tween(250, 600, LinearEasing)
                        )
                    }
                }

                is TraceOrderViewModel.TraceOrderEvents.Phone -> {
                    context.dialPhoneNumber(event.phone)
                }

                is TraceOrderViewModel.TraceOrderEvents.GoToJivoChat -> {
                    navController.navigateToWebView(event.link)
                }
            }
        }
    }
}
