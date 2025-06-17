package com.vodovoz.app.feature.addresses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.SectionUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.model.AddressScreenTypeUi
import com.vodovoz.app.feature.addresses.model.AddressUi
import com.vodovoz.app.feature.addresses.model.mapToUi
import com.vodovoz.app.ui.model.AddressFlowTitle
import com.vodovoz.app.ui.model.AddressUI
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AddressesFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<AddressesFlowViewModel.AddressesState, AddressesFlowViewModel.AddressesEvents>(
    AddressesState(
        screenType = savedState.get<AddressScreenTypeUi>("screenType") ?: AddressScreenTypeUi.Add
    )
) {

    init {
        fetchAddresses()
    }

    fun fetchAddresses() = viewModelScope.launch {
        val addressesResult = vodovozServiceRepository.getAddresses().singleResult()

        addressesResult.onSuccess { sections ->

            val addressSections = sections.map { section ->
                section.toUi { addressModelList ->
                    addressModelList.mapToUi()
                }
            }

            uiStateListener.updateData { s ->
                s.copy(
                    addressSections = addressSections,
                    selectedAddress = addressSections.firstOrNull()?.items?.firstOrNull()
                        ?: AddressUi.Empty,
                    uiState = AddressesUiState.Success
                )
            }
        }.onFailure { t ->
            val uiState = if (t is EmptyResultException && t.placeholder != null) {
                AddressesUiState.Empty(t.placeholder.toUi())
            } else AddressesUiState.Error

            uiStateListener.updateData { s ->
                s.copy(
                    uiState = if (s.uiState != AddressesUiState.Success || uiState is AddressesUiState.Empty) {
                        uiState
                    } else {
                        s.uiState
                    }
                )
            }
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(AddressesEvents.GoBack)
    }

    fun addAddress() = viewModelScope.launch {
        eventListener.emit(AddressesEvents.GoToMap(null))
    }

    fun navigateToOrdering() = viewModelScope.launch {

    }

    fun editAddress(address: AddressUi) = viewModelScope.launch {
        eventListener.emit(AddressesEvents.GoToMap(address))
    }

    fun selectAddress(address: AddressUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(selectedAddress = address)
        }
    }

    fun showRemoveAddressDialog(address: AddressUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentRemoveAddress = address,
                showRemoveAddressDialog = true
            )
        }
    }

    fun hideRemoveAddressDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentRemoveAddress = null,
                showRemoveAddressDialog = false
            )
        }
    }

    fun removeAddress(currentRemoveAddress: AddressUi) = viewModelScope.launch {
        uiStateListener.updateData { state ->
            state.copy(
                addressSections = state.addressSections.map { section ->
                    section.copy(items = section.items.filter { it.id != currentRemoveAddress.id })
                },
                showRemoveAddressDialog = false,
                currentRemoveAddress = null
            )
        }
        vodovozServiceRepository.removeAddress(currentRemoveAddress.id.toInt()).singleResult()
    }

    sealed class AddressesEvents : Event {
        data object GoBack : AddressesEvents()
        data class GoToMap(val address: AddressUi?) : AddressesEvents()
    }

    @Immutable
    data class AddressesState(
        val screenType: AddressScreenTypeUi,
        val addressSections: List<SectionUi<AddressUi>> = emptyList(),
        val selectedAddress: AddressUi = AddressUi.Empty,
        val uiState: AddressesUiState = AddressesUiState.Loading,
        val showRemoveAddressDialog: Boolean = false,
        val currentRemoveAddress: AddressUi? = null,
    ) : State

    @Stable
    sealed interface AddressesUiState {
        data object Loading : AddressesUiState
        data object Error : AddressesUiState
        data object Success : AddressesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AddressesUiState
    }
}