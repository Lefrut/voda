package com.vodovoz.app.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToWriteComment
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductCommentsFragment : Fragment() {

    private val viewModel: ProductCommentsFlowViewModel by viewModels()

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
                val viewState by viewModel.observeUiState().collectAsStateWithLifecycle()
                val lazyListState = rememberLazyListState()

                VodovozTheme {
                    ProductCommentsScreen(
                        viewModel = viewModel,
                        viewState = viewState.data,
                        lazyListState = lazyListState
                    )


                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                ProductCommentsFlowViewModel.ProductCommentsEvents.ScrollToTop -> {
                                    lazyListState.animateScrollToItem(0)
                                }


                                ProductCommentsFlowViewModel.ProductCommentsEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductCommentsFlowViewModel.ProductCommentsEvents.GoToWriteComment -> {
                                    findNavController().navigateToWriteComment(
                                        productId = event.productId,
                                        productImage = event.productName,
                                        productName = event.productImage,
                                        rating = 0
                                    )
                                }
                            }

                        }
                    }

                    BackHandler { viewModel.navigateBack() }
                }

            }
        }
    }
}
