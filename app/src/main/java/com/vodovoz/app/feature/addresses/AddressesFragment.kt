package com.vodovoz.app.feature.addresses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToMap
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.util.extensions.snack
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddressesFragment : Fragment() {

    companion object {
        const val SELECTED_ADDRESS = "SELECTED_ADDRESS"
    }

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
                val viewState by rememberUpdatedState(newValue = pagingState.data)

                VodovozTheme {
                    AddressesScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )
                }

                LifecycleEffect {
                    observeEvents()
                }

                LifecycleEffect {
                    observeRefresh()
                }
            }
        }
    }

    private suspend fun observeRefresh() {
        tabManager
            .observeAddressesRefresh()
            .collect { refreshAddress ->
                if (refreshAddress) {
                    viewModel.refresh()
                    tabManager.setAddressesRefreshState(false)
                }
            }
    }

    private suspend fun observeEvents() {
        viewModel.observeEvent().collect { event ->
            when (event) {
                is AddressesFlowViewModel.AddressesEvents.DeleteEvent -> {
                    requireActivity().snack(event.message)
                }

                is AddressesFlowViewModel.AddressesEvents.OnAddressClick -> {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        SELECTED_ADDRESS, event.address
                    )
                    findNavController().popBackStack(R.id.orderingFragment, false)
                }

                is AddressesFlowViewModel.AddressesEvents.UpdateAddress -> {

                }

                AddressesFlowViewModel.AddressesEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                AddressesFlowViewModel.AddressesEvents.GoToMap -> {
                    findNavController().navigateToMap()
                }
            }
        }
    }

}

