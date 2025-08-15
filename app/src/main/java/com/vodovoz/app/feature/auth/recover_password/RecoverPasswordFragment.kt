package com.vodovoz.app.feature.auth.recover_password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordEvent
import com.vodovoz.app.feature.auth.recover_password.model.RecoverPasswordUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecoverPasswordFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    private val viewModel by viewModels<RecoverPasswordViewModel>()

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

                    when (val uiState = viewState.uiState) {
                        RecoverPasswordUiState.Body -> {
                            RecoverPasswordScreen(viewModel = viewModel, viewState = viewState)
                        }

                        RecoverPasswordUiState.Error -> {
                            NetworkErrorPlaceholder {
                                viewModel.fetchRecoverPasswordDetails()
                            }
                        }

                        RecoverPasswordUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        is RecoverPasswordUiState.Success -> {
                            VodovozLongPlaceholder(
                                data = uiState.placeholder,
                                onButtonClick = {
                                    viewModel.navigateBack()
                                },
                                onCloseClick = {
                                    viewModel.navigateBack()
                                }
                            )
                        }
                    }


                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                RecoverPasswordEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is RecoverPasswordEvent.GoToWebView -> {
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
