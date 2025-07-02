package com.vodovoz.app.feature.map

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.contains
import com.vodovoz.app.design_system.model.distanceKm
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toDomain
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.map.model.MapAddressUi
import com.vodovoz.app.feature.map.model.MapAreaUi
import com.vodovoz.app.feature.map.model.findNearestPointTo
import com.vodovoz.app.feature.map.model.mapToUi
import com.vodovoz.app.feature.map.model.toUi
import com.vodovoz.app.util.extensions.debounceWithMax
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
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
) : PagingContractViewModel<MapFlowViewModel.MapFlowState, MapFlowViewModel.MapFlowEvents>(
    MapFlowState()
) {

    private val addressName = savedState.get<String>("addressName")?.apply {
        uiStateListener.updateData { s -> s.copy(screenType = MapScreenTypeUi.Edit) }
    }

    private val searchQueryFlow = MutableStateFlow(dataState.query)

    companion object {
        const val CORE_AREA_ID = 91851
    }

    init {
        handleSearchQueries()
        fetchMapAreas()
    }

    private fun fetchMapAreas() = viewModelScope.launch {
        val maxErrors = 5

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
                mapAreasResult.onSuccess { mapAreas ->

                    uiStateListener.updateData { s ->
                        s.copy(areas = mapAreas.mapToUi())
                    }

                }
            }
    }

    fun navigateBack() = viewModelScope.launch {
        when (dataState.mode) {
            MapUiMode.OnlyMap -> {
                eventListener.emit(MapFlowEvents.GoBack)
            }

            MapUiMode.Search -> {
                eventListener.emit(MapFlowEvents.HideKeyboard)
                uiStateListener.updateData { s ->
                    s.copy(mode = MapUiMode.OnlyMap)
                }
            }
        }
    }

    fun plusZoom() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.MoveCameraPlus)
    }

    fun minusZoom() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.MoveCameraMinus)
    }

    fun checkGeo() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.CheckGeo)
    }

    private fun handleSearchQueries() =
        searchQueryFlow.filter { query -> query.isNotBlank() }.debounceWithMax(250L, 6)
            .onEach { query ->
                val addressesInMoscowByQueryResult =
                    mapServiceRepository.getAddressesInMoscowByQuery(query).singleResult()


                addressesInMoscowByQueryResult.onSuccess { addresses ->
                    uiStateListener.updateData { s ->
                        s.copy(recommendedAddresses = addresses)
                    }
                }
            }.launchIn(viewModelScope)


    fun changeQuery(query: String) {
        viewModelScope.launch {
            searchQueryFlow.emit(query)
            uiStateListener.updateData { s ->
                s.copy(query = query)
            }
        }
    }

    fun searchAddressByQuery() {
        searchAddress(dataState.query)
    }


    private suspend fun calculateDistanceFromMoscowToAddress(addressPoint: MapPointUi): Float? {
        val coreMapArea =
            dataState.areas.find { area -> area.id == CORE_AREA_ID && area.isMoscowRingRow }
                ?: return null

        if(coreMapArea.contains(addressPoint)){ return 0f }

        val nearestPoint = coreMapArea.findNearestPointTo(addressPoint) ?: return null
        val route = nearestPoint.getRoute(addressPoint) ?: return null
        val fromMoscowToPoint = route.distanceKm()
        return fromMoscowToPoint
    }

    fun searchAddress(addressName: String) = viewModelScope.launch {
        if (addressName == dataState.currentMapAddress?.name || addressName.any { c -> c.isDigit() } || dataState.query == addressName) {

            uiStateListener.updateData { s ->
                s.copy(addressIsLoading = true)
            }

            val currentAddress = mapServiceRepository.searchAddressInMoscow(
                addressName
            ).singleResult().getOrNull()?.toUi()

            currentAddress?.let { mapAddress ->

                val fromMoscowToPoint =
                    calculateDistanceFromMoscowToAddress(mapAddress.point) ?: return@launch

                changeAddress(currentAddress.copy(fromMoscowToPoint = fromMoscowToPoint))
                eventListener.emit(MapFlowEvents.HideKeyboard)
                eventListener.emit(MapFlowEvents.MoveToAddress(currentAddress.point))
            }

        } else {
            changeQuery(addressName)
        }
    }


    fun moveToAvailableGeo() = viewModelScope.launch {
        val addressPoint = dataState.currentMapAddress?.point
        if (addressPoint != null) {
            eventListener.emit(MapFlowEvents.MoveToAddress(addressPoint))
        } else if (addressName != null) {
            searchAddress(addressName)
        } else {
            eventListener.emit(MapFlowEvents.MoveToGeoOrMoscow)
        }
    }

    fun moveToUserGeo() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.MoveToGeoOrMoscow)
    }

    fun showSettingDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingsDialog = true)
        }
    }

    fun closeSettingsDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingsDialog = false)
        }
    }

    fun showAddressBottomSheet() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.ShowAddressBottomSheet)
    }

    fun hideAddressBottomSheet() = viewModelScope.launch {
        eventListener.emit(MapFlowEvents.HideAddressBottomSheet)
    }

    private var searchAddressJob: Job? = null

    fun searchAddress(point: MapPointUi?) = viewModelScope.launch {
        if (point == null || point == dataState.currentMapAddress?.point) return@launch

        searchAddressJob?.cancel()

        searchAddressJob = launch job@{
            uiStateListener.updateData { s ->
                s.copy(addressIsLoading = true)
            }

            delay(350L)

            val addressByGeoResult =
                mapServiceRepository.getAddressByGeo(point.lat, point.lon).singleResult()



            addressByGeoResult.onSuccess { mapAddressModel ->
                val mapAddress = mapAddressModel.toUi()

                val fromMoscowToPoint =
                    calculateDistanceFromMoscowToAddress(mapAddress.point) ?: return@job

                changeAddress(mapAddress.copy(fromMoscowToPoint = fromMoscowToPoint))
            }
        }
    }

    private suspend fun MapPointUi.getRoute(end: MapPointUi): List<MapPointUi>? {
        return mapServiceRepository.getRoute(toDomain(), end.toDomain()).singleResult().getOrNull()
            ?.mapToUi()
    }

    private fun changeAddress(address: MapAddressUi) {
        uiStateListener.updateData { s ->
            s.copy(
                currentMapAddress = address,
                mode = MapUiMode.OnlyMap,
                addressIsLoading = false,
                addressIsError = with(address) { house.isBlank() }
            )
        }

    }

    fun changeToSearchMode() {
        uiStateListener.updateData { s ->
            s.copy(mode = MapUiMode.Search)
        }
    }

    fun navigateToLocationSettings() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingsDialog = false)
        }
        eventListener.emit(MapFlowEvents.GoToLocationSettings)
    }

    fun navigateToAddAddress() = viewModelScope.launch {
        if (dataState.addressIsLoading || dataState.addressIsError || dataState.buttonIsLoading) return@launch

        uiStateListener.updateData { s ->
            s.copy(buttonIsLoading = true)
        }

        val mapAddress = dataState.currentMapAddress
        val screenType = dataState.screenType

        when {
            screenType == MapScreenTypeUi.Add && mapAddress != null -> {
                eventListener.emit(MapFlowEvents.GoToAddAddress(mapAddress))

            }

            screenType == MapScreenTypeUi.Edit && mapAddress != null -> {
                eventListener.emit(MapFlowEvents.BackToAddAddress(mapAddress))
            }
        }

        uiStateListener.updateData { s ->
            s.copy(buttonIsLoading = false)
        }

    }

    @Immutable
    data class MapFlowState(
        val areas: List<MapAreaUi> = emptyList(),
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
        Add, Edit
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