package com.vodovoz.app.feature.all.orders.detail.traceorder

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.feature.all.orders.detail.model.DriverPointsEntity
import com.vodovoz.app.util.extensions.debugLog
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class TraceOrderViewModel @Inject constructor(
    private val application: Application,
    private val accountManager: AccountManager,
) : PagingContractViewModel<TraceOrderViewModel.TraceOrderState, TraceOrderViewModel.TraceOrderEvents>(
    TraceOrderState()
) {

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    init {
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

    fun fetchWhereOrderDetails() = viewModelScope.launch {



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
                            snapshot.child("Position_vodila").child("Longitude").value.toString()
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

                        val driverPointsEntity = list.find { it?.OrderNumber == orderId.toString() }

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
        eventListener.emit(TraceOrderEvents.CheckGeo)
    }

    fun showSettingDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showSettingDialog = true)
        }
    }

    fun moveToGeo() = viewModelScope.launch {
        eventListener.emit(TraceOrderEvents.MoveToGeo)
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
        val carPoint: MapPointUi? = MapPointUi(55.740117, 37.565874),
        val deliveryPoint: MapPointUi? = MapPointUi(55.729210, 37.574360),
        val uiState: TraceOrderUiState = TraceOrderUiState.NotLoading
    ) : State

    sealed interface TraceOrderUiState {
        data object NotLoading: TraceOrderUiState
        data object Error: TraceOrderUiState
    }

    sealed class TraceOrderEvents : Event {

        data object GoBack : TraceOrderEvents()

        data object MoveCameraPlus : TraceOrderEvents()

        data object MoveCameraMinus : TraceOrderEvents()

        data object CheckGeo : TraceOrderEvents()
        data object MoveToGeo : TraceOrderEvents()
        data object GoToLocationSettings : TraceOrderEvents()


    }
}