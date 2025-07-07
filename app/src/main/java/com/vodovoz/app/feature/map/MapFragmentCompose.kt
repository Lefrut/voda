package com.vodovoz.app.feature.map

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.gms.location.LocationServices
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.android.getLocationOrNull
import com.vodovoz.app.core.android.handleLocationAvailability
import com.vodovoz.app.core.android.locationPermissions
import com.vodovoz.app.core.navigation.navigateToAddAddress
import com.vodovoz.app.core.navigation.slideAnim
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.design_system.model.toPoint
import com.vodovoz.app.ui.yandex_map.VodovozUserLocationListener
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.ui.yandex_map.animMove
import com.vodovoz.app.ui.yandex_map.copy
import com.vodovoz.app.ui.yandex_map.minusZoom
import com.vodovoz.app.ui.yandex_map.plusZoom
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: MapFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }
    private val mapKit: MapKit by lazy { MapKitFactory.getInstance() }

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
    private val mapWindow: MapWindow get() = yandexMap.mapView.mapWindow
    private val map: Map get() = mapView.mapWindow.map

    private val userLocationListener by lazy { VodovozUserLocationListener(requireContext()) }
    private val userLocationLayer: UserLocationLayer by lazy {
        mapKit.createUserLocationLayer(mapWindow)
    }

    private val moscowPoint = Point(55.75, 37.62)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                val viewState by rememberUpdatedState(pagingState.data)
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                    if (granted) {
                        viewModel.moveToAvailableGeo()
                    }
                }

                val anchoredDraggableState = remember {
                    AnchoredDraggableState(initialValue = PartiallyExpanded)
                }


                VodovozTheme {
                    MapScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        yandexMap = yandexMap,
                        anchoredDraggableState = anchoredDraggableState
                    )
                }

                val coroutineScope = rememberCoroutineScope()
                val keyboardController = LocalSoftwareKeyboardController.current

                LifecycleEffect(anchoredDraggableState) {
                    observeEvents(
                        locationLauncher = locationPermissionLauncher,
                        anchoredDraggableState = anchoredDraggableState,
                        mainScope = coroutineScope,
                        keyboardController = keyboardController
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(userLocationListener)

    }

    override fun onStart() {
        super.onStart()
        mapKit.onStart()
    }


    override fun onStop() {
        mapKit.onStop()
        super.onStop()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private suspend fun observeEvents(
        locationLauncher: ActivityResultLauncher<Array<String>>,
        anchoredDraggableState: AnchoredDraggableState<SheetValue>,
        mainScope: CoroutineScope,
        keyboardController: SoftwareKeyboardController?,
    ): Unit =
        viewModel.observeEvent().onSubscription {
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

                    val location = fusedLocationClient.getLocationOrNull(requireContext())

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
                    requireActivity().handleLocationAvailability(
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
                                requireContext().startActivity(intent)
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
                    findNavController().popBackStack()
                }

                MapFlowViewModel.MapFlowEvents.HideKeyboard -> {
                    keyboardController?.hide()
                }

                MapFlowViewModel.MapFlowEvents.GoToLocationSettings -> {
                    val context = requireContext()

                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                        intent.data =
                            Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }
                }

                is MapFlowViewModel.MapFlowEvents.GoToAddAddress -> {
                    findNavController().navigateToAddAddress(
                        mapAddress = event.mapAddress,
                        navOptions = navOptions {
                            launchSingleTop = true
                            restoreState = true
                            slideAnim()
                        }
                    )
                }

                is MapFlowViewModel.MapFlowEvents.BackToAddAddress -> {
                    val navController = findNavController()
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "mapAddress", event.mapAddress
                    )
                    navController.popBackStack()
                }


            }
        }

}
