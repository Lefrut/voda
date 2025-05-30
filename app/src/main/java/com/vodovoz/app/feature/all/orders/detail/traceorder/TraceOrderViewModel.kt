package com.vodovoz.app.feature.all.orders.detail.traceorder

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.jivochat.JivoChatController
import com.vodovoz.app.design_system.model.ImageAndTextUi
import com.vodovoz.app.design_system.model.ImageButtonUi
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.all.orders.detail.model.DriverPointsEntity
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val application: Application,
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val siteStateManager: SiteStateManager,
    savedStateHandle: SavedStateHandle,
) : PagingContractViewModel<TraceOrderViewModel.TraceOrderState, TraceOrderViewModel.TraceOrderEvents>(
    TraceOrderState()
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    private val orderId: Long = savedStateHandle["orderId"] ?: navigateBack().run { -1 }
    private val driverId: String = savedStateHandle["driverId"] ?: navigateBack().run { "" }

    init {
        viewModelScope.launch {
            fetchWhereOrderDetails()
        }

        viewModelScope.launch(Dispatchers.Default) {
            generateBitmap(
                "http://vodovoz.ru/bitrix/templates/vodovoz/images/karta/auto.png",
                true
            )
            generateBitmap(
                "http://vodovoz.ru/bitrix/templates/vodovoz/images/karta/home.png",
                false
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.GoBack)
    }

    private fun generateBitmap(url: String, auto: Boolean) {
        val bitmap = Glide
            .with(application)
            .asBitmap()
            .load(url)
            .submit()
            .get()

        val scaledBitmap = Bitmap.createScaledBitmap(
            bitmap,
            80,
            80,
            false
        )

        uiStateListener.value = if (auto) {
            state.copy(
                data = state.data.copy(
                    autoBitmap = scaledBitmap
                )
            )
        } else {
            state.copy(
                data = state.data.copy(
                    homeBitmap = scaledBitmap
                )
            )
        }
    }

    fun orderDetailsCallbackFlow(): Flow<Unit> = callbackFlow {

        val ticker = launch {

            fetchWhereOrderDetails()

            moveToAvailableGeo()

            delay(3_000)

            while (isActive) {
                fetchWhereOrderDetails()
                delay(4_000)
            }
        }

        awaitClose { ticker.cancel() }
    }


    private suspend fun fetchWhereOrderDetails() {

        //
        //orderId
        //driverId
        val whereOrderDetailsResult =
            vodovozServiceRepository.getWhereMyOrderDetails(1761455, "mh-2424%20(Сафонов)")
                .singleResult()

        whereOrderDetailsResult.onSuccess { whereOrderDetails ->
            uiStateListener.updateData { s ->
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

    fun fetchDriverData(driverId: String?, orderId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            debugLog { "driverId $driverId" }
            if (driverId == null) return@launch
            runCatching {
                firebaseDatabase.child(driverId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (!snapshot.exists()) return

                        val nameBuilder = StringBuilder()
                        val carBuilder = StringBuilder()
                        val lastName = snapshot.child("LastName").value.toString()
                        val firstName = snapshot.child("FirstName").value.toString()
                        val carName = snapshot.child("Auto").value.toString()
                        val carNumber = snapshot.child("CarNumber").value.toString()

                        debugLog { "lastName $lastName, firstName $firstName, carName $carName, carNumber $carNumber" }

                        nameBuilder
                            .append("водитель ")
                            .apply {
                                if (lastName.isNotEmpty()) {
                                    append("$lastName ")
                                }
                                if (firstName.isNotEmpty()) {
                                    append(firstName)
                                }
                            }

                        carBuilder
                            .apply {
                                if (carName.isNotEmpty()) {
                                    append("$carName ")
                                }
                                if (carNumber.isNotEmpty()) {
                                    append(", номер машины $carNumber")
                                }
                            }

                        val driverLatitude =
                            snapshot.child("Position_vodila").child("Latitude").value.toString()
                                .replace(",", ".")
                        val driverLongitude =
                            snapshot.child("Position_vodila")
                                .child("Longitude").value.toString()
                                .replace(",", ".")

                        debugLog { "driverLatitude $driverLatitude, driverLongitude $driverLongitude" }

                        val point =
                            if (driverLatitude.isNotEmpty() && driverLongitude.isNotEmpty()) {
                                Point(driverLatitude.toDouble(), driverLongitude.toDouble())
                            } else {
                                null
                            }

                        val list = mutableListOf<DriverPointsEntity?>()
                        snapshot.child("ListTochki").children.forEach {
                            val driverPointsEntity = it.getValue(DriverPointsEntity::class.java)
                            list.add(driverPointsEntity)
                        }

                        val driverPointsEntity =
                            list.find { it?.OrderNumber == orderId.toString() }

                        debugLog { "driverPointsEntity $driverPointsEntity" }


                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                name = nameBuilder.toString(),
                                car = carBuilder.toString(),
                                driverPoint = point,
                                driverPointsEntity = driverPointsEntity
                            )
                        )

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }.onFailure {
                debugLog { "fetchDriverData error $it" }
                accountManager.reportError("fetchDriverData error", it)
            }
        }
    }

    fun plusZoom() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.MoveCameraPlus)
    }

    fun minusZoom() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.MoveCameraMinus)
    }

    fun checkGeo() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.CheckUserGeo)
    }

    fun showSettingDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingDialog = true)
        }
    }

    fun moveToUserGeo() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.MoveToUserGeo)
    }

    fun closeSettingsDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingDialog = false)
        }
    }

    fun navigateToLocationSettings() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingDialog = false)
        }
        eventListener.emit(TraceOrderEvents.GoToLocationSettings)
    }

    fun activateButton(imageButton: ImageButtonUi) = viewModelScope.launch {


        when (imageButton.id) {
            "chat" -> {

                eventListener.emit(TraceOrderEvents.GoToJivoChat(JivoChatController.getLink()))
            }

            else -> {
                val siteState = siteStateManager.siteStateFlow.value ?: return@launch
                val phone = siteState.callPhoneNumber
                eventListener.emit(TraceOrderEvents.Phone(phone))
            }
        }
    }

    fun moveToAvailableGeo() = viewModelScope.launch {
        if (dataState.carPoint != null) {
            eventListener.emit(
                TraceOrderEvents.MoveToDeliveryGeo(
                    dataState.finishPoint,
                    dataState.carPoint
                )
            )
        } else {
            eventListener.emit(
                TraceOrderEvents.MoveToUserGeo
            )
        }
    }

    fun hideBottomSheet() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.HideBottomSheet)
    }

    fun showBottomSheet() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.ShowBottomSheet)
    }

    @Immutable
    data class TraceOrderState(
        val item: Item? = null,
        val name: String? = null,
        val car: String? = null,
        val driverPoint: Point? = null,
        val autoBitmap: Bitmap? = null,
        val homeBitmap: Bitmap? = null,
        val driverPointsEntity: DriverPointsEntity? = null,

        val showSettingDialog: Boolean = false,
        val carPoint: MapPointUi? = null,
        val finishPoint: MapPointUi? = null,
        val uiState: TraceOrderUiState = TraceOrderUiState.NotLoading,
        val title: String = "",
        val bottomSheetTitle: String = "",
        val bottomSheetButtons: List<ImageButtonUi> = emptyList(),
        val bottomSheetItems: List<ImageAndTextUi> = emptyList(),
    ) : State

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