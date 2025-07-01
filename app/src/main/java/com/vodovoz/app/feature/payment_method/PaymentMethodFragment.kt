package com.vodovoz.app.feature.payment_method

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.payment_method.model.PaymentMethodEvent
import com.vodovoz.app.feature.payment_method.model.toNav
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentMethodFragment : Fragment() {

    private val viewModel by viewModels<PaymentMethodViewModel>()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onPause() {
        super.onPause()
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
                    val viewState by viewModel.state.collectAsStateWithLifecycle()

                    PaymentMethodScreen(viewModel = viewModel, viewState = viewState)

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                PaymentMethodEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is PaymentMethodEvent.GoBackToOrdering -> {
                                    val navController = findNavController()
                                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                                        set("paymentBalance", event.paymentBalance?.toNav())
                                        set("paymentMethod", event.paymentMethod?.toNav())
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