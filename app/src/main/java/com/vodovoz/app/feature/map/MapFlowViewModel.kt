package com.vodovoz.app.feature.map

import android.os.CountDownTimer
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
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.model.AddressUi
import com.vodovoz.app.feature.map.manager.DeliveryZonesManager
import com.vodovoz.app.feature.map.model.MapAddressUi
import com.vodovoz.app.feature.map.model.toDomain
import com.vodovoz.app.feature.map.model.toUi
import com.vodovoz.app.mapper.AddressMapper.mapToUI
import com.vodovoz.app.ui.model.AddressUI
import com.vodovoz.app.ui.model.custom.DeliveryZonesBundleUI
import com.vodovoz.app.util.extensions.debounceWithMax
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class MapFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: MainRepository,
    private val deliveryZonesManager: DeliveryZonesManager,
    private val accountManager: AccountManager,
    private val mapServiceRepository: MapServiceRepository,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<MapFlowViewModel.MapFlowState, MapFlowViewModel.MapFlowEvents>(
    MapFlowState()
) {

    private val selectedAddress = savedState.get<AddressUi>("address")?.apply {
        uiStateListener.updateData { s ->
            s.copy(screenType = MapScreenTypeUi.Edit)
        }
    }

    private val searchQueryFlow = MutableStateFlow(dataState.query)

    init {
        viewModelScope.launch {
            deliveryZonesManager
                .observeDeliveryZonesState()
                .collect { deliveryState ->
                    if (deliveryState == null) {
                        deliveryZonesManager.fetchDeliveryZonesBundle()
                    } else {
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                deliveryZonesBundleUI = deliveryState.deliveryZonesBundleUI
                            )
                        )
                    }
                }
        }
        handleSearchQueries()
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

    fun updateZones(bool: Boolean) {
        uiStateListener.value = state.copy(
            data = state.data.copy(
                updateZones = bool
            )
        )
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

    fun fetchAddressByGeocode(
        latitude: Double,
        longitude: Double,
    ) {
        uiStateListener.value = state.copy(loadingPage = true)

        viewModelScope.launch {
            flow { emit(repository.fetchAddressByGeocodeResponse(latitude, longitude)) }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI().copy(id = state.data.addressUI?.id ?: 0)
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                addressUI = data
                            ),
                            error = null,
                            loadingPage = false
                        )

                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "fetch address by geocode error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun showAddAddressBottomDialog() {
        viewModelScope.launch {
            val mappedAddress = state.data.addressUI?.copy(
                latitude = state.data.savedPointData?.latitude ?: "",
                longitude = state.data.savedPointData?.longitude ?: "",
                length = state.data.savedPointData?.length ?: ""
            )
            eventListener.emit(MapFlowEvents.ShowAddAddressBottomDialog(mappedAddress))
        }
    }

    fun showInfoDialog() {
        viewModelScope.launch {
            eventListener.emit(MapFlowEvents.ShowInfoDialog(state.data.deliveryZonesBundleUI?.aboutDeliveryTimeUrl))
        }
    }

    fun fetchSeveralMinimalLineDistancesToMainPolygonPoints(
        startPoint: Point,
        pendingUpdateAddressUI: AddressUI? = null,
    ) {
        viewModelScope.launch {
            val listOnPoints =
                deliveryZonesManager.fetchSeveralMinimalLineDistancesToMainPolygonPoints(startPoint)
            uiStateListener.value = state.copy(
                data = state.data.copy(
                    pendingUpdateAddressUI = pendingUpdateAddressUI,
                    listOnPoints = listOnPoints
                )
            )
            eventListener.emit(MapFlowEvents.Submit(startPoint, listOnPoints))
        }
    }

    private fun savePointData(
        latitude: String,
        longitude: String,
        length: String,
        distance: Double,
    ) {
        val mappedAddress = state.data.addressUI?.copy(
            latitude = latitude,
            longitude = longitude,
            length = length
        )
        uiStateListener.value = state.copy(

            data = state.data.copy(
                savedPointData = SavedPointData(latitude, longitude, length),
                distance = distance,
                addressUI = mappedAddress
            )
        )
    }

    private val amountControllerTimer = object : CountDownTimer(
        ApiConfig.AMOUNT_CONTROLLER_TIMER,
        ApiConfig.AMOUNT_CONTROLLER_TIMER
    ) {
        override fun onTick(millisUntilFinished: Long) {}
        override fun onFinish() {
            addPolyline()
        }
    }

    fun savePolyline(distance: Double, polyline: Polyline?, startPoint: Point, endPoint: Point) {

        uiStateListener.value = state.copy(
            data = state.data.copy(
                listOfSavedPolylinesData = state.data.listOfSavedPolylinesData + listOf(
                    SavedPolylineData(distance, polyline, startPoint, endPoint)
                )
            )
        )

        amountControllerTimer.cancel()
        amountControllerTimer.start()

        viewModelScope.launch {
            if (deliveryZonesManager.containsInCenterPolygon(endPoint)) {
                amountControllerTimer.cancel()
                savePointData(
                    latitude = endPoint.latitude.toString(),
                    longitude = endPoint.longitude.toString(),
                    length = "0",
                    distance = 0.0
                )
                tryToUpdate(
                    lat = endPoint.longitude.toString(),
                    long = endPoint.longitude.toString(),
                    length = "0",
                    polyline = null
                )
            } else {

                if (state.data.listOnPoints.size == state.data.listOfSavedPolylinesData.size) {
                    amountControllerTimer.cancel()
                    addPolyline()
                }

            }
        }

    }

    private fun tryToUpdate(lat: String, long: String, length: String, polyline: Polyline?) {
        viewModelScope.launch {
            val pendingUpdateAddressUi = state.data.pendingUpdateAddressUI
            if (pendingUpdateAddressUi == null) {
                eventListener.emit(MapFlowEvents.ShowPolyline(polyline = polyline))
            } else {
                val userId = accountManager.fetchAccountId() ?: return@launch
                updateAddress(
                    locality = pendingUpdateAddressUi.locality,
                    street = pendingUpdateAddressUi.street,
                    house = pendingUpdateAddressUi.house,
                    entrance = pendingUpdateAddressUi.entrance,
                    floor = pendingUpdateAddressUi.floor,
                    office = pendingUpdateAddressUi.flat,
                    intercom = pendingUpdateAddressUi.intercom,
                    type = pendingUpdateAddressUi.type,
                    userId = userId,
                    addressId = pendingUpdateAddressUi.id,
                    lat = lat,
                    longitude = long,
                    length = length,
                    fullAddress = pendingUpdateAddressUi.fullAddress
                )
            }
        }

    }

    internal fun addPolyline() {
        viewModelScope.launch {
            val minDistancePolyline =
                state.data.listOfSavedPolylinesData.filter { it.polyline != null }
                    .minByOrNull { it.distance }
            minDistancePolyline?.let {
                if (it.polyline == null) {
                    eventListener.emit(MapFlowEvents.ShowPolyline())
                    return@launch
                }
                val newDistance = (it.distance / 1000).roundToInt().toString()

                savePointData(
                    latitude = it.startPoint.latitude.toString(),
                    longitude = it.startPoint.longitude.toString(),
                    length = newDistance,
                    distance = it.distance
                )

                tryToUpdate(
                    polyline = it.polyline,
                    lat = it.startPoint.latitude.toString(),
                    long = it.startPoint.longitude.toString(),
                    length = newDistance
                )
            }
        }
    }

    fun clearState() {
        amountControllerTimer.cancel()
        uiStateListener.value = state.copy(
            data = state.data.copy(
                savedPointData = null,
                distance = null,
                listOnPoints = emptyList(),
                listOfSavedPolylinesData = emptyList()
            )
        )
    }

    fun action(
        entrance: String?,
        floor: String?,
        office: String?,
        intercom: String?,
        type: Int?,
    ) {
        val userId = accountManager.fetchAccountId() ?: return
        val addressId = state.data.addressUI?.id

        val locality = state.data.addressUI?.locality
        val street = state.data.addressUI?.street
        val house = state.data.addressUI?.house
        val lat = state.data.addressUI?.latitude
        val longitude = state.data.addressUI?.longitude
        val length = state.data.addressUI?.length
        val fullAddress = state.data.addressUI?.fullAddress?.substringAfter("Россия, ")

        if (house.isNullOrEmpty() || lat.isNullOrEmpty() || longitude.isNullOrEmpty() || length.isNullOrEmpty() || fullAddress.isNullOrEmpty()) {
            viewModelScope.launch {
                eventListener.emit(MapFlowEvents.ShowSearchError)
            }
            return
        }

        if (addressId == null || addressId == 0L) {
            addAddress(
                locality,
                street,
                house,
                entrance,
                floor,
                office,
                intercom,
                type,
                userId,
                lat,
                longitude,
                length,
                fullAddress
            )
        } else {
            updateAddress(
                locality,
                street,
                house,
                entrance,
                floor,
                office,
                intercom,
                type,
                userId,
                addressId,
                lat,
                longitude,
                length,
                fullAddress
            )
        }
    }

    private fun addAddress(
        locality: String?,
        street: String?,
        house: String?,
        entrance: String?,
        floor: String?,
        office: String?,
        intercom: String?,
        type: Int?,
        userId: Long,
        lat: String,
        longitude: String,
        length: String,
        fullAddress: String,
    ) {
        uiStateListener.value = state.copy(loadingPage = true)

        viewModelScope.launch {
            flow {
                emit(
                    repository.addAddress(
                        locality = locality,
                        street = street,
                        house = house,
                        entrance = entrance,
                        floor = floor,
                        office = office,
                        intercom = intercom,
                        type = type,
                        userId = userId,
                        lat = lat,
                        longitude = longitude,
                        length = length,
                        fullAddress = fullAddress
                    )
                )
            }
                .onEach { response ->
                    uiStateListener.value = state.copy(
                        loadingPage = false,
                        error = null
                    )
                    when (response) {
                        is ResponseEntity.Success -> eventListener.emit(MapFlowEvents.AddAddressSuccess)
                        is ResponseEntity.Error -> eventListener.emit(
                            MapFlowEvents.AddAddressError(
                                response.errorMessage
                            )
                        )

                        is ResponseEntity.Hide -> eventListener.emit(MapFlowEvents.AddAddressError("Неизвестная ошибка"))
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "add address error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    private fun updateAddress(
        locality: String?,
        street: String?,
        house: String?,
        entrance: String?,
        floor: String?,
        office: String?,
        intercom: String?,
        type: Int?,
        userId: Long,
        addressId: Long,
        lat: String,
        longitude: String,
        length: String,
        fullAddress: String,
    ) {
        uiStateListener.value = state.copy(loadingPage = true)

        viewModelScope.launch {
            flow {
                emit(
                    repository.updateAddress(
                        locality = locality,
                        street = street,
                        house = house,
                        entrance = entrance,
                        floor = floor,
                        office = office,
                        intercom = intercom,
                        type = type,
                        userId = userId,
                        addressId = addressId,
                        lat = lat,
                        longitude = longitude,
                        length = length,
                        fullAddress = fullAddress
                    )
                )
            }
                .onEach { response ->
                    uiStateListener.value = state.copy(
                        loadingPage = false,
                        error = null
                    )
                    when (response) {
                        is ResponseEntity.Success -> {
                            val pendingUpdateAddressUi = state.data.pendingUpdateAddressUI
                            if (pendingUpdateAddressUi == null) {
                                eventListener.emit(MapFlowEvents.AddAddressSuccess)
                            } else {
                                eventListener.emit(
                                    MapFlowEvents.UpdatePendingAddressUISuccess(
                                        pendingUpdateAddressUi
                                    )
                                )
                                uiStateListener.value = state.copy(
                                    data = state.data.copy(
                                        pendingUpdateAddressUI = null
                                    )
                                )
                            }
                        }

                        is ResponseEntity.Error -> eventListener.emit(
                            MapFlowEvents.AddAddressError(
                                response.errorMessage
                            )
                        )

                        is ResponseEntity.Hide -> eventListener.emit(MapFlowEvents.AddAddressError("Неизвестная ошибка"))
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "update address error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun searchAddressByQuery() {
        searchAddress(dataState.query)
    }

    fun searchAddress(addressName: String) = viewModelScope.launch {
        if (addressName == dataState.currentAddress?.name || addressName.any { c -> c.isDigit() } || dataState.query == addressName) {

            uiStateListener.updateData { s ->
                s.copy(addressIsLoading = true)
            }

            val currentAddress = mapServiceRepository.searchAddressInMoscow(
                addressName
            ).singleResult().getOrNull()?.toUi()

            currentAddress?.let {
                changeAddress(currentAddress)
                eventListener.emit(MapFlowEvents.HideKeyboard)
                eventListener.emit(MapFlowEvents.MoveToAddress(currentAddress.point))
            }

        } else {
            changeQuery(addressName)
        }
    }

    fun moveToAvailableGeo() = viewModelScope.launch {
        val addressPoint = dataState.currentAddress?.point
        if (addressPoint != null) {
            eventListener.emit(MapFlowEvents.MoveToAddress(addressPoint))
        } else if (selectedAddress != null) {
            searchAddress(selectedAddress.address)
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
        if (point == null || point == dataState.currentAddress?.point) return@launch

        searchAddressJob?.cancel()

        searchAddressJob = launch {
            uiStateListener.updateData { s ->
                s.copy(addressIsLoading = true)
            }

            delay(300L)

            val addressByGeoResult =
                mapServiceRepository.getAddressByGeo(point.lat, point.lon).singleResult()

            addressByGeoResult.onSuccess { address ->
                changeAddress(address.toUi())
            }
        }
    }

    private fun changeAddress(address: MapAddressUi) {
        uiStateListener.updateData { s ->
            s.copy(
                currentAddress = address,
                mode = MapUiMode.OnlyMap,
                addressIsLoading = false,
                addressIsError = with(address) { street.isBlank() || city.isBlank() || house.isBlank() }
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

        val addressId = selectedAddress?.id ?: dataState.currentAddress?.let { address ->
            vodovozServiceRepository.addAddress(address.toDomain()).singleResult().onFailure { throwable ->

            }.getOrNull()
        }

        addressId?.let {
            eventListener.emit(MapFlowEvents.GoToAddAddress(addressId))
        }

        uiStateListener.updateData { s ->
            s.copy(buttonIsLoading = false)
        }

    }

    data class SavedPolylineData(
        val distance: Double,
        val polyline: Polyline?,
        val startPoint: Point,
        val endPoint: Point,
    )

    data class SavedPointData(
        val latitude: String,
        val longitude: String,
        val length: String,
    )

    @Immutable
    data class MapFlowState(

        val deliveryZonesBundleUI: DeliveryZonesBundleUI? = null,
        val addressUI: AddressUI? = null,
        val updateZones: Boolean = false,
        val savedPointData: SavedPointData? = null,
        val polyline: Polyline? = null,
        val distance: Double? = null,
        val listOnPoints: List<Point> = emptyList(),
        val listOfSavedPolylinesData: List<SavedPolylineData> = emptyList(),
        val pendingUpdateAddressUI: AddressUI? = null,

        val query: String = "",
        val showSettingsDialog: Boolean = false,
        val currentAddress: MapAddressUi? = null,
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

        data class ShowAddAddressBottomDialog(val address: AddressUI?) : MapFlowEvents()
        data class Submit(val startPoint: Point, val list: List<Point>) : MapFlowEvents()
        data class ShowInfoDialog(val url: String?) : MapFlowEvents()
        data class ShowAlert(val response: ResponseBody) : MapFlowEvents()
        data class ShowPolyline(val polyline: Polyline? = null, val message: String? = null) :
            MapFlowEvents()

        data object AddAddressSuccess : MapFlowEvents()
        data class AddAddressError(val message: String) : MapFlowEvents()
        data class UpdatePendingAddressUISuccess(val address: AddressUI) : MapFlowEvents()
        data object ShowSearchError : MapFlowEvents()

        data object MoveToGeoOrMoscow : MapFlowEvents()
        data object CheckGeo : MapFlowEvents()
        data object MoveCameraMinus : MapFlowEvents()
        data object MoveCameraPlus : MapFlowEvents()
        data object ShowAddressBottomSheet : MapFlowEvents()
        data object HideAddressBottomSheet : MapFlowEvents()
        data object GoBack : MapFlowEvents()
        data object HideKeyboard : MapFlowEvents()
        data object GoToLocationSettings : MapFlowEvents()
        data class GoToAddAddress(val addressId: Long) : MapFlowEvents()

        data class MoveToAddress(val addressPoint: MapPointUi) : MapFlowEvents()
    }
}