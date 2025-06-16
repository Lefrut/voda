package com.vodovoz.app.feature.map

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SheetValue.PartiallyExpanded
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.android.getLocationOrNull
import com.vodovoz.app.core.android.handleLocationAvailability
import com.vodovoz.app.core.android.locationPermissions
import com.vodovoz.app.core.android.locationPermissionGranted
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.toMapPoint
import com.vodovoz.app.design_system.model.toPoint
import com.vodovoz.app.ui.yandex_map.VodovozUserLocationListener
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.ui.yandex_map.copy
import com.vodovoz.app.ui.yandex_map.minusZoom
import com.vodovoz.app.ui.yandex_map.plusZoom
import com.vodovoz.app.util.extensions.enableFullScreen
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
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
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
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
        mapKit.createUserLocationLayer(
            mapWindow
        )
    }

    private val moscowPoint = Point(55.75, 37.62)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
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
                val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                val viewState by rememberUpdatedState(newValue = pagingState.data)
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                    if (granted) {
                        viewModel.moveToAvailableGeo()
                    }
                }

                val anchoredDraggableState =
                    rememberSaveable(saver = AnchoredDraggableState.Saver()) {
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

                LifecycleEffect(anchoredDraggableState) {
                    observeEvents(locationPermissionLauncher, anchoredDraggableState)
                }

                LaunchedEffect(Unit) {
                    delay(1000L)
                    viewModel.moveToAvailableGeo()
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true
        userLocationLayer.setObjectListener(userLocationListener)


        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.changeTabVisibility(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    override fun onStart() {
        super.onStart()
        mapKit.onStart()

    }

    override fun onPause() {
        super.onPause()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        tabManager.changeTabWindowInsets(false)
    }

    override fun onStop() {
        mapKit.onStop()
        super.onStop()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        lifecycleScope.launch {
            delay(500L)
            WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
            tabManager.changeTabWindowInsets(true)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private suspend fun observeEvents(
        locationLauncher: ActivityResultLauncher<Array<String>>,
        anchoredDraggableState: AnchoredDraggableState<SheetValue>,
    ): Unit =
        viewModel.observeEvent()
            .collect { event ->
                when (event) {
                    is MapFlowViewModel.MapFlowEvents.MoveToAddress -> {
                        val addressPoint = event.addressPoint.toPoint()
                        map.move(
                            map.cameraPosition.copy(
                                target = addressPoint,
                                zoom = 16f,
                            ),
                            Animation(Animation.Type.LINEAR, 0.25f),
                            null
                        )
                    }

                    MapFlowViewModel.MapFlowEvents.MoveToGeoOrMoscow -> {
                        val moscowCameraPosition = map.cameraPosition.copy(moscowPoint, 10f, 0f, 0f)

                        if (requireContext().locationPermissionGranted
                            && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                        ) {
                            val location = fusedLocationClient.getLocationOrNull(requireContext())

                            val cameraPosition = location?.let {
                                map.cameraPosition.copy(
                                    Point(
                                        location.latitude,
                                        location.longitude
                                    ),
                                    16f,
                                    0f,
                                    0f
                                )
                            } ?: moscowCameraPosition

                            map.move(
                                cameraPosition,
                                Animation(Animation.Type.LINEAR, 0.25f),
                                null
                            )
                            viewModel.changeMarkerPoint(cameraPosition.target.toMapPoint())
                        } else {
                            map.move(
                                moscowCameraPosition,
                                Animation(Animation.Type.LINEAR, 0.25f),
                                null
                            )
                            viewModel.changeMarkerPoint(moscowCameraPosition.target.toMapPoint())
                        }
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
                        anchoredDraggableState.animateTo(PartiallyExpanded)
                    }

                    MapFlowViewModel.MapFlowEvents.HideAddressBottomSheet -> {
                        anchoredDraggableState.animateTo(SheetValue.Hidden)
                    }

                    MapFlowViewModel.MapFlowEvents.GoBack -> {
                        findNavController().popBackStack()
                    }

                    else -> {

                    }
                }
            }

}
