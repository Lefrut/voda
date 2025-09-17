package com.m.vodovoz.feature.product_filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RangeSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToProductFilterValues
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.filters.FilterUi
import com.m.vodovoz.design_system.model.filters.FiltersPriceUi
import com.m.vodovoz.util.extensions.calculateActiveRange
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductFiltersFlowFragment : Fragment() {

    private val viewModel: ProductFiltersFlowViewModel by viewModels()

    @Inject
    internal lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun rememberFiltersRangeSliderState(
        uiState: ProductFiltersFlowViewModel.ProductFiltersUiState,
        filtersPrice: FiltersPriceUi,
    ): RangeSliderState {
        return remember(
            uiState,
            filtersPrice.min,
            filtersPrice.max
        ) {
            val range = calculateActiveRange(
                min = filtersPrice.min,
                max = filtersPrice.max,
                currentMin = filtersPrice.currentMin,
                currentMax = filtersPrice.currentMax
            )

            RangeSliderState(
                activeRangeStart = range.start,
                activeRangeEnd = range.endInclusive,
                valueRange = 0f..1f
            )
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<FilterUi>("filter")
            ?.let { newFilter ->
                viewModel.changeFilter(newFilter)
            }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    

                    val filterPrice = viewState.filters.price

                    val sliderState = rememberFiltersRangeSliderState(
                        viewState.uiState, filterPrice,
                    )

                    when (viewState.uiState) {
                        ProductFiltersFlowViewModel.ProductFiltersUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchFiltersByCategory() }
                        }

                        ProductFiltersFlowViewModel.ProductFiltersUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        ProductFiltersFlowViewModel.ProductFiltersUiState.Success -> {
                            ProductFiltersScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                sliderState = sliderState
                            )
                        }
                    }


                    LifecycleEffect(sliderState) {
                        viewModel.events.collect { event ->
                            when (event) {
                                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoToFilterValues -> {
                                    findNavController().navigateToProductFilterValues(
                                        event.categoryId,
                                        event.filter
                                    )
                                }

                                is ProductFiltersFlowViewModel.ProductFiltersEvent.GoToProductList -> {
                                    val navController = findNavController()

                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "filters",
                                        event.filters
                                    ).also {
                                        navController.popBackStack()
                                    }
                                }

                                ProductFiltersFlowViewModel.ProductFiltersEvent.ResetSlider -> {
                                    sliderState.activeRangeStart = 0f
                                    sliderState.activeRangeEnd = 1f
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}