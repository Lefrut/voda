package com.m.vodovoz.feature.addresses.add

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.AddAddressLabelBSUi
import com.m.vodovoz.design_system.model.AddressLabelUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.EmptyTextValidator
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.WidgetUi
import com.m.vodovoz.design_system.model.widgets.WidgetUpdater
import com.m.vodovoz.design_system.model.widgets.WidgetUpdaterKeeperFactory
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.mapToUi
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.addresses.add.model.AddAddressEvent
import com.m.vodovoz.feature.addresses.add.model.AddAddressState
import com.m.vodovoz.feature.addresses.add.model.AddAddressUiState
import com.m.vodovoz.feature.map.model.MapAddressUi
import com.m.vodovoz.feature.map.model.toDomain
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AddAddressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<AddAddressState, AddAddressEvent>(AddAddressState()) {


    private val addressId = savedStateHandle.get<Long>("addressId")?.also { id ->
        updateState { s -> s.copy(addressId = id) }
    }
    private val addressName = savedStateHandle.get<String>("addressName")

    init {
        savedStateHandle.get<MapAddressUi>("mapAddress")?.let { mapAddress ->
            updateState { s -> s.copy(mapAddress = mapAddress) }
        }
        fetchAddressDetails()
    }

    fun navigateBack() = viewModelScope.launch {
        if (addressId == null) {
            sendEvent(AddAddressEvent.GoBackToMap)
        } else {
            sendEvent(AddAddressEvent.GoBackToAddresses)
        }
    }

    fun showRemoveAddressDialog() {
        updateState { s ->
            s.copy(showRemoveAddressDialog = true)
        }
    }

    fun hideRemoveAddressDialog() {
        updateState { s ->
            s.copy(showRemoveAddressDialog = false)
        }
    }

    fun removeAddress() = viewModelScope.launch {
        if (addressId == null) return@launch

        vodovozServiceRepository.removeAddress(addressId.toInt()).singleResult()
        updateState { s ->
            s.copy(showRemoveAddressDialog = false)
        }
        navigateBack()
    }

    private fun getWidgetIdsAndValues(): Map<String, String> = with(stateSnapshot) {
        return (linearSwitches.associate { w ->
            w.id to VodovozBoolean.from(w.value()).boolean.toString()
        } + linearFields.associate { w ->
            w.id to w.value()
        } + gridFields.associate { w ->
            w.id to w.value()
        } + with(addressField) {
            id to (mapAddress?.name ?: value())
        }) + mapOf(addressLabel.id to addressLabel.name)
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
                updateState { s ->
                    s.copy(gridFields = fields, button = s.button.copy(enabled = false))
                }
                return@launch
            }
        }

        val params = getWidgetIdsAndValues()

        updateState { s ->
            s.copy(
                button = s.button.copy(
                    loading = true,
                    enabled = false
                )
            )
        }

        val addAddressResult = vodovozServiceRepository.addAddress(
            address = mapAddress.toDomain(),
            params = params
        ).singleResult()



        addAddressResult.onSuccess {
            sendEvent(AddAddressEvent.GoBackToAddresses)
        }.onFailure { throwable ->
            sendEvent(
                AddAddressEvent.ShowSnackbar(
                    throwable.message ?: resourcesProvider.getString(R.string.add_address_error)
                )
            )
        }

        updateState { s -> s.copy(button = s.button.copy(loading = false)) }
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
                updateState { s ->
                    s.copy(
                        gridFields = fields,
                        button = s.button.copy(
                            enabled = false
                        )
                    )
                }
                return@launch
            }
        }

        val params = getWidgetIdsAndValues()

        val mapAddress = stateSnapshot.mapAddress

        updateState { s ->
            s.copy(button = s.button.copy(loading = true))
        }

        val updateAddressResult = vodovozServiceRepository.updateAddress(
            addressId = addressId,
            address = mapAddress?.toDomain(),
            params = params
        ).singleResult()

        updateAddressResult.onSuccess {
            sendEvent(AddAddressEvent.GoBackToAddresses)
        }.onFailure { throwable ->
            sendEvent(
                AddAddressEvent.ShowSnackbar(
                    throwable.message ?: resourcesProvider.getString(R.string.update_address_error)
                )
            )
        }

        updateState { s ->
            s.copy(
                button = s.button.copy(
                    loading = false,
                    enabled = false
                )
            )
        }

    }

    private val widgetUpdaterKeeper = WidgetUpdaterKeeperFactory.createWidgetUpdateKeeper(
        updaters = listOf(addressSwitchesUpdater) + WidgetUpdaterKeeperFactory.baseUpdaters
    ) { id -> resourcesProvider.getString(id) }

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) {
        val updatedLinearFields = widgetUpdaterKeeper.updateWidget(
            widgets = stateSnapshot.linearFields,
            widget = widget,
            updatedWidget = updatedWidget
        ).filterIsInstance<FieldUi>()

        val updatedSwitches = widgetUpdaterKeeper.updateWidget(
            widgets = stateSnapshot.linearSwitches,
            widget = widget,
            updatedWidget = updatedWidget
        ).filterIsInstance<SwitchUi>()

        val updatedGridFields = widgetUpdaterKeeper.updateWidget(
            widgets = stateSnapshot.gridFields,
            widget = widget,
            updatedWidget = updatedWidget
        ).filterIsInstance<FieldUi>()

        updateState { s ->
            s.copy(
                linearFields = updatedLinearFields,
                linearSwitches = updatedSwitches,
                gridFields = updatedGridFields.updateByPrivateHouseRules(updatedSwitches),
                button = s.button.copy(enabled = true)
            )
        }


    }

    fun fetchAddressDetails() = vodovozServiceRepository.getAddAddressDetails(addressId).combine(
        vodovozServiceRepository.getAddressLabels()
    ) { p1, p2 ->
        p1 to p2
    }.onStart {
        updateState { s ->
            s.copy(uiState = AddAddressUiState.Loading)
        }
    }.onEach { (addAddressDetailsResult, addressLabelsModel) ->

        addAddressDetailsResult.onFailure {
            updateState { s ->
                s.copy(uiState = AddAddressUiState.Error)
            }
        }.onSuccess { addAddressDetails ->
            updateState { s ->
                val addressField = addAddressDetails.addressField.toUi()
                val linearSwitches = addAddressDetails.linearSwitches.mapToUi()
                s.copy(
                    uiState = AddAddressUiState.Form,
                    linearFields = addAddressDetails.linearFields.mapToUi(),
                    gridFields = addAddressDetails.gridFields.mapToUi()
                        .updateByPrivateHouseRules(linearSwitches),
                    addressField = addressField.copy(
                        value = stateSnapshot.mapAddress?.name ?: addressName
                        ?: addressField.value
                    ),
                    button = addAddressDetails.button.toUi(),
                    linearSwitches = linearSwitches,
                    addressLabel = addAddressDetails.label.toUi()
                )
            }
        }.mapCatching {
            val addressLabels = addressLabelsModel.getOrThrow()
            updateState { s ->
                s.copy(
                    labels = addressLabels.labels.mapToUi(),
                    addLabelBS = addressLabels.popupWindow?.toUi()
                )
            }
        }
    }.launchIn(viewModelScope)


    private fun List<FieldUi>.updateByPrivateHouseRules(
        switches: List<SwitchUi>,
    ): List<FieldUi> {
        val privateHouseSwitch = switches.find { it.id == PRIVATE_HOUSE_ID } ?: return this
        return map { gridField ->
            if (gridField.id == FLOOR_ID || gridField.id == FLAT_ID) {
                gridField.copy(
                    isRequired = !privateHouseSwitch.value,
                    isError = gridField.isError && !privateHouseSwitch.value
                )
            } else {
                gridField
            }
        }
    }

    fun checkWidgetOnAddress(widget: WidgetUi) = viewModelScope.launch {
        if (addressId == null) {
            sendEvent(AddAddressEvent.GoBackToMap)
        } else {
            sendEvent(AddAddressEvent.GoToMap(widget.value()))
        }
    }

    fun changeMapAddress(mapAddress: MapAddressUi) = viewModelScope.launch {
        updateState { s ->
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

    fun selectAddressLabel(addressLabel: AddressLabelUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(addressLabel = addressLabel)
        }
    }

    fun removeAddressLabel(addressLabel: AddressLabelUi) = viewModelScope.launch {
        updateState { s ->
            val currentAddressLabel = s.addressLabel
            s.copy(
                addressLabel = if (addressLabel == currentAddressLabel) AddressLabelUi.Empty else currentAddressLabel,
                labels = buildList {
                    addAll(s.labels)
                    removeIf { it.name == addressLabel.name }
                }
            )
        }
        vodovozServiceRepository.deleteAddressLabel(addressLabel.name).singleResult()
    }

    fun changeAddedLabel(value: String) {
        updateAddLabelBS { copy(value = value) }
    }

    fun addLabel(label: String) = viewModelScope.launch {
        if(label.isBlank()) return@launch

        updateAddLabelBS {
            copy(button = button.copy(loading = true))
        }

        vodovozServiceRepository.addAddressLabel(label).singleResult()
        vodovozServiceRepository.getAddressLabels().singleResult().mapCatching { labelsModel ->
            labelsModel.labels.mapToUi()
        }.recoverCatching {
            stateSnapshot.labels
        }.onSuccess { labels ->
            updateState { state ->
                val bs = state.addLabelBS

                state.copy(
                    labels = labels,
                    showAddLabelBS = false,
                    addLabelBS = bs?.copy(
                        value = "",
                        button = bs.button.copy(
                            loading = false
                        )
                    )
                )
            }
        }
    }

    private fun updateAddLabelBS(
        showBS: Boolean = stateSnapshot.showAddLabelBS,
        block: AddAddressLabelBSUi.() -> AddAddressLabelBSUi = { this },
    ) {
        updateState { s ->
            val bs = s.addLabelBS?.let {
                block(s.addLabelBS)
            }
            s.copy(
                showAddLabelBS = showBS,
                addLabelBS = bs
            )
        }
    }

    fun showAddLabelBS() {
        updateAddLabelBS(true)
    }

    fun closeAddLabelBS() {
        updateAddLabelBS(false)
    }

}

private const val PRIVATE_HOUSE_ID = "chasdom"
private const val DELIVERY_OFFICE_ID = "dostavkaofis"
private const val FLOOR_ID = "floor"
private const val FLAT_ID = "flat"

private val addressSwitchesUpdater = object : WidgetUpdater {
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
