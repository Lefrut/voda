package com.vodovoz.app.feature.all.promotions

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.core.navigation.navigateToPromotionDetails
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize

@AndroidEntryPoint
class AllPromotionsFragment : Fragment() {

    private val viewModel: AllPromotionsFlowViewModel by viewModels()

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

                    val lazyListState = rememberLazyListState()

                    AllPromotionsScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        lazyListState = lazyListState
                    )

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            val navController = findNavController()
                            when (event) {
                                AllPromotionsFlowViewModel.AllPromotionsEvent.ScrollTop -> {
                                    lazyListState.animateScrollToItem(0)
                                }

                                is AllPromotionsFlowViewModel.AllPromotionsEvent.GoToProductDetails -> {
                                    navController.navigateToPromotionDetails(event.promotionId)
                                }

                                AllPromotionsFlowViewModel.AllPromotionsEvent.GoBack -> {
                                    navController.popBackStack()
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
        class ByBanner(val bannerId: Long, val blockId: Long) : DataSource()

        @Parcelize
        data object All : DataSource()
    }
}


