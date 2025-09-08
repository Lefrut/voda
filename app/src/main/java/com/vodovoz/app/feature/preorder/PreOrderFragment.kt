package com.vodovoz.app.feature.preorder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.withTimeoutOrNull

@AndroidEntryPoint
class PreOrderFragment : Fragment() {

    private val viewModel: PreOrderFlowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.fetchPreOrderData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    val keyboardController = LocalSoftwareKeyboardController.current
                    val snackbarHostState = remember {
                        SnackbarHostState()
                    }

                    when (viewState.uiState) {
                        PreOrderFlowViewModel.UiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchPreOrderData() }
                        }

                        PreOrderFlowViewModel.UiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        PreOrderFlowViewModel.UiState.Success -> {
                            PreOrderScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                PreOrderFlowViewModel.PreOrderEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                PreOrderFlowViewModel.PreOrderEvent.HideKeyboard -> {
                                    keyboardController?.hide()
                                }

                                is PreOrderFlowViewModel.PreOrderEvent.ShowSnackbar -> {
                                    withTimeoutOrNull(if (event.isVeryShort) 150L else 1200L) {
                                        snackbarHostState.showSnackbar(
                                            message = event.message,
                                            duration = SnackbarDuration.Indefinite
                                        )
                                    }
                                }

                                is PreOrderFlowViewModel.PreOrderEvent.GoToWebView -> {
                                    findNavController().navigateToWebView(event.url, event.title)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}