package com.vodovoz.app.feature.service_order

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.EmptyTextValidator
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.preorder.model.KeyboardTypeValidator
import com.vodovoz.app.feature.preorder.model.NameValidator
import com.vodovoz.app.feature.preorder.model.NoRequiredValidator
import com.vodovoz.app.feature.preorder.model.PhoneNumberValidator
import com.vodovoz.app.feature.preorder.model.checkFields
import com.vodovoz.app.feature.preorder.model.getErrorText
import com.vodovoz.app.feature.preorder.model.mapToDomain
import com.vodovoz.app.feature.preorder.model.mapToUi
import com.vodovoz.app.feature.preorder.model.updateField
import com.vodovoz.app.mapper.ServiceOrderFormFieldMapper.mapToUI
import com.vodovoz.app.ui.model.ServiceOrderFormFieldUI
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ServiceOrderViewModel @Inject constructor(
    private val repository: MainRepository,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourceProvider: ResourcesProvider,
    savedStateHandle: SavedStateHandle,
) : PagingContractViewModel<ServiceOrderViewModel.ServiceOrderState, ServiceOrderViewModel.ServiceOrderEvent>(
    ServiceOrderState()
) {


    private val serviceType = savedStateHandle.get<String>("serviceType") ?: navigateBack().run { "" }

    init {
        fetchServiceOrderDetails()
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

    fun fetchData() {

        viewModelScope.launch {
            uiStateListener.value =
                state.copy(isFirstLoad = true, loadingPage = true, data = ServiceOrderState())
            val userId = accountManager.fetchAccountId()
            if (userId == null) {
                uiStateListener.value =
                    state.copy(
                        error = ErrorState.Error(
                            messageInfo = "Авторизуйтесь, пожалуйста",
                            desc = ""
                        ),
                        loadingPage = false
                    )
                return@launch
            }
            flow {
                emit(
                    repository.fetchFormForOrderService(
                        type = serviceType,
                        userId = userId
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        response.data.mapToUI().let { data ->
                            uiStateListener.value = state.copy(
                                data = state.data.copy(
                                    serviceOrderFormFieldUIListMLD = data
                                ),
                                loadingPage = false,
                                error = null
                            )
                        }
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .catch {
                    debugLog { "fetch Form For Order error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun orderService(value: String) {

        viewModelScope.launch {
            uiStateListener.value =
                state.copy(isFirstLoad = true, loadingPage = true, data = ServiceOrderState())
            val userId = accountManager.fetchAccountId() ?: return@launch
            flow {
                emit(
                    repository.orderService(
                        type = serviceType,
                        userId = userId,
                        value = value
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                successMessageMLD = response.data
                            ),
                            loadingPage = false,
                            error = null
                        )
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .catch {
                    debugLog { "fetch order Service error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
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
        val serviceOrderFormFieldUIListMLD: List<ServiceOrderFormFieldUI> = listOf(),
        val successMessageMLD: String = "",

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