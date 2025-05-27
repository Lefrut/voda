package com.vodovoz.app.feature.bottom.services.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToServiceOrder
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceDetailEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServiceDetailFragment : Fragment() {

    private val viewModel: ServiceDetailViewModel by viewModels()

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

                    ServiceDetailScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )

                    LifecycleEffect {
                        viewModel.listenFavorites()
                    }

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenLoadings()
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                ServiceDetailEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ServiceDetailEvent.GoToAnalogs -> {
                                    findNavController().navigateToProductAnalogs(event.productId)
                                }

                                is ServiceDetailEvent.GoToProductDetails -> {
                                    findNavController().navigateToProductDetails(event.productId)
                                }

                                is ServiceDetailEvent.GoToServiceOrder -> {
                                    findNavController().navigateToServiceOrder(event.serviceType)
                                }
                            }
                        }
                    }

                }
            }
        }
    }

}

