package com.m.vodovoz.feature.addresses

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import com.m.vodovoz.util.extensions.onFailure
import com.m.vodovoz.util.extensions.onSuccess
import com.m.vodovoz.util.extensions.singleGetOrNull
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class AddressesFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val mapServiceRepository: MapServiceRepository,
) : MviViewModel<AddressesFlowViewModel.AddressesState, AddressesFlowViewModel.AddressesEvents>(
    AddressesState(
        screenType = savedState.get<AddressScreenTypeUi>("screenType") ?: AddressScreenTypeUi.Add
    )
) {

    private val selectedAddressId = savedState.get<Long>("addressId")

    init {
        fetchMapAreas()
    }

    private fun fetchMapAreas() = vodovozServiceRepository.getMapAreas().onFailure { throwable ->
        throw throwable
    }.onSuccess { mapZonesModel ->
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
                it.id == selectedAddressId
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
        sendEvent(AddressesEvents.GoBack)
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
            selectedAddress.id
        ).singleGetOrNull() ?: return@launch

        val mapAddress = mapServiceRepository.searchAddressInMoscow(
            selectedAddress.address
        ).singleGetOrNull()?.toUi() ?: return@launch
        val addressPoint = mapAddress.point

        val mapAreas = stateSnapshot.mapAreas.ifEmpty {
            return@launch
        }
        val coreMapArea = mapAreas.find { area ->
            area.id == MapAreaModel.CORE_AREA_ID && area.isMoscowRingRow
        } ?: return@launch

        val fromMoscowToPoint = if (coreMapArea.contains(addressPoint)) {
            0f
        } else {
            val nearestPoints = coreMapArea.findNearestPointsTo(addressPoint)
            val routes = nearestPoints.mapNotNull { nearestPoint ->
                addressPoint.routeTo(nearestPoint)
            }
            val fromMoscowToPoint = routes.minOf { route -> route.distanceKm() }
            fromMoscowToPoint
        }

        val addressParams = with(addressDetails) {
            linearSwitches.mapToUi().associate { w ->
                w.id to VodovozBoolean.from(w.value()).boolean.toString()
            } + linearFields.mapToUi().associate { w ->
                w.id to w.value()
            } + gridFields.mapToUi().associate { w ->
                w.id to w.value()
            } + with(addressField.toUi()) { id to mapAddress.name }
        }

        vodovozServiceRepository.updateAddress(
            addressId = selectedAddress.id,
            address = mapAddress.copy(fromMoscowToPoint = fromMoscowToPoint.roundToInt())
                .toDomain(),
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
                currentRemoveAddress = null
            )
        }
        vodovozServiceRepository.removeAddress(currentRemoveAddress.id.toInt()).singleResult()
        refresh()
    }

    fun refresh() = viewModelScope.launch {
        updateState {
            it.copy(showRefreshIndicator = true)
        }

        fetchAddresses().join()

        updateState {
            it.copy(showRefreshIndicator = false)
        }
    }

    sealed class AddressesEvents : Event {
        data object GoBack : AddressesEvents()
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
    ) : State {
    }

    @Stable
    sealed interface AddressesUiState {
        data object Loading : AddressesUiState
        data object Error : AddressesUiState
        data object Success : AddressesUiState
        data class Empty(val placeholder: VodovozPlaceholderUi) : AddressesUiState
    }
}