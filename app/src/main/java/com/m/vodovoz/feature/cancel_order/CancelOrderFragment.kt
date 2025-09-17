package com.m.vodovoz.feature.cancel_order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cancel_order.model.CancelOrderEvent
import com.m.vodovoz.feature.cancel_order.model.CancelOrderUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CancelOrderFragment : Fragment() {

    private val viewModel by viewModels<CancelOrderViewModel>()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()

                    when (viewState.uiState) {
                        CancelOrderUiState.Body -> {
                            CancelOrderScreen(
                                viewModel = viewModel,
                                viewState = viewState
                            )
                        }

                        CancelOrderUiState.Error -> {
                            NetworkErrorPlaceholder {
                                viewModel.fetchCancelOrderDetails()
                            }
                        }

                        CancelOrderUiState.Loading -> {
                            LoadingPlaceholder()
                        }
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                CancelOrderEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}