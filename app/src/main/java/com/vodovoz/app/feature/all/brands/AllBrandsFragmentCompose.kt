package com.vodovoz.app.feature.all.brands

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToBrandProductList
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllBrandsFragment : Fragment() {

    private val viewModel: AllBrandsFlowViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.state.collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState)

                    when (viewState.uiState) {
                        AllBrandsFlowViewModel.AllBrandsUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        AllBrandsFlowViewModel.AllBrandsUiState.Success -> {
                            AllBrandsScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }


                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                AllBrandsFlowViewModel.AllBrandsEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is AllBrandsFlowViewModel.AllBrandsEvents.GoToBrandProducts -> {
                                    findNavController().navigateToBrandProductList(event.brandId)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
