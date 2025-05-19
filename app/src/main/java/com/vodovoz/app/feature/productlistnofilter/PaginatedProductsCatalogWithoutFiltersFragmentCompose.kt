package com.vodovoz.app.feature.productlistnofilter

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.product.rating.RatingProductManager
import com.vodovoz.app.core.navigation.navigateToAnalogs
import com.vodovoz.app.core.navigation.navigateToCategories
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToProductFilters
import com.vodovoz.app.core.navigation.navigateToSearch
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.filters.FiltersUi
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.feature.profile.core.ContentSearchNavigator
import com.vodovoz.app.util.extensions.shareText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class PaginatedProductsCatalogWithoutFiltersFragment : Fragment() {

    internal val viewModel: ProductsListNoFilterFlowViewModel by viewModels()

    @Inject
    lateinit var cartManager: CartManager

    @Inject
    lateinit var likeManager: LikeManager

    @Inject
    lateinit var ratingProductManager: RatingProductManager

    @Inject
    lateinit var navigatorFactory: ContentSearchNavigator.Factory

    private lateinit var searchNavigator: ContentSearchNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchNavigator = navigatorFactory.create(
            findNavController(), this
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>("category")
            ?.let { category ->
                viewModel.selectCategory(category)
            }

        findNavController().currentBackStackEntry?.savedStateHandle?.remove<FiltersUi>("filters")
            ?.let { filters ->
                viewModel.changeFilters(filters)
            }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState = pagingState.data
                    val lazyGridState = rememberLazyGridState()

                    ProductsNoFiltersScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        lazyGridState = lazyGridState
                    )

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToSearch -> {
                                    findNavController().navigateToSearch(event.query)
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToCategories -> {
                                    findNavController().navigateToCategories(
                                        category = event.currentCategory,
                                        categories = event.categories
                                    )
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToProductDetails -> {
                                    findNavController().navigateToProductDetails(event.productId)
                                }

                                ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.ScrollToTop -> {
                                    lazyGridState.animateScrollToItem(0);
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToProductFilters -> {
                                    findNavController().navigateToProductFilters(
                                        event.categoryId,
                                        event.filters
                                    )
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.Share -> {
                                    shareText(event.text)
                                }

                                is ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToProductAnalogs -> {
                                    findNavController().navigateToAnalogs(event.productId)
                                }

                                ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToQrCode -> {
                                    searchNavigator.navigateToImageSearch()
                                }

                                ProductsListNoFilterFlowViewModel.ProductListNoFilterEvent.GoToSpeech -> {
                                    searchNavigator.navigateToVoiceSearch()
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    sealed class DataSource : Parcelable {
        @Parcelize
        class Brand(val brandId: Long) : DataSource()

        @Parcelize
        data object HurryBuyUpProducts : DataSource()

        @Parcelize
        data object NewProducts : DataSource()

        @Parcelize
        data object ViewedProducts : DataSource()

        @Parcelize
        data class ButtonProducts(val buttonId: Int) : DataSource()

        @Parcelize
        data class Products(val bannerId: Long, val blockId: Long) : DataSource()

        @Parcelize
        data class Search(val query: String) : DataSource()

        @Parcelize
        data class Category(val categoryId: Long) : DataSource()

        @Parcelize
        data object Missing : DataSource()
    }

}
