package com.m.vodovoz.feature.product_analogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.bottom_sheet.SortOptionsBottomSheet
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_analogs.model.ProductAnalogsEvent
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductAnalogsFragment : Fragment() {

    private val viewModel: ProductAnalogsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchProductAnalogs()
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
                    ProductAnalogsScreen(viewModel = viewModel, viewState = viewState)

                    if (viewState.showSortOptionsBottomSheet) {
                        SortOptionsBottomSheet(
                            onDismissRequest = { viewModel.closeSortOptionsBottomSheet() },
                            currentSort = viewState.currentSort,
                            sorting = viewState.productsSection.sorting,
                            onSortSelect = { sort ->
                                viewModel.selectSort(sort)
                            }
                        )
                    }
                }

                LifecycleEffect {
                    viewModel.events.collect { event ->
                        when (event) {
                            ProductAnalogsEvent.GoBack -> {
                                findNavController().popBackStack()
                            }

                            is ProductAnalogsEvent.GoToProductAnalogs -> {
                                findNavController().navigateToProductDetails(event.productId)
                            }

                            is ProductAnalogsEvent.GoToProductDetails -> {
                                findNavController().navigateToProductDetails(event.productId)
                            }
                        }
                    }
                }
            }
        }
    }

}