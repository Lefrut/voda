package com.m.vodovoz.feature.addresses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.design_system.model.MapPointUi
import com.m.vodovoz.design_system.model.SectionUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.design_system.model.contains
import com.m.vodovoz.design_system.model.distanceKm
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toDomain
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.mapToUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.domain.general.respository.MapServiceRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.addresses.api.AddressesNavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import com.m.vodovoz.feature.addresses.model.AddressUi
import com.m.vodovoz.feature.addresses.model.mapToUi
import com.m.vodovoz.feature.map.model.MapAreaUi
import com.m.vodovoz.feature.map.model.findNearestPointsTo
import com.m.vodovoz.feature.map.model.mapToUi
import com.m.vodovoz.feature.map.model.toDomain
import com.m.vodovoz.feature.map.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.util.extensions.onEachFailure
import com.m.vodovoz.util.extensions.onEachSuccess
import com.m.vodovoz.util.extensions.singleGetOrNull
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlin.math.floor

@HiltViewModel(assistedFactory = AddressesFlowViewModel.Factory::class)
@Stable
class AddressesFlowViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val mapServiceRepository: MapServiceRepository,
    @Assisted private val navKey: AddressesNavKey,
) : MviViewModel<AddressesFlowViewModel.AddressesState, AddressesFlowViewModel.AddressesEvents>(
    AddressesState(
        screenType = navKey.screenType
    )
) {

    private val selectedAddressId = navKey.addressId

    init {
        fetchMapAreas()
    }

    private fun fetchMapAreas() =
        vodovozServiceRepository.getMapAreas().onEachFailure { throwable ->
            throw throwable
        }.onEachSuccess { mapZonesModel ->
            updateState { s ->
                s.copy(mapAreas = mapZonesModel.areas.mapToUi())
            }
        }.retry {
            delay(350)
            true
        }.launchIn(viewModelScope)

    fun fetchAddresses() = viewModelScope.launch {
        val addressesResult = vodovozServiceRepository.getAddresses().singleResult()

        addressesResult.onSuccess { sections ->

            val addressSections = sections.map { section ->
                section.toUi { addressModelList ->
                    addressModelList.mapToUi()
                }
            }

            val selectedAddress = addressSections.flatMap { it.items }.find {
                it.id == selectedAddressId || it.id == stateSnapshot.selectedAddress.id
            } ?: addressSections.firstOrNull()?.items?.firstOrNull() ?: AddressUi.Empty


            updateState { s ->
                s.copy(
                    addressSections = addressSections,
                    selectedAddress = selectedAddress,
                    uiState = AddressesUiState.Success
                )
            }
        }.onFailure { t ->
            val uiState = if (t is EmptyResultException && t.placeholder != null) {
                AddressesUiState.Empty(t.placeholder.toUi())
            } else AddressesUiState.Error

            updateState { s ->
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
        sendEvent(AddressesEvents.GoBack(stateSnapshot.selectedAddress))
    }

    fun addAddress() = viewModelScope.launch {
        sendEvent(AddressesEvents.GoToMap)
    }

    fun searchThenNavigateToOrdering() = viewModelScope.launch {
        updateState { s ->
            s.copy(buttonLoading = true)
        }

        val selectedAddress = stateSnapshot.selectedAddress
        val addressDetails = vodovozServiceRepository.getAddAddressDetails(
            addressId = selectedAddress.id
        ).singleGetOrNull() ?: return@launch
        val fromMoscowRingToAddress = addressDetails.formMoscowRingToAddressKm

        if (fromMoscowRingToAddress != null) {
            sendEvent(AddressesEvents.GoBackToOrdering(selectedAddress))
            return@launch
        }

        val mapAddress = mapServiceRepository.searchAddressInMoscow(
            address = selectedAddress.address
        ).singleGetOrNull()?.toUi() ?: return@launch
        val addressPoint = mapAddress.point

        val mapAreas = stateSnapshot.mapAreas.ifEmpty {
            return@launch
        }
        val coreMapArea = mapAreas.find { area ->
            area.id == MapAreaModel.CORE_AREA_ID && area.isMoscowRingRow
        } ?: return@launch

        val updatedFromMoscowRingToAddress = if (coreMapArea.contains(addressPoint)) {
            0f
        } else {
            val nearestPoints = coreMapArea.findNearestPointsTo(target = addressPoint, count = 5)
            val routes = nearestPoints.map { nearestPoint ->
                async { nearestPoint.routeTo(addressPoint) }
            }.awaitAll()
            val fromMoscowToPoint = routes.minOf { route ->
                route?.distanceKm() ?: Float.MAX_VALUE
            }
            fromMoscowToPoint
        }

        val addressParams = with(addressDetails) {
            linearSwitches.mapToUi().associate { w ->
                w.id to VodovozBoolean.from(w.value()).boolean.toString()
            } + linearFields.mapToUi().associate { w ->
                w.id to w.value()
            } + gridFields.mapToUi().associate { w ->
                w.id to w.value()
            } + with(addressField.toUi()) {
                id to mapAddress.name
            } + mapOf(label.id to label.name)
        }

        vodovozServiceRepository.updateAddress(
            addressId = selectedAddress.id,
            address = mapAddress.copy(
                fromMoscowToPoint = floor(updatedFromMoscowRingToAddress).toInt()
            ).toDomain(),
            params = addressParams
        ).singleResult().onSuccess {
            sendEvent(AddressesEvents.GoBackToOrdering(selectedAddress))
        }

    }.invokeOnCompletion {
        updateState { s -> s.copy(buttonLoading = false) }
    }

    private suspend fun MapPointUi.routeTo(end: MapPointUi): List<MapPointUi>? {
        return mapServiceRepository.getRoutes(
            start = this.toDomain(),
            end = end.toDomain()
        ).singleGetOrNull()?.mapToUi()
    }


    fun editAddress(address: AddressUi) = viewModelScope.launch {
        sendEvent(AddressesEvents.GoToEditAddress(address.id, address.address))
    }

    fun selectAddress(address: AddressUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(selectedAddress = address)
        }
    }

    fun showRemoveAddressDialog(address: AddressUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentRemoveAddress = address,
                showRemoveAddressDialog = true
            )
        }
    }

    fun hideRemoveAddressDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentRemoveAddress = null,
                showRemoveAddressDialog = false
            )
        }
    }

    fun removeAddress(currentRemoveAddress: AddressUi) = viewModelScope.launch {
        updateState { state ->
            state.copy(
                addressSections = state.addressSections.map { section ->
                    section.copy(items = section.items.filter { it.id != currentRemoveAddress.id })
                },
                showRemoveAddressDialog = false,
                currentRemoveAddress = null,
                selectedAddress = AddressUi.Empty
            )
        }
        vodovozServiceRepository.removeAddress(currentRemoveAddress.id.toInt()).singleResult()
        refresh().join()
    }

    fun refresh() = viewModelScope.launch {
        updateState {
            it.copy(
                showRefreshIndicator = true,
                buttonEnabled = false
            )
        }

        fetchAddresses().join()

        updateState {
            it.copy(
                showRefreshIndicator = false,
                buttonEnabled = true
            )
        }
    }

    sealed class AddressesEvents : Event {
        data class GoBack(val address: AddressUi) : AddressesEvents()
        data object GoToMap : AddressesEvents()
        data class GoToEditAddress(val addressId: Long, val addressName: String) : AddressesEvents()
        data class GoBackToOrdering(val address: AddressUi) : AddressesEvents()
    }

    @Immutable
    data class AddressesState(
        val screenType: AddressScreenTypeUi,
        val addressSections: List<SectionUi<AddressUi>> = emptyList(),
        val selectedAddress: AddressUi = AddressUi.Empty,
        val uiState: AddressesUiState = AddressesUiState.Loading,
        val showRemoveAddressDialog: Boolean = false,
        val currentRemoveAddress: AddressUi? = null,
        val showRefreshIndicator: Boolean = false,
        val mapAreas: List<MapAreaUi> = emptyList(),
        val buttonLoading: Boolean = false,
        val buttonEnabled: Boolean = false,
    ) : State {
    }

    @Stable
    sealed interface AddressesUiState {
        data object Loading : AddressesUiState
        data object Error : AddressesUiState
        data object Success : AddressesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AddressesUiState
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: AddressesNavKey): AddressesFlowViewModel
    }
}
