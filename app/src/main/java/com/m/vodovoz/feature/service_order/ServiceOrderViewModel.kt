package com.m.vodovoz.feature.service_order

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.preorder.model.FormUi
import com.m.vodovoz.feature.preorder.model.toUi
import com.m.vodovoz.feature.service_order.api.ServiceOrderNavKey
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.FormMviViewModel
import com.m.vodovoz.ui.mvi.FormState
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = ServiceOrderViewModel.Factory::class)
@Stable
class ServiceOrderViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    val insetsState: InsetsVisibilityState,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourceProvider: ResourcesProvider,
    @Assisted private val navKey: ServiceOrderNavKey,
) : FormMviViewModel<ServiceOrderViewModel.ServiceOrderState, ServiceOrderViewModel.ServiceOrderEvent>(
    ServiceOrderState()
) {


    private val serviceType = navKey.serviceType

    init {
        viewModelScope.launch { delay(200L) }.invokeOnCompletion {
            fetchServiceOrderDetails()
        }
    }

    private fun fetchServiceOrderDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = ServiceOrderUiState.Loading)
        }

        val serviceOrderDetailsResult = vodovozServiceRepository.getServiceOrderDetails(
            serviceType
        ).singleResult()

        serviceOrderDetailsResult.onSuccess { form ->

            updateState { s ->
                s.copy(
                    form = form.toUi(),
                    uiState = ServiceOrderUiState.Form
                )
            }
        }.onFailure {
            navigateBack()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ServiceOrderEvent.GoBack)
    }

    fun doOrderService() = viewModelScope.launch {
        if (!validateWidgets(resourceProvider::getString)) return@launch


        updateForm {
            copy(button = button.copy(loading = true))
        }

        val form = stateSnapshot.form

        val queries = (form.fields + form.checkbox)
            .filterIsInstance<WidgetUi>()
            .associate { widget -> widget.id to widget.value() }

        vodovozServiceRepository.orderService(serviceType, queries)
            .singleResult().onSuccess { placeholder ->
                updateState { s ->
                    s.copy(uiState = ServiceOrderUiState.Success(placeholder.toUi()))
                }
            }.onFailure {
                updateForm {
                    copy(
                        button = button.copy(
                            loading = false,
                            enabled = false
                        )
                    )
                }
            }
    }

    override fun ServiceOrderState.withForm(form: FormUi): ServiceOrderState {
        return copy(form = form)
    }

    fun navigateToWebView(url: String, title: String) = viewModelScope.launch {
        sendEvent(ServiceOrderEvent.GoToWebView(url, title))
    }


    @Immutable
    data class ServiceOrderState(
        override val form: FormUi = FormUi.Empty,
        val uiState: ServiceOrderUiState = ServiceOrderUiState.Loading,
    ) : FormState()

    sealed interface ServiceOrderEvent : Event {
        data class GoToWebView(
            val url: String,
            val title: String,
        ) : ServiceOrderEvent

        data object GoBack : ServiceOrderEvent
    }

    @Stable
    sealed interface ServiceOrderUiState {
        data object Loading : ServiceOrderUiState
        data object Form : ServiceOrderUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : ServiceOrderUiState
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: ServiceOrderNavKey): ServiceOrderViewModel
    }
}
