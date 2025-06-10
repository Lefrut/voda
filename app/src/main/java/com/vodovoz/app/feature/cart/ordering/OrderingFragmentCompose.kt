package com.vodovoz.app.feature.cart.ordering

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.core.navigation.navigateToAddresses
import com.vodovoz.app.core.navigation.navigateToDeliveryDate
import com.vodovoz.app.core.navigation.navigateToPaymentMethod
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderingFragment : Fragment() {

    private val viewModel: OrderingFlowViewModel by viewModels()

    @Inject
    lateinit var accountManager: AccountManager

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
                    val scrollState = rememberScrollState()

                    OrderingScreen(
                        viewModel = viewModel,
                        viewState = viewState,
                        scrollState = scrollState
                    )

                    LifecycleEffect {
                        observeEvents()
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountManager.reportEvent("Зашел на экран оформления заказа")
    }


    private suspend fun observeEvents() {
        viewModel.observeEvent().collect { event ->
            when (event) {
                OrderingFlowViewModel.OrderingEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                OrderingFlowViewModel.OrderingEvents.GoToAddresses -> {
                    findNavController().navigateToAddresses(AddressScreenTypeUi.Choose)
                }

                is OrderingFlowViewModel.OrderingEvents.GoToDeliveryDate -> {
                    findNavController().navigateToDeliveryDate(event.addressId)
                }

                is OrderingFlowViewModel.OrderingEvents.GoToPaymentMethod ->{
                    findNavController().navigateToPaymentMethod(event.addressId, event.date)
                }

                else -> {

                }
            }
        }
    }


}
