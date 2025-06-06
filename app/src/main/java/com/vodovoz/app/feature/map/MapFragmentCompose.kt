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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.location.LocationServices
import com.vodovoz.app.core.android.getLocationOrNull
import com.vodovoz.app.core.android.handleLocationAvailability
import com.vodovoz.app.core.android.locationPermissions
import com.vodovoz.app.core.android.locationPermissionsGranted
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.toPoint
import com.vodovoz.app.feature.map.adapter.AddressResult
import com.vodovoz.app.ui.yandex_map.VodovozUserLocationListener
import com.vodovoz.app.ui.yandex_map.YandexMapUi
import com.vodovoz.app.ui.yandex_map.copy
import com.vodovoz.app.ui.yandex_map.minusZoom
import com.vodovoz.app.ui.yandex_map.plusZoom
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SuggestItem
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestSession
import com.yandex.mapkit.search.SuggestType
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.Error
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: MapFlowViewModel by viewModels()

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireContext())
    }

    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    private val mapKit: MapKit by lazy {
        MapKitFactory.getInstance()
    }

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

    private val searchManager: SearchManager by lazy {
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    }
    private val suggestSession: SuggestSession by lazy {
        searchManager.createSuggestSession()
    }

    private val searchOptions = SuggestOptions().setSuggestTypes(
        SuggestType.GEO.value or SuggestType.BIZ.value or SuggestType.TRANSIT.value
    )

    private val suggestSessionListener = object : SuggestSession.SuggestListener {
        override fun onResponse(suggestItems: MutableList<SuggestItem>) {
            val addressList = mutableListOf<AddressResult>()

            suggestItems.filter { item -> item.uri?.contains("geo") == true }.forEach { item ->
                val coordinates =
                    item.uri?.split("&")?.firstOrNull()?.split("=")?.get(1)?.split("%2C")

                addressList.add(
                    AddressResult(
                        item.displayText.toString(),
                        Point(
                            coordinates?.lastOrNull()?.toDoubleOrNull() ?: 0.0,
                            coordinates?.lastOrNull()?.toDoubleOrNull() ?: 0.0
                        )
                    )
                )
            }
        }

        override fun onError(p0: Error) = Unit

    }


    private val userLocationListener by lazy { VodovozUserLocationListener(requireContext()) }
    private val userLocationLayer: UserLocationLayer by lazy {
        mapKit.createUserLocationLayer(mapWindow)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    //
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


                VodovozTheme {
                    MapScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        yandexMap = yandexMap
                    )
                }


                LaunchedEffect(Unit) {
                    viewModel.fetchAddressByGeocode(55.75, 37.62)
                }

                LifecycleEffect {
                    observeEvents(locationPermissionLauncher)
                }

                LaunchedEffect(Unit) {
                    delay(500L)
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
    }

    override fun onStart() {
        super.onStart()
        mapKit.onStart()
    }

    override fun onStop() {
        mapKit.onStop()
        super.onStop()
    }

    private suspend fun observeEvents(locationLauncher: ActivityResultLauncher<Array<String>>): Unit =
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
                        val moscowPoint = Point(55.75, 37.62)

                        if (requireContext().locationPermissionsGranted
                            && locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                        ) {
                            val location = fusedLocationClient.getLocationOrNull(requireContext())
                            val cameraPosition = location?.let {
                                map.cameraPosition.copy(
                                    Point(it.latitude, it.longitude),
                                    16f,
                                    0f,
                                    0f
                                )
                            } ?: map.cameraPosition.copy(moscowPoint, 10f, 0f, 0f)

                            map.move(
                                cameraPosition,
                                Animation(Animation.Type.LINEAR, 0.25f),
                                null
                            )
                        } else {
                            map.move(
                                map.cameraPosition.copy(moscowPoint, 17f, 0f, 0f),
                                Animation(Animation.Type.LINEAR, 0.25f),
                                null
                            )
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


                    else -> {

                    }
                }
            }

}
