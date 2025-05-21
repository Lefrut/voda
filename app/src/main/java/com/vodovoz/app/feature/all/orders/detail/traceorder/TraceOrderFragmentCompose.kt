package com.vodovoz.app.feature.all.orders.detail.traceorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.LocationServices
import com.vodovoz.app.R
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.RotationType
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TraceOrderFragment : Fragment() {

    val viewModel by viewModels<TraceOrderViewModel>()


    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val locationPermissionsGranted
        get() = locationPermissions.any { perm ->
            ContextCompat.checkSelfPermission(
                requireContext(),
                perm
            ) == PackageManager.PERMISSION_GRANTED
        }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(requireActivity())
    }
    private val locationManager by lazy {
        requireContext().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    private val mapKit: MapKit = MapKitFactory.getInstance()


    private val mapView by lazy {
        MapView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    private val map: Map get() = mapView.mapWindow.map

    private val userLocationLayer by lazy {
        mapKit.createUserLocationLayer(mapView.mapWindow)
    }

    private val userLocationListener = object : UserLocationObjectListener {
        override fun onObjectAdded(p0: UserLocationView) {
            p0.arrow.setIcon(
                ImageProvider.fromResource(requireContext(), R.drawable.png_gps_1),
                IconStyle().setScale(0.12f)
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(1f)
            )
            p0.pin.setIcon(
                ImageProvider.fromResource(requireContext(), R.drawable.png_gps_1),
                IconStyle().setScale(0.12f)
                    .setRotationType(RotationType.ROTATE)
                    .setZIndex(0f)
            )

            p0.accuracyCircle.fillColor = Color.Transparent.hashCode()
        }

        override fun onObjectRemoved(p0: UserLocationView) = Unit

        override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) = Unit
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {

                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)
                    val locationPermissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { result ->
                        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                        if (granted) {
                            viewModel.moveToGeo()
                        }
                    }

                    TraceOrderScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        mapView = { mapView }
                    )

                    LaunchedEffect(Unit) {

                    }


                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                TraceOrderViewModel.TraceOrderEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveCameraMinus -> {
                                    val cameraPosition = map.cameraPosition
                                    val newZoom = cameraPosition.zoom - 1f

                                    mapView.map.move(
                                        CameraPosition(
                                            cameraPosition.target,
                                            newZoom,
                                            cameraPosition.azimuth,
                                            cameraPosition.tilt
                                        ),
                                        Animation(Animation.Type.LINEAR, 0.25f),
                                        null
                                    )
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveCameraPlus -> {
                                    val cameraPosition = map.cameraPosition
                                    val newZoom = cameraPosition.zoom + 1f
                                    mapView.map.move(
                                        CameraPosition(
                                            cameraPosition.target,
                                            newZoom,
                                            cameraPosition.azimuth,
                                            cameraPosition.tilt
                                        ),
                                        Animation(Animation.Type.LINEAR, 0.25f),
                                        null
                                    )
                                }

                                TraceOrderViewModel.TraceOrderEvents.CheckGeo -> {
                                    val showSettingDialog =
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            !locationPermissions.any { perm ->
                                                requireActivity().shouldShowRequestPermissionRationale(
                                                    perm
                                                )
                                            } && !locationPermissionsGranted
                                        } else false


                                    when {
                                        locationPermissionsGranted -> {
                                            viewModel.moveToGeo()
                                        }


                                        locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true -> {
                                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).also { intent ->
                                                context.startActivity(intent)
                                            }
                                        }


                                        showSettingDialog -> {
                                            viewModel.showSettingDialog()
                                        }

                                        else -> {
                                            locationPermissionLauncher.launch(locationPermissions)
                                        }
                                    }
                                }

                                TraceOrderViewModel.TraceOrderEvents.MoveToGeo -> {
                                    val context = requireContext()
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
                                        location ?: return@addOnSuccessListener

                                        mapView.mapWindow.map.move(
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

                                TraceOrderViewModel.TraceOrderEvents.GoToLocationSettings -> {
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also { intent ->
                                        intent.data =
                                            Uri.fromParts("package", context.packageName, null)
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}