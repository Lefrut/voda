package com.vodovoz.app.feature.service_order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceOrderFragment : Fragment() {

    private val viewModel: ServiceOrderViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        tabManager.changeTabVisibility(false)
        tabManager.changeTabWindowInsets(true)
    }

    override fun onStop() {
        super.onStop()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        tabManager.changeTabWindowInsets(false)
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
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

                    when (val uiState = viewState.uiState) {
                        is ServiceOrderViewModel.ServiceOrderUiState.Success -> {
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

                        else -> {
                            ServiceOrderScreen(
                                viewModel = viewModel,
                                viewState = viewState
                            )
                        }

                    }

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when (event) {
                                ServiceOrderViewModel.ServiceOrderEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.changeTabWindowInsets(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }
    }

}
