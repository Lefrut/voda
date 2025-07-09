package com.vodovoz.app.feature.cart.ordering

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToAddresses
import com.vodovoz.app.core.navigation.navigateToDeliveryDate
import com.vodovoz.app.core.navigation.navigateToOrderCallYou
import com.vodovoz.app.core.navigation.navigateToOrderRecipient
import com.vodovoz.app.core.navigation.navigateToPaymentMethod
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.VodovozLongPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi
import com.vodovoz.app.feature.addresses.model.AddressUi
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.delivery_date.model.DeliveryDateOptionUi
import com.vodovoz.app.feature.delivery_date.model.DeliveryTimeIntervalUi
import com.vodovoz.app.feature.order_call_you.model.CallYouItemUi
import com.vodovoz.app.feature.payment_method.model.PaymentMethodItemNav
import com.vodovoz.app.feature.payment_method.model.toUi
import com.vodovoz.app.util.extensions.openUrl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onSubscription
import javax.inject.Inject

@AndroidEntryPoint
class OrderingFragment : Fragment() {

    private val viewModel: OrderingFlowViewModel by viewModels()
    private val cartViewModel: CartFlowViewModel by activityViewModels()

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var tabManager: TabManager

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

                    when (val uiState = viewState.uiState) {
                        OrderingFlowViewModel.OrderingUiState.Error,
                        OrderingFlowViewModel.OrderingUiState.Loading,
                        OrderingFlowViewModel.OrderingUiState.Order -> {
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
                                    viewModel.navigateBackWithRefresh()
                                },
                                onButtonClick = {
                                    viewModel.activatePayButton(uiState.placeholder.button)
                                }
                            )

                            DisposableEffect(Unit) {
                                tabManager.changeTabVisibility(false)
                                onDispose {
                                    tabManager.changeTabVisibility(true)
                                }
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
        viewModel.observeEvent().onSubscription {

            val backEntrySavedStateHandle =
                findNavController().currentBackStackEntry?.savedStateHandle

            backEntrySavedStateHandle?.remove<AddressUi>("address")
                ?.let { address -> viewModel.setAddress(address) }

            val timeInterval =
                backEntrySavedStateHandle?.remove<DeliveryTimeIntervalUi>("timeInterval")
            val date = backEntrySavedStateHandle?.remove<DeliveryDateOptionUi>("dateOption")

            if (timeInterval != null && date != null) {
                viewModel.setDeliveryDateTime(timeInterval, date)
            }

            backEntrySavedStateHandle?.remove<CallYouItemUi>("callYou")
                ?.let { callYouItem -> viewModel.setCallYou(callYouItem) }

            backEntrySavedStateHandle?.remove<PaymentMethodItemNav>("paymentBalance")?.toUi()
                ?.let { paymentBalance -> viewModel.setPaymentBalance(paymentBalance) }

            backEntrySavedStateHandle?.remove<PaymentMethodItemNav>("paymentMethod")?.toUi()
                ?.let { paymentMethod -> viewModel.setPaymentMethod(paymentMethod) }

            backEntrySavedStateHandle?.remove<Boolean>("updateRecipient")?.let {
                viewModel.refreshRecipient()
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
                    cartViewModel.refresh()
                }

                is OrderingFlowViewModel.OrderingEvents.GoToWebView -> {
                    findNavController().navigateToWebView(
                        title = context?.getString(
                            R.string.space
                        ) ?: "",
                        url = event.url
                    )
                }
                is OrderingFlowViewModel.OrderingEvents.OpenUrl -> {
                    context?.openUrl(event.url)
                }
            }
        }
    }


}
