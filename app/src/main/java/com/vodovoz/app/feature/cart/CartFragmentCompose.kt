package com.vodovoz.app.feature.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToAllBottles
import com.vodovoz.app.core.navigation.navigateToGifts
import com.vodovoz.app.core.navigation.navigateToOrdering
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.cart.model.CartPresentItemUi
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
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)

                    when (val uiState = viewState.uiState) {
                        CartFlowViewModel.CartUiState.Cart -> {
                            CartScreen(
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
                                    onButtonClick = { viewModel.navigateToCatalog() }
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
                        viewModel.listenFavorites()
                    }

                    LifecycleEffect {
                        observeEvents()
                    }
                    LaunchedEffect(viewState.showPromotionCodeBottomSheet) {
                        if (viewState.showPromotionCodeBottomSheet) tabManager.changeTabVisibility(false)
                        else {
                            delay(75)
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
        viewModel.observeEvent()
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

                    CartFlowViewModel.CartEvents.GoToAllBottles -> {
                        findNavController().navigateToAllBottles()
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