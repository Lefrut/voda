package com.vodovoz.app.feature.bottom.services

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.core.navigation.navigateToServiceDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.bottom.services.adapter.ServicesClickListener
import com.vodovoz.app.feature.bottom.services.newservs.model.ServiceNew
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutServicesFragment : Fragment() {

    private val viewModel: AboutServicesFlowViewModel by viewModels()

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

                    when (viewState.uiState) {
                        AboutServicesFlowViewModel.AboutServicesUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchAboutServicesDetails() }
                        }

                        AboutServicesFlowViewModel.AboutServicesUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        AboutServicesFlowViewModel.AboutServicesUiState.Success -> {
                            AboutServicesScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                AboutServicesFlowViewModel.AboutServicesEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is AboutServicesFlowViewModel.AboutServicesEvents.GoToServiceDetails -> {
                                    findNavController().navigateToServiceDetails(event.serviceId)
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private fun getServicesClickListener(): ServicesClickListener {
        return object : ServicesClickListener {
            override fun onItemClick(item: ServiceNew) {
                val id = item.id ?: return
                findNavController().navigate(
                    AboutServicesFragmentDirections.actionToServiceDetailNewFragment(
                        id
                    )
                )
            }
        }
    }


}
