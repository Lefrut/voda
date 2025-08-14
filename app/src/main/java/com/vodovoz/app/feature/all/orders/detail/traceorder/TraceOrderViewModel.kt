package com.vodovoz.app.feature.all.orders.detail.traceorder

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.jivochat.JivoChatController
import com.vodovoz.app.design_system.model.ImageAndTextUi
import com.vodovoz.app.design_system.model.ImageButtonUi
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class TraceOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val siteStateManager: SiteStateManager,
    private val mapServiceRepository: MapServiceRepository,
) : MviViewModel<TraceOrderViewModel.TraceOrderState, TraceOrderViewModel.TraceOrderEvents>(
    TraceOrderState()
) {

    private val orderId: Long = savedStateHandle["orderId"] ?: navigateBack().run { -1 }
    private val driverId: String = savedStateHandle["driverId"] ?: navigateBack().run { "" }

    init {
        viewModelScope.launch {
            fetchWhereOrderDetails()
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.GoBack)
    }

    fun orderDetailsCallbackFlow(): Flow<Unit> = callbackFlow {

        val ticker = launch {

            fetchWhereOrderDetails()
            moveToAvailableGeo()

            delay(5_000)

            while (isActive) {
                fetchWhereOrderDetails()
                delay(3_500)
            }
        }

        awaitClose { ticker.cancel() }
    }

    private suspend fun fetchWhereOrderDetails() {

        val whereOrderDetailsResult =
            vodovozServiceRepository.getWhereMyOrderDetails(orderId, driverId)
                .singleResult()

        whereOrderDetailsResult.onSuccess { whereOrderDetails ->
            _state.update { s ->
                s.copy(
                    uiState = TraceOrderUiState.NotLoading,
                    carPoint = whereOrderDetails.driverPont?.toUi() ?: s.carPoint,
                    finishPoint = whereOrderDetails.finishPoint?.toUi() ?: s.finishPoint,
                    title = whereOrderDetails.title.ifEmpty { s.title },
                    bottomSheetTitle = whereOrderDetails.secondTitle.ifEmpty { s.bottomSheetTitle },
                    bottomSheetItems = whereOrderDetails.items.mapToUi()
                        .ifEmpty { s.bottomSheetItems },
                    bottomSheetButtons = whereOrderDetails.buttons.mapToUi()
                        .ifEmpty { s.bottomSheetButtons }
                )
            }
        }
    }

    fun plusZoom() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.MoveCameraPlus)
    }

    fun minusZoom() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.MoveCameraMinus)
    }

    fun checkGeo() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.CheckUserGeo)
    }

    fun showSettingDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showSettingDialog = true)
        }
    }

    fun moveToUserGeo() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.MoveToUserGeo)
    }

    fun closeSettingsDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showSettingDialog = false)
        }
    }

    fun navigateToLocationSettings() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showSettingDialog = false)
        }
        sendEvent(TraceOrderEvents.GoToLocationSettings)
    }

    fun activateButton(imageButton: ImageButtonUi) = viewModelScope.launch {
        when (imageButton.id) {
            "chat" -> {
                sendEvent(TraceOrderEvents.GoToJivoChat(JivoChatController.getLink()))
            }

            else -> {
                val siteState = siteStateManager.siteStateFlow.value ?: return@launch
                val phone = siteState.callPhoneNumber
                sendEvent(TraceOrderEvents.Phone(phone))
            }
        }
    }

    fun moveToAvailableGeo() = viewModelScope.launch {
        if (stateSnapshot.carPoint != null) {
            sendEvent(
                TraceOrderEvents.MoveToDeliveryGeo(
                    stateSnapshot.finishPoint,
                    stateSnapshot.carPoint
                )
            )
        } else {
            sendEvent(
                TraceOrderEvents.MoveToUserGeo
            )
        }
    }

    fun hideBottomSheet() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.HideBottomSheet)
    }

    fun showBottomSheet() = viewModelScope.launch {
        sendEvent(TraceOrderEvents.ShowBottomSheet)
    }

    @Immutable
    data class TraceOrderState(
        val showSettingDialog: Boolean = false,
        val carPoint: MapPointUi? = null,
        val currentRoutePoints: List<MapPointUi> = emptyList(),
        val finishPoint: MapPointUi? = null,
        val uiState: TraceOrderUiState = TraceOrderUiState.NotLoading,
        val title: String = "",
        val bottomSheetTitle: String = "",
        val bottomSheetButtons: List<ImageButtonUi> = emptyList(),
        val bottomSheetItems: List<ImageAndTextUi> = emptyList(),
    ) : State

    @Stable
    sealed interface TraceOrderUiState {
        data object NotLoading : TraceOrderUiState
        data object Error : TraceOrderUiState
    }

    sealed class TraceOrderEvents : Event {

        data object GoBack : TraceOrderEvents()

        data object MoveCameraPlus : TraceOrderEvents()

        data object MoveCameraMinus : TraceOrderEvents()

        data object CheckUserGeo : TraceOrderEvents()
        data class MoveToDeliveryGeo(val finishPoint: MapPointUi?, val driverPoint: MapPointUi?) :
            TraceOrderEvents()

        data class Phone(val phone: String) : TraceOrderEvents()
        data class GoToJivoChat(val link: String) : TraceOrderEvents()

        data object MoveToUserGeo : TraceOrderEvents()
        data object GoToLocationSettings : TraceOrderEvents()
        data object HideBottomSheet : TraceOrderEvents()
        data object ShowBottomSheet : TraceOrderEvents()


    }
}