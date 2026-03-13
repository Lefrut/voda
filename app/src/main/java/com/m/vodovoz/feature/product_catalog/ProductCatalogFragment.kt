package com.m.vodovoz.feature.product_catalog

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.ContentSearchNavigator
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategories
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.core.navigation.navigateToProductFilters
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.filters.FiltersUi
import com.m.vodovoz.feature.home.model.CategoryUi
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.shareText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ProductCatalogFragment : Fragment() {

    internal val viewModel: ProductCatalogViewModel by viewModels()


    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var navigatorFactory: ContentSearchNavigator.Factory

    private lateinit var searchNavigator: ContentSearchNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchNavigator = navigatorFactory.create(
            findNavController(), this
        )
    }

    override fun onStart() {
        super.onStart()
        findNavController().currentBackStackEntry?.savedStateHandle?.remove<CategoryUi>("category")
            ?.let { category ->
                viewModel.selectCategory(category)
            }

        findNavController().currentBackStackEntry?.savedStateHandle?.remove<FiltersUi>("filters")
            ?.let { filters ->
                viewModel.changeFilters(filters)
            }
    }


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

                    val lazyGridState = rememberLazyGridState()
                    val context = LocalContext.current


                    when (val uiState = viewState.uiState) {
                        is ProductCatalogViewModel.ProductCatalogUiState.ForAdults -> {
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
                            ProductCatalogScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                lazyGridState = lazyGridState
                            )
                        }
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                ProductCatalogViewModel.ProductCatalogEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.GoToSearch -> {
                                    findNavController().navigateToSearch(event.query)
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.GoToCategories -> {
                                    findNavController().navigateToCategories(
                                        category = event.currentCategory,
                                        categories = event.categories
                                    )
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductDetails -> {
                                    findNavController().navigateToProductDetails(event.productId)
                                }

                                ProductCatalogViewModel.ProductCatalogEvent.ScrollToTop -> {
                                    lazyGridState.animateScrollToItem(0);
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductFilters -> {
                                    findNavController().navigateToProductFilters(
                                        event.categoryId,
                                        event.filters
                                    )
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.Share -> {
                                    context.shareText(event.text)
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.GoToProductAnalogs -> {
                                    findNavController().navigateToProductAnalogs(event.productId)
                                }

                                ProductCatalogViewModel.ProductCatalogEvent.GoToQrCode -> {
                                    searchNavigator.navigateToImageSearch()
                                }

                                ProductCatalogViewModel.ProductCatalogEvent.GoToSpeech -> {
                                    searchNavigator.navigateToVoiceSearch()
                                }

                                ProductCatalogViewModel.ProductCatalogEvent.GoToCatalog -> {
                                    findNavController().popBackStack()
                                    tabManager.selectTab(R.id.graph_catalog)
                                }

                                is ProductCatalogViewModel.ProductCatalogEvent.ActivateAction -> {
                                    event.action.activate(
                                        navController = findNavController(),
                                        context = requireActivity(),
                                        cookie = cookieManager.fetchCookieSessionId() ?: "",
                                        tabManager = tabManager
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @Stable
    sealed class DataSource : Parcelable {
        @Parcelize
        @Immutable
        class Brand(val brandId: Long) : DataSource()

        @Parcelize
        data object HurryBuyUpProducts : DataSource()

        @Parcelize
        data object NewProducts : DataSource()

        @Parcelize
        data object ViewedProducts : DataSource()

        @Parcelize
        data object PastPurchases : DataSource()

        @Parcelize
        @Immutable
        data class ButtonProducts(val buttonId: Int) : DataSource()

        @Parcelize
        @Immutable
        data class Products(val bannerId: Long, val blockId: Long) : DataSource()

        @Parcelize
        @Immutable
        data class Search(val query: String) : DataSource()

        @Parcelize
        @Immutable
        data class Category(val categoryId: Long) : DataSource()

        @Parcelize
        @Immutable
        data object Missing : DataSource()
    }

}
