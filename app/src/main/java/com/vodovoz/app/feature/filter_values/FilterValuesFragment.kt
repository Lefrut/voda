package com.vodovoz.app.feature.filter_values

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment


import androidx.fragment.app.viewModels
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterValuesFlowFragment : Fragment() {


    private val viewModel: FilterValuesViewModel by viewModels()

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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    

                    when (viewState.uiState) {
                        FilterValuesViewModel.ConcreteFilterUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        FilterValuesViewModel.ConcreteFilterUiState.Success -> {
                            FilterValuesScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                FilterValuesViewModel.ConcreteFilterEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is FilterValuesViewModel.ConcreteFilterEvent.GoToProductFilters -> {
                                    val navController = findNavController()
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "filter",
                                        event.filter
                                    )
                                    findNavController().popBackStack(
                                        R.id.productFiltersFragment, false
                                    )
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}