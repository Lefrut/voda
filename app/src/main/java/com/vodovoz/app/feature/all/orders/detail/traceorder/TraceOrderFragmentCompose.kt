package com.vodovoz.app.feature.all.orders.detail.traceorder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.vodovoz.app.core.android.handleLocationAvailability
import com.vodovoz.app.core.android.locationPermissions
import com.vodovoz.app.core.android.locationPermissionsGranted
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.toPoint
import com.vodovoz.app.ui.yandex_map.VodovozUserLocationListener
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.ui.yandex_map.calculateBounds
import com.vodovoz.app.ui.yandex_map.minusZoom
import com.vodovoz.app.ui.yandex_map.plusZoom
import com.vodovoz.app.util.extensions.dialPhoneNumber
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.ScreenPoint
import com.yandex.mapkit.ScreenRect
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class TraceOrderFragment : Fragment() {

    val viewModel by viewModels<TraceOrderViewModel>()

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    private val mapKit: MapKit = MapKitFactory.getInstance()

    private val yandexMap by lazy {
        YandexMapUi(
            mapView = MapView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        )
    }

    private val mapView: MapView get() = yandexMap.mapView
    private val mapWindow: MapWindow get() = mapView.mapWindow
    private val map: Map get() = mapView.mapWindow.map

    private val userLocationLayer by lazy {
        mapKit.createUserLocationLayer(mapView.mapWindow)
    }

    private val userLocationListener by lazy {
        VodovozUserLocationListener(requireContext())
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())


    }

    override fun onStart() {
        super.onStart()
        mapKit.onStart()
    }

    override fun onStop() {
        mapKit.onStop()
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(userLocationListener)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {

                    val context = LocalContext.current

                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

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

                    TraceOrderScreen(
                        yandexMap = yandexMap,
                        viewModel = viewModel,
                        viewState = viewState,
                        anchoredDraggableState = anchoredDraggableState
                    )


                    LaunchedEffect(Unit) {
                        if (context.locationPermissionsGranted) return@LaunchedEffect

                        locationPermissionLauncher.launch(locationPermissions)
                    }

                    LifecycleEffect {
                        withContext(Dispatchers.Default) {
                            viewModel.orderDetailsCallbackFlow().collect()
                        }
                    }

                    LifecycleEffect(anchoredDraggableState) {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                TraceOrderViewModel.TraceOrderEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveCameraMinus -> {
                                    map.minusZoom()
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveCameraPlus -> {
                                    map.plusZoom()
                                }

                                TraceOrderViewModel.TraceOrderEvents.CheckUserGeo -> {
                                    requireActivity().handleLocationAvailability(
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

                                    //todo - need tests focus rect

                                    val bounds = calculateBounds(
                                        event.finishPoint?.toPoint() ?: return@collect,
                                        event.driverPoint?.toPoint() ?: return@collect
                                    )


                                    val (tilt, azimuth) = map.cameraPosition.let {
                                        it.tilt to it.azimuth
                                    }


                                    val cameraPositionWithBounds = map.cameraPosition(bounds)

                                    val cameraPosition = CameraPosition(
                                        cameraPositionWithBounds.target,
                                        cameraPositionWithBounds.zoom - 1f,
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
                                        intent.data =
                                            Uri.fromParts("package", context.packageName, null)
                                        context.startActivity(intent)
                                    }
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveToUserGeo -> {
                                    if (ActivityCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.ACCESS_FINE_LOCATION
                                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
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
                                    findNavController().navigateToWebView(event.link)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}