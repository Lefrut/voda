package com.vodovoz.app.feature.write_comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.write_comment.model.WriteCommentEvent
import com.vodovoz.app.feature.write_comment.model.WriteCommentUiState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WriteCommentFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    private val viewModel: WriteCommentViewModel by viewModels<WriteCommentViewModel>()

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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pickImagesLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenMultipleDocuments()
                    ) { uri -> viewModel.addUri(uri) }

                    val snackbarHostState = remember { SnackbarHostState() }
                    val viewState by viewModel.collectAsState()


                    when (val uiState = viewState.uiState) {
                        WriteCommentUiState.Comment -> {
                            WriteCommentScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        is WriteCommentUiState.Success -> {
                            VodovozLongPlaceholder(
                                data = uiState.placeholder,
                                onButtonClick = { viewModel.navigateBack() },
                                onCloseClick = { viewModel.navigateBack() }
                            )
                        }
                    }

                    LifecycleEffect(snackbarHostState) {
                        viewModel.events.collect { event ->
                            when (event) {
                                WriteCommentEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                WriteCommentEvent.OpenImagePicker -> {
                                    pickImagesLauncher.launch(arrayOf("image/*"))
                                }

                                is WriteCommentEvent.SetRatedProductResult -> {
                                    val navController = findNavController()
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        key = "ratedProductId",
                                        value = event.productId
                                    )
                                }

                                is WriteCommentEvent.ShowSnackbar -> {
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