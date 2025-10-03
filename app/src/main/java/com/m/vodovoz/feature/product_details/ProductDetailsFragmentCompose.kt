package com.m.vodovoz.feature.product_details


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.mainFragment
import com.m.vodovoz.core.navigation.navigateToAboutProduct
import com.m.vodovoz.core.navigation.navigateToBrandProductList
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToDetailMedia
import com.m.vodovoz.core.navigation.navigateToPreOrder
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductComments
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSearchProductList
import com.m.vodovoz.core.navigation.navigateToWriteComment
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.ui.snackbar.snackBarHostState
import com.m.vodovoz.util.extensions.copyText
import com.m.vodovoz.util.extensions.shareText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProductDetailsFragment : Fragment() {


    internal val viewModel: ProductDetailsFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

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
                    val uiState = viewState.uiState

                    val mediaPagerState = when (uiState) {
                        ProductDetailsFlowViewModel.ProductDetailsUiState.Success -> {
                            rememberPagerState { viewState.productDetails.mediaList.size }
                        }

                        else -> {
                            rememberPagerState { 0 }
                        }
                    }


                    when (uiState) {
                        is ProductDetailsFlowViewModel.ProductDetailsUiState.ForAdults -> {
                            ForAdultsPlaceholder(
                                forAdults = uiState.forAdultsUi,
                                onBackClick = {
                                    viewModel.navigateBack()
                                },
                                onApplyClick = {
                                    viewModel.setCanViewAdultProducts()
                                }
                            )
                        }

                        else -> {
                            ProductDetailsScreen(
                                viewState = viewState,
                                viewModel = viewModel,
                                mediaPagerState = mediaPagerState
                            )
                        }
                    }


                    LifecycleEffect {
                        observeEvents(mediaPagerState)
                    }

                    LifecycleEffect {
                        viewModel.listenProductDetailsUpdates(this)
                    }

                    LifecycleEffect {
                        viewModel.listenCartUpdates()
                    }
                }
            }
        }
    }

    private suspend fun observeEvents(mediaPagerState: PagerState): Unit =
        viewModel.events.onSubscription {
            val mediaIndex = findNavController().currentBackStackEntry
                ?.savedStateHandle
                ?.get<Int>("mediaIndex")

            mediaIndex?.let { page ->
                viewModel.setMediaPage(page)
            }
        }.collect { event ->
            when (event) {
                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToPreOrder -> {
                    findNavController().navigateToPreOrder(event.id)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProfile -> {
                    tabManager.setAuthRedirect(findNavController().graph.id)
                    tabManager.selectTab(R.id.graph_profile)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToCart -> {
                    tabManager.setAuthRedirect(findNavController().graph.id)
                    tabManager.selectTab(R.id.graph_cart)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToAboutProduct -> {
                    findNavController().navigateToAboutProduct(
                        productId = event.productId,
                        prices = event.prices,
                        analogButton = event.analogButton,
                        isAvailable = event.isAvailable
                    )
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductComments -> {
                    findNavController().navigateToProductComments(
                        productId = event.productId,
                        productName = event.productName,
                        productImage = event.productImage
                    )
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductAnalogs -> {
                    findNavController().navigateToProductAnalogs(event.productId)
                }

                ProductDetailsFlowViewModel.ProductDetailsEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToSearch -> {
                    findNavController().navigateToSearch(event.query)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToProductDetails -> {
                    findNavController().navigateToProductDetails(event.productId)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToCategoryProductList -> {
                    findNavController().navigateToCategoryProductList(event.categoryId)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.Share -> {
                    kotlin.runCatching { requireContext().shareText(event.text) }
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToSearchProductList -> {
                    findNavController().navigateToSearchProductList(event.query)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.Copy -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        requireContext().copyText(event.text)
                        mainFragment?.snackBarHostState?.showSnackbar(
                            VodovozSnackBarVisuals.create(getString(R.string.article_copied))
                        )
                    }
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToBrandProducts -> {
                    findNavController().navigateToBrandProductList(event.brandId)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToWriteComment -> {
                    findNavController().navigateToWriteComment(
                        event.id,
                        event.name,
                        event.detailPicture,
                        event.rating
                    )
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.GoToDetailMedia -> {
                    findNavController().navigateToDetailMedia(event.media, event.mediaList)
                }

                is ProductDetailsFlowViewModel.ProductDetailsEvents.ScrollToMediaPage -> {
                    mediaPagerState.scrollToPage(event.page)
                }
            }
        }
}
