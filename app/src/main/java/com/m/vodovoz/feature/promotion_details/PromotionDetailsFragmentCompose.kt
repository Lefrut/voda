package com.m.vodovoz.feature.promotion_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromotionDetailsFragment : Fragment() {

    internal val viewModel: PromotionDetailsViewModel by viewModels()

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()


                    when (viewState.uiState) {
                        PromotionDetailsViewModel.UiState.Error -> {
                            NetworkErrorPlaceholder(
                                onTryAgainClick = { viewModel.fetchPromotionDetails() }
                            )
                        }

                        else -> {
                            PromotionDetailsScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                            )
                        }
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                PromotionDetailsViewModel.PromotionDetailEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is PromotionDetailsViewModel.PromotionDetailEvent.OpenUrl -> {
                                    requireContext().openUrl(event.url)
                                }

                                is PromotionDetailsViewModel.PromotionDetailEvent.GoToProductAnalogs -> {
                                    findNavController().navigateToProductAnalogs(event.productId)
                                }

                                is PromotionDetailsViewModel.PromotionDetailEvent.GoToProductDetails -> {
                                    findNavController().navigateToProductDetails(event.productId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}