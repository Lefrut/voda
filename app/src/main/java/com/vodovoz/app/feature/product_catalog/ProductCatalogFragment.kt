package com.vodovoz.app.feature.product_catalog

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.ContentSearchNavigator
import com.vodovoz.app.core.navigation.navigateToCategories
import com.vodovoz.app.core.navigation.navigateToProductAnalogs
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToProductFilters
import com.vodovoz.app.core.navigation.navigateToSearch
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.ForAdultsPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.design_system.model.filters.FiltersUi
import com.vodovoz.app.feature.home.model.CategoryUi
import com.vodovoz.app.util.extensions.shareText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ProductCatalogFragment : Fragment() {

    internal val viewModel: ProductCatalogViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

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
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState = pagingState.data
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

                            DisposableEffect(Unit) {
                                tabManager.changeTabVisibility(false)
                                onDispose {
                                    tabManager.changeTabVisibility(true)
                                }
                            }
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
                        viewModel.listenCanViewAdultProducts()
                    }

                    LifecycleEffect {
                        viewModel.listenCart()
                    }

                    LifecycleEffect {
                        viewModel.listenProductLoadings()
                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
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
