package com.vodovoz.app.feature.service_order

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.KeyboardTypeValidator
import com.vodovoz.app.design_system.model.widgets.NameValidator
import com.vodovoz.app.design_system.model.widgets.NoRequiredValidator
import com.vodovoz.app.design_system.model.widgets.PhoneNumberValidator
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.getErrorText
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateField
import com.vodovoz.app.util.extensions.singleResult
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
) : PagingContractViewModel<ServiceOrderViewModel.ServiceOrderState, ServiceOrderViewModel.ServiceOrderEvent>(
    ServiceOrderState()
) {


    private val serviceType = savedStateHandle.get<String>("serviceType") ?: navigateBack().run { "" }

    init {
        viewModelScope.launch { delay(200L) }.invokeOnCompletion {
            fetchServiceOrderDetails()
        }
    }

    private fun fetchServiceOrderDetails() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = ServiceOrderUiState.Loading)
        }

        val serviceOrderDetailsResult = vodovozServiceRepository.getServiceOrderDetails(
            serviceType
        ).singleResult()

        serviceOrderDetailsResult.onSuccess { serviceOrderDetails ->
            uiStateListener.updateData { s ->
                s.copy(
                    title = serviceOrderDetails.title,
                    subtitle = serviceOrderDetails.subtitle,
                    fields = serviceOrderDetails.fields.mapToUi(),
                    button = serviceOrderDetails.button.toUi().copy(enabled = false),
                    uiState = ServiceOrderUiState.Form
                )
            }
        }.onFailure {
            navigateBack()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(ServiceOrderEvent.GoBack)
    }

    fun doOrderService() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        dataState.fields.checkFields(
            putErrors = true,
            validators = listOf(
                NoRequiredValidator,
                PhoneNumberValidator,
                NameValidator,
                KeyboardTypeValidator
            ),
            getSupportingText = { field ->
                field.getErrorText { resId ->
                    resourceProvider.getString(resId)
                }
            }
        ) { updatedFields, isValid ->

            if (isValid) return@checkFields

            uiStateListener.updateData { s ->
                s.copy(
                    fields = updatedFields,
                    button = s.button.copy(
                        enabled = false,
                        loading = false
                    )
                )
            }

            return@launch

        }

        vodovozServiceRepository.orderService(serviceType, dataState.fields.mapToDomain())
            .singleResult().onSuccess { placeholder ->
                uiStateListener.updateData { s ->
                    s.copy(uiState = ServiceOrderUiState.Success(placeholder.toUi()))
                }
            }.onFailure { t ->
                uiStateListener.updateData { s ->
                    s.copy(
                        button = s.button.copy(
                            loading = false,
                            enabled = false
                        ),
                    )
                }
            }
    }

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            val updatedFields = s.fields.updateField(
                field,
                updatedField
            )
            s.copy(
                fields = updatedFields,
                button = s.button.copy(
                    enabled = updatedFields.checkFields(
                        validators = listOf(
                            PhoneNumberValidator,
                            EmptyTextValidator
                        )
                    )
                )
            )
        }
    }

    @Immutable
    data class ServiceOrderState(
        val title: String = "",
        val subtitle: String = "",
        val fields: List<FieldUi> = emptyList(),
        val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
        val uiState: ServiceOrderUiState = ServiceOrderUiState.Loading,
    ) : State

    sealed interface ServiceOrderEvent : Event {
        data object GoBack : ServiceOrderEvent
    }

    sealed interface ServiceOrderUiState {
        data object Loading : ServiceOrderUiState
        data object Form : ServiceOrderUiState
        data class Success(val placeholder: VodovozPlaceholderUi) : ServiceOrderUiState
    }

}