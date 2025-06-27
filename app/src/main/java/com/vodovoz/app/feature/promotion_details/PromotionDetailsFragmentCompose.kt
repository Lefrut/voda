package com.vodovoz.app.feature.promotion_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.util.extensions.openUrl
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
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)
                    val context = LocalContext.current


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
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                    LifecycleEffect {
                        viewModel.listenFavorites()
                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
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