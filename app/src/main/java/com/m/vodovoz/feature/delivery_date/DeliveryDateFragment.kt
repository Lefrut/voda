package com.m.vodovoz.feature.delivery_date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeliveryDateFragment : Fragment() {

    private val viewModel by viewModels<DeliveryDateViewModel>()

    @Inject
    lateinit var tabManager: TabManager

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
                    val viewState by viewModel.collectAsState()

                    DeliveryDateScreen(
                        viewState = viewState,
                        viewModel = viewModel
                    )

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                DeliveryDateEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is DeliveryDateEvent.GoBackToOrdering -> {
                                    val navController = findNavController()
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set("timeInterval", event.timeInterval)
                                        set("dateOption", event.dateOption)
                                        set("earlierCheckbox", event.earlierCheckbox)
                                    }
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}