package com.m.vodovoz.feature.addresses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToAddAddress
import com.m.vodovoz.core.navigation.navigateToMap
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddressesFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    internal val viewModel: AddressesFlowViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        val screenType = viewModel.state.value.screenType
        when(screenType){
            AddressScreenTypeUi.Add -> {}
            AddressScreenTypeUi.Choose -> {
                tabManager.changeTabVisibility(false)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                val viewState by viewModel.collectAsState()
                

                VodovozTheme {
                    AddressesScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.refresh()
                }

                LifecycleEffect {
                    observeEvents()
                }

            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.collect { event ->
            when (event) {
                AddressesFlowViewModel.AddressesEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                AddressesFlowViewModel.AddressesEvents.GoToMap -> {
                    findNavController().navigateToMap(null)
                }

                is AddressesFlowViewModel.AddressesEvents.GoToEditAddress -> {
                    findNavController().navigateToAddAddress(
                        addressId = event.addressId,
                        addressName = event.addressName
                    )
                }

                is AddressesFlowViewModel.AddressesEvents.GoBackToOrdering -> {
                    val navController = findNavController()
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", event.address)
                    navController.popBackStack()
                }
            }
        }
    }

}

