package com.vodovoz.app.feature.addresses.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToMap
import com.vodovoz.app.core.navigation.slideAnim
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.addresses.add.composables.AddAddressScreen
import com.vodovoz.app.feature.addresses.add.model.AddAddressEvent
import com.vodovoz.app.feature.map.model.MapAddressUi
import com.yandex.mapkit.MapKit
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AddAddressFragment : Fragment() {

    private val viewModel: AddAddressViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    private var initialTabVisibility: Boolean = true

    private val mapKit: MapKit by lazy { MapKitFactory.getInstance() }

    override fun onStart() {
        super.onStart()
        mapKit.onStart()
        initialTabVisibility = tabManager.observeTabVisibility().value
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        mapKit.onStop()
        super.onStop()
        tabManager.changeTabVisibility(initialTabVisibility)
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
                    val viewState by viewModel.state.collectAsStateWithLifecycle()
                    val mainScope = rememberCoroutineScope()
                    val snackbarHostState = remember { SnackbarHostState() }

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
            }
        }
    }

    private suspend fun observeEvents(
        mainScope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
    ) {
        viewModel.events.onSubscription {
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<MapAddressUi>("mapAddress")
                ?.let { mapAddress -> viewModel.changeMapAddress(mapAddress) }
        }.collect { event ->
            when (event) {
                AddAddressEvent.GoBackToMap -> {
                    findNavController().popBackStack(
                        destinationId = R.id.mapFragment,
                        inclusive = false,
                        saveState = true
                    )
                }

                is AddAddressEvent.GoToMap -> {
                    findNavController().navigateToMap(
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
                    findNavController().popBackStack(
                        destinationId = R.id.addressesFragment,
                        inclusive = false,
                        saveState = false
                    )
                }
            }
        }
    }
}
