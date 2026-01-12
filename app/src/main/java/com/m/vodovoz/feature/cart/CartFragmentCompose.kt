package com.m.vodovoz.feature.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToAllBottles
import com.m.vodovoz.core.navigation.navigateToGifts
import com.m.vodovoz.core.navigation.navigateToOrdering
import com.m.vodovoz.core.navigation.navigateToProductAnalogs
import com.m.vodovoz.core.navigation.navigateToProductDetails
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.ForAdultsPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CartFragment : Fragment() {

    internal val viewModel: CartFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager


    override fun onStart() {
        super.onStart()
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.remove<CartPresentItemUi>("gift")
            ?.let { gift ->
                viewModel.addGiftToCart(gift)
            }
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
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


                    when (val uiState = viewState.uiState) {
                        CartFlowViewModel.CartUiState.Cart -> {
                            val forAdultsUi = viewState.forAdultsUi
                            if (forAdultsUi != null) {
                                ForAdultsPlaceholder(
                                    forAdults = forAdultsUi,
                                    onBackClick = viewModel::closeForAdultsPlaceholder,
                                    onApplyClick = viewModel::setCanViewForAdults
                                )

                                BackHandler {
                                    viewModel.closeForAdultsPlaceholder()
                                }

                            } else CartScreen(
                                viewModel = viewModel,
                                viewState = viewState
                            )
                        }

                        is CartFlowViewModel.CartUiState.Empty -> {
                            val placeholder = uiState.placeholder
                            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                                Text(
                                    text = placeholder.title.ifEmpty { stringResource(R.string.cart) },
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.headlineSmall,
                                    modifier = Modifier.padding(16.dp)
                                )

                                VodovozPlaceholder(
                                    data = placeholder,
                                    onButtonClick = { viewModel.navigateToCatalog() },
                                    onProductLike = viewModel::changeFavorite,
                                    onProductClick = viewModel::navigateToProductDetails,
                                    onDecrementToCart = viewModel::decrementProduct,
                                    onIncrementToCart = viewModel::incrementProduct,
                                    onAnalogsClick = viewModel::navigateToAnalogsOrShow18
                                )
                            }
                        }

                        CartFlowViewModel.CartUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchCartDetails() }
                        }

                        CartFlowViewModel.CartUiState.Loading -> {
                            LoadingPlaceholder()
                        }
                    }

                    LifecycleEffect {
                        observeEvents()
                    }

                    LaunchedEffect(viewState.showPromotionCodeBottomSheet) {
                        if (viewState.showPromotionCodeBottomSheet) tabManager.changeTabVisibility(
                            false
                        )
                        else {
                            delay(145)
                            tabManager.changeTabVisibility(true)
                        }
                    }

                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTabReselect()
        accountManager.reportEvent("Зашел в корзину")

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.changeTabVisibility(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    private suspend fun observeEvents() {
        viewModel.events
            .collect { event ->
                when (event) {
                    is CartFlowViewModel.CartEvents.GoToOrder -> {
                        if (findNavController().currentBackStackEntry?.destination?.id == R.id.orderingFragment) {
                            findNavController().popBackStack()
                        }
                        findNavController().navigateToOrdering(event.coupon)
                    }

                    is CartFlowViewModel.CartEvents.GoToGifts -> {
                        findNavController().navigateToGifts(
                            event.present,
                            event.popupWindow
                        )
                    }

                    is CartFlowViewModel.CartEvents.GoToProfile -> {
                        tabManager.setAuthRedirect(findNavController().graph.id)
                        tabManager.selectTab(R.id.graph_profile)
                    }

                    is CartFlowViewModel.CartEvents.GoToProductDetails -> {
                        findNavController().navigateToProductDetails(event.productId)
                    }

                    CartFlowViewModel.CartEvents.GoToCatalog -> {
                        tabManager.selectTab(R.id.graph_catalog)
                    }

                    is CartFlowViewModel.CartEvents.GoToAllBottles -> {
                        findNavController().navigateToAllBottles(event.bottles)
                    }

                    is CartFlowViewModel.CartEvents.GoToAnalogs -> {
                        findNavController().navigateToProductAnalogs(event.productId)
                    }
                }
            }
    }

    private fun observeTabReselect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager.observeTabReselect()
                    .collect { id ->
                        if (id != TabManager.DEFAULT_STATE && id == R.id.cartFragment) {
                            tabManager.setDefaultState()
                        }
                    }
            }
        }
    }
}