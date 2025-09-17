package com.m.vodovoz.feature.cart.gifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.gifts.model.GiftsEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GiftsFragment : Fragment() {

    private val viewModel: GiftsViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        tabManager.changeTabVisibility(false)
        super.onStart()
    }

    override fun onStop() {
        tabManager.changeTabVisibility(true)
        super.onStop()
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

                    GiftsScreen(viewModel = viewModel, viewState = viewState)

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                GiftsEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is GiftsEvent.GoToCart -> {
                                    val navController = findNavController()

                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "gift", event.currentGift
                                    )
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