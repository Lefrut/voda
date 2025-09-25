package com.m.vodovoz.feature.service_order

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.preorder.model.FormUi
import com.m.vodovoz.feature.preorder.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.FormMviViewModel
import com.m.vodovoz.ui.mvi.FormState
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ServiceOrderViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourceProvider: ResourcesProvider,
    savedStateHandle: SavedStateHandle,
) : FormMviViewModel<ServiceOrderViewModel.ServiceOrderState, ServiceOrderViewModel.ServiceOrderEvent>(
    ServiceOrderState()
) {


    private val serviceType = savedStateHandle.get<String>("serviceType") ?: ""

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


}