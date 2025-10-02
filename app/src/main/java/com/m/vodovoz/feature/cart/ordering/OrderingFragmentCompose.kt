package com.m.vodovoz.feature.cart.ordering

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToAddresses
import com.m.vodovoz.core.navigation.navigateToDeliveryDate
import com.m.vodovoz.core.navigation.navigateToOrderCallYou
import com.m.vodovoz.core.navigation.navigateToOrderRecipient
import com.m.vodovoz.core.navigation.navigateToPaymentMethod
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.addresses.model.AddressUi
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.delivery_date.model.DeliveryDateOptionUi
import com.m.vodovoz.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.m.vodovoz.feature.order_call_you.model.CallYouItemUi
import com.m.vodovoz.feature.payment_method.model.PaymentMethodItemNav
import com.m.vodovoz.feature.payment_method.model.toUi
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderingFragment : Fragment() {

    private val viewModel: OrderingFlowViewModel by viewModels()
    private val cartViewModel: CartFlowViewModel by activityViewModels()

    @Inject
    lateinit var accountManager: AccountManager

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

                    val scrollState = rememberScrollState()

                    when (val uiState = viewState.uiState) {
                        OrderingFlowViewModel.OrderingUiState.Error,
                        OrderingFlowViewModel.OrderingUiState.Loading,
                        OrderingFlowViewModel.OrderingUiState.Order,
                        -> {
                            OrderingScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                scrollState = scrollState
                            )
                        }

                        is OrderingFlowViewModel.OrderingUiState.Success -> {
                            VodovozLongPlaceholder(
                                data = uiState.placeholder,
                                onCloseClick = {
                                    viewModel.navigateToHomeWithRefresh()
                                },
                                onButtonClick = {
                                    viewModel.activatePayButton(uiState.placeholder.button)
                                }
                            )

                            LaunchedEffect(Unit) {
                                tabManager.changeTabVisibility(true)
                            }
                        }
                    }


                    LifecycleEffect {
                        observeEvents(scrollState)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        accountManager.reportEvent("Зашел на экран оформления заказа")
    }


    private suspend fun observeEvents(scrollState: ScrollState) {
        viewModel.events.onSubscription {

            val backEntrySavedStateHandle =
                findNavController().currentBackStackEntry?.savedStateHandle

            backEntrySavedStateHandle?.apply {
                remove<AddressUi>("address")?.let { address ->
                    viewModel.setAddress(address)
                }

                val timeInterval = remove<DeliveryTimeIntervalUi>("timeInterval")
                val date = remove<DeliveryDateOptionUi>("dateOption")

                if (timeInterval != null && date != null) {
                    viewModel.setDeliveryDateTime(timeInterval, date)
                }

                remove<CheckboxUi>("earlierCheckbox")?.let { checkbox ->
                    viewModel.setEarlierDelivery(checkbox)
                }

                remove<CallYouItemUi>("callYou")?.let { callYouItem ->
                    viewModel.setCallYou(callYouItem)
                }

                remove<PaymentMethodItemNav>("paymentBalance")?.toUi()?.let { paymentBalance ->
                    viewModel.setPaymentBalance(paymentBalance)
                }

                remove<PaymentMethodItemNav>("paymentMethod")?.toUi()?.let { paymentMethod ->
                    viewModel.setPaymentMethod(paymentMethod)
                }

                remove<Boolean>("updateRecipient")?.let {
                    viewModel.refreshRecipient()
                }
            }


        }.collect { event ->
            when (event) {
                OrderingFlowViewModel.OrderingEvents.GoBack -> {
                    findNavController().popBackStack()
                }

                is OrderingFlowViewModel.OrderingEvents.GoToAddresses -> {
                    findNavController().navigateToAddresses(
                        AddressScreenTypeUi.Choose,
                        event.addressId
                    )
                }

                is OrderingFlowViewModel.OrderingEvents.GoToDeliveryDate -> {
                    findNavController().navigateToDeliveryDate(
                        earlierDelivery = event.earlierDelivery,
                        addressId = event.addressId,
                        date = event.date,
                        timeInterval = event.timeInterval
                    )
                }

                is OrderingFlowViewModel.OrderingEvents.GoToPaymentMethod -> {
                    findNavController().navigateToPaymentMethod(
                        event.addressId,
                        event.date,
                        event.paymentMethodId,
                        event.balance
                    )
                }

                is OrderingFlowViewModel.OrderingEvents.GoToOrderRecipient -> {
                    findNavController().navigateToOrderRecipient(event.addressId)
                }

                is OrderingFlowViewModel.OrderingEvents.GoToCallYou -> {
                    findNavController().navigateToOrderCallYou(event.addressId, event.callYouId)
                }

                OrderingFlowViewModel.OrderingEvents.ScrollToTop -> {
                    scrollState.animateScrollTo(0)
                }

                OrderingFlowViewModel.OrderingEvents.RefreshCart -> {
                    tabManager.clearBottomNavCartState()
                    cartViewModel.refresh()
                }

                is OrderingFlowViewModel.OrderingEvents.GoToWebView -> {
                    findNavController().navigateToWebView(
                        title = context?.getString(
                            R.string.space
                        ) ?: "",
                        url = event.url,
                        navOptions = navOptions {
                            popUpTo(R.id.cartFragment) {
                                inclusive = false
                            }
                        }
                    )
                }

                is OrderingFlowViewModel.OrderingEvents.OpenUrl -> {
                    context?.openUrl(event.url)
                    findNavController().popBackStack()
                }

                OrderingFlowViewModel.OrderingEvents.GoToHome -> {
                    findNavController().popBackStack()
                    tabManager.selectTab(R.id.graph_home)
                }

                OrderingFlowViewModel.OrderingEvents.ScrollToBottom -> {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

                OrderingFlowViewModel.OrderingEvents.UpdateBottomCart -> {
                    viewLifecycleOwner.lifecycleScope.launch {
                        tabManager.updateBottomNavCartState()
                    }
                }
            }
        }
    }


}
