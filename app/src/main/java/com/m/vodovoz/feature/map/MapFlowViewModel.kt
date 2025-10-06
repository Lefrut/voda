package com.m.vodovoz.feature.map

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.design_system.model.ImageButtonUi
import com.m.vodovoz.design_system.model.MapPointUi
import com.m.vodovoz.design_system.model.contains
import com.m.vodovoz.design_system.model.distanceKm
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toDomain
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.domain.general.model.location.MapAreaModel
import com.m.vodovoz.domain.general.respository.MapServiceRepository
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.map.model.MapAddressUi
import com.m.vodovoz.feature.map.model.MapAreaUi
import com.m.vodovoz.feature.map.model.MapPopupWindowUi
import com.m.vodovoz.feature.map.model.findNearestPointTo
import com.m.vodovoz.feature.map.model.mapToUi
import com.m.vodovoz.feature.map.model.toUi
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import com.m.vodovoz.util.extensions.debounceWithMax
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class MapFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val mapServiceRepository: MapServiceRepository,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<MapFlowViewModel.MapFlowState, MapFlowViewModel.MapFlowEvents>(
    MapFlowState()
) {

    private val addressName = savedState.get<String>("addressName")?.apply {
        updateState { s -> s.copy(screenType = MapScreenTypeUi.Edit) }
    }

    private val searchQueryFlow = MutableStateFlow(stateSnapshot.query)
    private val activeSearchJobs = mutableListOf<Job>()

    init {
        handleSearchQueries()
        fetchMapAreas()
    }

    private fun fetchMapAreas() = viewModelScope.launch {
        val maxErrors = 10

        vodovozServiceRepository.getMapAreas()
            .onEach { result ->
                result.onFailure { fail -> throw fail }
            }
            .retryWhen { _, attempt ->
                if (attempt < maxErrors) true.also { delay(1000L) }
                else false
            }
            .catch { fail -> emit(Result.failure(fail)) }
            .collect { mapAreasResult ->
                mapAreasResult.onSuccess { mapZones ->
                    updateState { s ->
                        s.copy(
                            areas = mapZones.areas.mapToUi(),
                            deliveryButton = mapZones.imageButton?.toUi(),
                            deliveryPopupWindow = mapZones.popupWindow?.toUi()
                        )
                    }
                }

            }
    }

    fun navigateBack() = viewModelScope.launch {
        when (stateSnapshot.mode) {
            MapUiMode.OnlyMap -> {
                sendEvent(MapFlowEvents.GoBack)
            }

            MapUiMode.Search -> {
                sendEvent(MapFlowEvents.HideKeyboard)
                updateState { s ->
                    s.copy(mode = MapUiMode.OnlyMap)
                }
            }
        }
    }

    fun plusZoom() = viewModelScope.launch {
        sendEvent(MapFlowEvents.MoveCameraPlus)
    }

    fun minusZoom() = viewModelScope.launch {
        sendEvent(MapFlowEvents.MoveCameraMinus)
    }

    fun checkGeo() = viewModelScope.launch {
        sendEvent(MapFlowEvents.CheckGeo)
    }

    private fun handleSearchQueries() = searchQueryFlow.filter { query ->
        query.isNotBlank()
    }.debounceWithMax(250L, 6).onEach { query ->
        val addressesInMoscowByQueryResult =
            mapServiceRepository.getAddressesInMoscowByQuery(query).singleResult()


        addressesInMoscowByQueryResult.onSuccess { addresses ->
            updateState { s ->
                s.copy(
                    recommendedAddresses = addresses.ifEmpty { s.recommendedAddresses }
                )
            }
        }
    }.launchIn(viewModelScope)


    fun changeQuery(query: String) = viewModelScope.launch {
        searchQueryFlow.emit(query)
        updateState { s ->
            s.copy(query = query)
        }
    }

    fun searchAddressByQuery() {
        searchAddress(stateSnapshot.query)
    }

    private suspend fun getDistanceFromAreaBoundToAddress(addressPoint: MapPointUi): Float? {
        while (stateSnapshot.areas.isEmpty()) {
            delay(300L)
        }

        val coreMapArea = stateSnapshot.areas.find { area ->
            area.id == MapAreaModel.CORE_AREA_ID && area.isMoscowRingRow
        } ?: run { return null }

        if (coreMapArea.contains(addressPoint)) {
            return 0f
        }

        val route = coreMapArea
            .findNearestPointTo(addressPoint)
            ?.routeTo(addressPoint) ?: return null

        val fromMoscowToPoint = route.distanceKm()

        return fromMoscowToPoint
    }

    fun moveToAvailableGeo() = viewModelScope.launch {
        val addressPoint = stateSnapshot.currentMapAddress?.point
        if (addressPoint != null) {
            sendEvent(MapFlowEvents.MoveToAddress(addressPoint))
        } else if (addressName != null) {
            searchAddress(addressName)
        } else {
            sendEvent(MapFlowEvents.MoveToGeoOrMoscow)
        }
    }

    fun moveToUserGeo() = viewModelScope.launch {
        sendEvent(MapFlowEvents.MoveToGeoOrMoscow)
    }

    fun showSettingDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showSettingsDialog = true)
        }
    }

    fun closeSettingsDialog() = viewModelScope.launch {
        updateState { s ->
            s.copy(showSettingsDialog = false)
        }
    }

    fun showAddressBottomSheet() = viewModelScope.launch {
        sendEvent(MapFlowEvents.ShowAddressBottomSheet)
    }

    fun hideAddressBottomSheet() = viewModelScope.launch {
        sendEvent(MapFlowEvents.HideAddressBottomSheet)
    }

    fun searchAddress(addressName: String) = searchThenUpdateAddress(
        datasource = {
            mapServiceRepository.searchAddressInMoscow(addressName)
        },
        validation = {
            addressName == stateSnapshot.currentMapAddress?.name
                    || addressName.any { c -> c.isDigit() }
                    || stateSnapshot.query == addressName
        },
        invalid = {
            changeQuery(addressName)
        },
        successful = { mapAddress ->
            sendEvent(MapFlowEvents.HideKeyboard)
            sendEvent(MapFlowEvents.MoveToAddress(mapAddress.point))
        }
    )

    fun searchAddress(point: MapPointUi?) = searchThenUpdateAddress(
        datasource = {
            delay(350L)
            point?.let {
                mapServiceRepository.getAddressByGeo(point.lat, point.lon)
            } ?: emptyFlow()
        },
        validation = {
            point != stateSnapshot.currentMapAddress?.point && point != null
        },
    )

    private fun searchThenUpdateAddress(
        datasource: suspend () -> Flow<Result<MapAddressModel>>,
        validation: () -> Boolean = { true },
        invalid: () -> Unit = {},
        successful: suspend (MapAddressUi) -> Unit = {},
    ): Job = viewModelScope.launch {
        val activeJobsSnapshot = activeSearchJobs.toList()
        val newSearchJob = launch childLaunch@{
            if (!validation()) {
                invalid().also { return@childLaunch }
            } else {
                activeJobsSnapshot.forEach { job -> job.cancel() }
            }

            updateState { s ->
                s.copy(addressIsLoading = true)
            }

            val currentAddressResult = datasource()
                .singleResult()
                .map { model -> model.toUi() }

            currentAddressResult.onSuccess { mapAddress ->
                val fromMoscowToPoint = getDistanceFromAreaBoundToAddress(
                    mapAddress.point
                ) ?: return@childLaunch
                val updatedMapAddress = mapAddress.copy(
                    fromMoscowToPoint = fromMoscowToPoint
                )
                updateState { s ->
                    s.copy(
                        currentMapAddress = updatedMapAddress,
                        mode = MapUiMode.OnlyMap,
                        addressIsLoading = false,
                        addressIsError = with(updatedMapAddress) { house.isBlank() }
                    )
                }
                successful(updatedMapAddress)
            }
        }
        activeSearchJobs.add(newSearchJob)
        newSearchJob.join()
    }

    private suspend fun MapPointUi.routeTo(end: MapPointUi): List<MapPointUi>? {
        return mapServiceRepository.getRoute(
            start = this.toDomain(),
            end = end.toDomain()
        ).singleResult().getOrNull()?.mapToUi()
    }

    fun changeToSearchMode() {
        updateState { s ->
            s.copy(mode = MapUiMode.Search)
        }
    }

    fun navigateToLocationSettings() = viewModelScope.launch {
        updateState { s ->
            s.copy(showSettingsDialog = false)
        }
        sendEvent(MapFlowEvents.GoToLocationSettings)
    }

    fun navigateToAddAddress() = viewModelScope.launch {
        val mapAddress = stateSnapshot.currentMapAddress

        if (
            stateSnapshot.addressIsLoading || stateSnapshot.addressIsError
            || stateSnapshot.buttonIsLoading
            || mapAddress == null
        ) return@launch

        updateState { s ->
            s.copy(buttonIsLoading = true)
        }

        val screenType = stateSnapshot.screenType

        when (screenType) {
            MapScreenTypeUi.Add -> {
                sendEvent(MapFlowEvents.GoToAddAddress(mapAddress))
            }

            MapScreenTypeUi.Edit -> {
                sendEvent(MapFlowEvents.BackToAddAddress(mapAddress))
            }
        }

        updateState { s ->
            s.copy(buttonIsLoading = false)
        }

    }

    fun showDeliveryBottomSheet() {
        updateState { s ->
            s.copy(showDeliveryBS = true)
        }
    }

    fun closeDeliveryBottomSheet() {
        updateState { s ->
            s.copy(showDeliveryBS = false)
        }
    }


    @Immutable
    data class MapFlowState(
        val areas: List<MapAreaUi> = emptyList(),
        val deliveryButton: ImageButtonUi? = null,
        val deliveryPopupWindow: MapPopupWindowUi? = null,
        val showDeliveryBS: Boolean = false,
        val query: String = "",
        val showSettingsDialog: Boolean = false,
        val currentMapAddress: MapAddressUi? = null,
        val addressIsLoading: Boolean = true,
        val addressIsError: Boolean = false,
        val buttonIsLoading: Boolean = false,
        val mode: MapUiMode = MapUiMode.OnlyMap,
        val recommendedAddresses: List<String> = emptyList(),
        val screenType: MapScreenTypeUi = MapScreenTypeUi.Add,
    ) : State


    @Stable
    enum class MapScreenTypeUi {
        Add, Edit;
    }

    @Stable
    sealed interface MapUiMode {
        data object OnlyMap : MapUiMode
        data object Search : MapUiMode
    }

    sealed class MapFlowEvents : Event {

        data object MoveToGeoOrMoscow : MapFlowEvents()
        data object CheckGeo : MapFlowEvents()
        data object MoveCameraMinus : MapFlowEvents()
        data object MoveCameraPlus : MapFlowEvents()
        data object ShowAddressBottomSheet : MapFlowEvents()
        data object HideAddressBottomSheet : MapFlowEvents()
        data object GoBack : MapFlowEvents()
        data object HideKeyboard : MapFlowEvents()
        data object GoToLocationSettings : MapFlowEvents()
        data class GoToAddAddress(val mapAddress: MapAddressUi) : MapFlowEvents()
        data class BackToAddAddress(val mapAddress: MapAddressUi) : MapFlowEvents()

        data class MoveToAddress(val addressPoint: MapPointUi) : MapFlowEvents()
    }
}