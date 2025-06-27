package com.vodovoz.app.feature.addresses

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToAddAddress
import com.vodovoz.app.core.navigation.navigateToMap
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddressesFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    internal val viewModel: AddressesFlowViewModel by viewModels()

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

                VodovozTheme {
                    AddressesScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )
                }

                LaunchedEffect(Unit) {
                    viewModel.fetchAddresses()
                }

                LifecycleEffect {
                    observeEvents()
                }

            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.observeEvent().collect { event ->
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
                    navController.previousBackStackEntry?.savedStateHandle?.set("addressId", event.addressId)
                    navController.popBackStack()
                }
            }
        }
    }

}

