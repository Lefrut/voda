package com.m.vodovoz.feature.all.brands

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
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToBrandProductList
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
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
                    val viewState by viewModel.collectAsState()

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
