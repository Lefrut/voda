package com.vodovoz.app.feature.write_message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.write_message.model.WriteMessageEvent
import com.vodovoz.app.feature.write_message.model.WriteMessageUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WriteMessageFragment @Inject constructor() : Fragment() {

    private val viewModel by viewModels<WriteMessageViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    val snackbarHostState = remember { SnackbarHostState() }

                    when(val uiState = viewState.uiState){
                        is WriteMessageUiState.Success -> {
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
                        else ->{
                            WriteMessageScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }

                    LifecycleEffect(Unit) {
                        viewModel.events.collect { event ->
                            when (event) {
                                WriteMessageEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is WriteMessageEvent.ShowSnackbar -> {
                                    snackbarHostState.showSnackbar(event.message)
                                }
                            }
                        }
                    }
                }
            }

        }
    }

}