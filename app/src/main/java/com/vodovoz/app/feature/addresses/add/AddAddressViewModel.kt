package com.vodovoz.app.feature.addresses.add

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.boolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.FieldWidgetUpdater
import com.vodovoz.app.design_system.model.widgets.NoRequiredValidator
import com.vodovoz.app.design_system.model.widgets.RadioGroupUpdater
import com.vodovoz.app.design_system.model.widgets.SingleCheckboxGroupUpdater
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.SwitchWidgetUpdater
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.design_system.model.widgets.WidgetUpdater
import com.vodovoz.app.design_system.model.widgets.WidgetUpdaterHandler
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.toUi
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.add.model.AddAddressEvent
import com.vodovoz.app.feature.addresses.add.model.AddAddressState
import com.vodovoz.app.feature.addresses.add.model.AddAddressUiState
import com.vodovoz.app.feature.map.model.MapAddressUi
import com.vodovoz.app.feature.map.model.toDomain
import com.vodovoz.app.feature.map.model.toUi
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AddAddressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val mapServiceRepository: MapServiceRepository,
) : MviViewModel<AddAddressState, AddAddressEvent>(AddAddressState()) {


    private val addressId = savedStateHandle.get<Long>("addressId")?.also { id ->
        _state.update { s -> s.copy(addressId = id) }
    }

    private val addressName = savedStateHandle.get<String>("addressName")


    companion object {
        private const val PRIVATE_HOUSE_ID = "chasdom"
        private const val DELIVERY_OFFICE_ID = "dostavkaofis"
        private const val FLOOR_ID = "floor"
        private const val FLAT_ID = "flat"

    }

    init {
        savedStateHandle.get<MapAddressUi>("mapAddress")?.let { mapAddress ->
            _state.update { s -> s.copy(mapAddress = mapAddress) }
        }
        fetchAddressDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        if (addressId == null) {
            _events.emit(AddAddressEvent.GoBackToMap)
        } else {
            _events.emit(AddAddressEvent.GoBackToAddresses)
        }
    }

    fun showRemoveAddressDialog() {
        _state.update { s ->
            s.copy(showRemoveAddressDialog = true)
        }
    }

    fun hideRemoveAddressDialog() {
        _state.update { s ->
            s.copy(showRemoveAddressDialog = false)
        }
    }

    fun removeAddress() = viewModelScope.launch {
        if (addressId == null) return@launch

        vodovozServiceRepository.removeAddress(addressId.toInt()).singleResult()
        _state.update { s ->
            s.copy(showRemoveAddressDialog = false)
        }
        navigateBack()
    }

    private fun getWidgetIdsAndValues(): Map<String, String> {
        return (stateSnapshot.linearSwitches.associate { w ->
            w.id to VodovozBoolean.from(w.value()).boolean.toString()
        } + stateSnapshot.linearFields.associate { w ->
            w.id to w.value()
        } + stateSnapshot.gridFields.associate { w ->
            w.id to w.value()
        } + with(stateSnapshot.addressField) { id to (stateSnapshot.mapAddress?.name ?: value()) })
    }

    fun addAddress() = viewModelScope.launch {

        val mapAddress = stateSnapshot.mapAddress ?: return@launch

        stateSnapshot.gridFields.checkFields(
            putErrors = true,
            validators = listOf(
                NoRequiredValidator,
                EmptyTextValidator,
            ),
        ) { fields, isValid ->
            if (!isValid) {
                _state.update { s ->
                    s.copy(gridFields = fields, button = s.button.copy(enabled = false))
                }
                return@launch
            }
        }

        val params = getWidgetIdsAndValues()

        _state.update { s ->
            s.copy(
                button = s.button.copy(
                    loading = true,
                    enabled = false
                )
            )
        }

        val addAddressResult =
            vodovozServiceRepository.addAddress(mapAddress.toDomain(), params).singleResult()



        addAddressResult.onSuccess {
            _events.emit(AddAddressEvent.GoBackToAddresses)
        }.onFailure { throwable ->
            _events.emit(
                AddAddressEvent.ShowSnackbar(
                    throwable.message ?: resourcesProvider.getString(R.string.add_address_error)
                )
            )
        }

        _state.update { s -> s.copy(button = s.button.copy(loading = false)) }
    }


    fun updateAddress() = viewModelScope.launch {
        if (addressId == null) return@launch

        stateSnapshot.gridFields.checkFields(
            putErrors = true,
            validators = listOf(
                NoRequiredValidator,
                EmptyTextValidator,
            ),
        ) { fields, isValid ->
            if (!isValid) {
                _state.update { s ->
                    s.copy(gridFields = fields, button = s.button.copy(enabled = false))
                }
                return@launch
            }
        }

        val params = getWidgetIdsAndValues()

        val mapAddress = stateSnapshot.mapAddress ?: return@launch

        _state.update { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val updateAddressResult = vodovozServiceRepository.updateAddress(
            addressId = addressId,
            address = mapAddress.toDomain(),
            params = params
        ).singleResult()

        updateAddressResult.onSuccess {
            _events.emit(AddAddressEvent.GoBackToAddresses)
        }.onFailure { throwable ->
            _events.emit(
                AddAddressEvent.ShowSnackbar(
                    throwable.message ?: resourcesProvider.getString(R.string.update_address_error)
                )
            )
        }

        _state.update { s ->
            s.copy(
                button = s.button.copy(
                    loading = false,
                    enabled = false
                )
            )
        }

    }

    private val addressTypesSwitchUpdater = object : WidgetUpdater {

        override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
            return widget is SwitchUi && updatedWidget is SwitchUi
                    && ((widget.id == PRIVATE_HOUSE_ID && updatedWidget.id == PRIVATE_HOUSE_ID)
                    || (widget.id == DELIVERY_OFFICE_ID && updatedWidget.id == DELIVERY_OFFICE_ID))
        }

        override fun update(
            widgets: List<WidgetUi>,
            widget: WidgetUi,
            updatedWidget: WidgetUi,
            getString: (Int) -> String,
        ): List<WidgetUi> {
            val changed = updatedWidget as SwitchUi
            val turningOn = changed.value

            return widgets.map { current ->
                when {
                    current.id == changed.id -> changed

                    turningOn && current is SwitchUi &&
                            (current.id == PRIVATE_HOUSE_ID || current.id == DELIVERY_OFFICE_ID) ->
                        current.copy(value = false)

                    else -> current
                }
            }
        }

    }

    private val widgetUpdaterHandler = WidgetUpdaterHandler(
        updaters = listOf(
            addressTypesSwitchUpdater,
            FieldWidgetUpdater(
                listOf(
                    NoRequiredValidator,
                    EmptyTextValidator,
                )
            ),
            SwitchWidgetUpdater(),
            RadioGroupUpdater(),
            SingleCheckboxGroupUpdater()
        ),
        getString = { id -> resourcesProvider.getString(id) }
    )

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) {
        _state.update { s ->
            s.copy(
                linearFields = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.linearFields,
                    widget = widget,
                    updatedWidget = updatedWidget
                ).filterIsInstance<FieldUi>(),
                linearSwitches = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.linearSwitches,
                    widget = widget,
                    updatedWidget = updatedWidget
                ).filterIsInstance<SwitchUi>(),
                gridFields = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.gridFields,
                    widget = widget,
                    updatedWidget = updatedWidget
                ).filterIsInstance<FieldUi>(),
                button = s.button.copy(enabled = true)
            )
        }

        val privateHouseSwitch = stateSnapshot.linearSwitches.find { it.id == PRIVATE_HOUSE_ID }
        _state.update { s ->
            s.copy(
                gridFields = s.gridFields.map { gridField ->
                    if (gridField.id == FLOOR_ID || gridField.id == FLAT_ID) {
                        gridField.copy(
                            isRequired = privateHouseSwitch?.value != true,
                            isError = gridField.isError && privateHouseSwitch?.value == false
                        )
                    } else {
                        gridField
                    }
                }
            )
        }
    }

    fun fetchAddressDetails() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = AddAddressUiState.Loading)
        }

        val mapAddressDeferred = async {
            stateSnapshot.mapAddress ?: mapServiceRepository.searchAddressInMoscow(
                addressName ?: return@async null
            ).singleResult().getOrNull()?.toUi()
        }


        val addAddressDetailsResult =
            vodovozServiceRepository.getAddAddressDetails(addressId).singleResult()

        addAddressDetailsResult.onSuccess { addAddressDetails ->

            delay(200L)

            _state.update { s ->

                val currentMapAddress = mapAddressDeferred.await()
                val addressField = addAddressDetails.addressField.toUi()

                s.copy(
                    mapAddress = currentMapAddress,
                    uiState = AddAddressUiState.Form,
                    linearFields = addAddressDetails.linearFields.mapToUi(),
                    gridFields = addAddressDetails.gridFields.mapToUi(),
                    addressField = addressField.copy(
                        value = currentMapAddress?.let { mapAddress ->
                            resourcesProvider.getString(
                                R.string.full_address,
                                mapAddress.city,
                                mapAddress.street,
                                mapAddress.house
                            )
                        } ?: addressField.value
                    ),
                    button = addAddressDetails.button.toUi(),
                    linearSwitches = addAddressDetails.linearSwitches.mapToUi().filter { switch ->
                        addressId == null || (switch.id != PRIVATE_HOUSE_ID && switch.id != DELIVERY_OFFICE_ID)
                    }
                )
            }
        }.onFailure {
            _state.update { s ->
                s.copy(uiState = AddAddressUiState.Error)
            }
        }


    }

    fun checkWidgetOnAddress(widget: WidgetUi) = viewModelScope.launch {
        if (addressId == null) {
            _events.emit(AddAddressEvent.GoBackToMap)
        } else {
            _events.emit(AddAddressEvent.GoToMap(widget.value()))
        }
    }

    fun changeMapAddress(mapAddress: MapAddressUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                mapAddress = mapAddress,
                addressField = s.addressField.copy(
                    value = resourcesProvider.getString(
                        R.string.full_address,
                        mapAddress.city,
                        mapAddress.street,
                        mapAddress.house
                    )
                )
            )
        }
    }


}