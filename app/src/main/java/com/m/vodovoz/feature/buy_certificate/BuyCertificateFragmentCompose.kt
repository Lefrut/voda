package com.m.vodovoz.feature.buy_certificate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToFAQ
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BuyCertificateFragment : Fragment() {

    private val viewModel by viewModels<BuyCertificateViewModel>()

    @Inject
    lateinit var tabManager: TabManager

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
                    
                    val snackbarHostState = remember { SnackbarHostState() }

                    when (val uiState = viewState.uiState) {
                        BuyCertificateViewModel.BuyCertificateUiState.Body -> {
                            BuyCertificateScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        BuyCertificateViewModel.BuyCertificateUiState.Error -> {
                            NetworkErrorPlaceholder {
                                viewModel.fetchBuyCertificateDetails()
                            }
                        }

                        BuyCertificateViewModel.BuyCertificateUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        is BuyCertificateViewModel.BuyCertificateUiState.Success -> {
                            VodovozLongPlaceholder(
                                data = uiState.placeholder,
                                onCloseClick = { viewModel.navigateBack() },
                                onButtonClick = { viewModel.pay() }
                            )
                        }
                    }

                    LifecycleEffect(snackbarHostState) {
                        viewModel.events.collect { event ->
                            when (event) {
                                BuyCertificateViewModel.BuyCertificateEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is BuyCertificateViewModel.BuyCertificateEvents.OpenLink -> {

                                }




                                is BuyCertificateViewModel.BuyCertificateEvents.GoToFAQ -> {
                                    findNavController().navigateToFAQ(event.faq)
                                }

                                BuyCertificateViewModel.BuyCertificateEvents.GoToProfile -> {
                                    tabManager.setAuthRedirect(findNavController().graph.id)
                                    tabManager.selectTab(R.id.graph_profile)
                                }

                                is BuyCertificateViewModel.BuyCertificateEvents.GoToWebView -> {
                                    findNavController().navigateToWebView(
                                        event.url,
                                        requireContext().getString(R.string.space)
                                    )
                                }

                                is BuyCertificateViewModel.BuyCertificateEvents.OpenUrl -> {
                                    requireContext().openUrl(event.url)
                                }

                                is BuyCertificateViewModel.BuyCertificateEvents.ShowToast -> {
                                    launch { snackbarHostState.showSnackbar(event.message) }
                                }
                            }
                        }
                    }

                }
            }
        }
    }

}