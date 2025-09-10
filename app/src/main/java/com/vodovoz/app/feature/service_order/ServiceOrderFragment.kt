package com.vodovoz.app.feature.service_order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.ui.insets.InsetsVisibilityState
import com.vodovoz.app.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServiceOrderFragment : Fragment() {

    private val viewModel: ServiceOrderViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var insetsState: InsetsVisibilityState

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
        insetsState.consumeSystemBarInsets(false)
    }

    override fun onStop() {
        super.onStop()
        insetsState.consumeSystemBarInsets(true)
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
                    val viewState by viewModel.collectAsState()



                    ServiceOrderScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )


                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            val navController = findNavController()
                            when (event) {
                                ServiceOrderViewModel.ServiceOrderEvent.GoBack -> {
                                    navController.popBackStack()
                                }

                                is ServiceOrderViewModel.ServiceOrderEvent.GoToWebView -> {
                                    navController.navigateToWebView(event.url, event.title)
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
            insetsState.consumeNavigationBarInsets(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }
    }

}
