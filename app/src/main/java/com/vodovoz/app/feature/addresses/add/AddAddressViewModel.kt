package com.vodovoz.app.feature.addresses.add

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.model.VodovozAddressType
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.FieldTypeUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.RadioButtonGroupUi
import com.vodovoz.app.design_system.model.widgets.RadioOptionUi
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.design_system.model.widgets.WidgetUpdaterHandler
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.addresses.add.model.AddAddressEvent
import com.vodovoz.app.feature.addresses.add.model.AddAddressState
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class AddAddressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<AddAddressState, AddAddressEvent>(AddAddressState()) {

    private val addressId = savedStateHandle.get<Long>("addressId") ?: (-1L)
    private val firstAddressName = savedStateHandle.get<String>("addressName").also { address ->
        _state.update { s -> s.copy(addressName = address ?: "") }
    } ?: ""


    init {
        initUi()
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(AddAddressEvent.GoBack)
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
        vodovozServiceRepository.removeAddress(addressId.toInt()).singleResult()
        _state.update { s ->
            s.copy(showRemoveAddressDialog = false)
        }
        _events.emit(AddAddressEvent.GoBack)
    }

    fun updateAddress() = viewModelScope.launch {
        //todo - update address
    }

    private val widgetUpdaterHandler = WidgetUpdaterHandler(
        getString = { id -> resourcesProvider.getString(id) }
    )

    fun changeWidget(widget: WidgetUi, updatedWidget: WidgetUi) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                widgets = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.widgets,
                    widget = widget,
                    updatedWidget = updatedWidget
                ),
                withoutSpaceWidgets = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.withoutSpaceWidgets,
                    widget = widget,
                    updatedWidget = updatedWidget
                ),
                fields = widgetUpdaterHandler.updateWidget(
                    widgets = stateSnapshot.fields,
                    widget = widget,
                    updatedWidget = updatedWidget
                ).filterIsInstance<FieldUi>()
            )
        }
    }

    private fun initUi() = viewModelScope.launch {

        val radioOptions = listOf(
            RadioOptionUi(
                name = resourcesProvider.getString(
                    R.string.personal_house_delivery_text,
                ),
                value = VodovozAddressType.Personal.value
            ),
            RadioOptionUi(
                name = resourcesProvider.getString(
                    R.string.company_office_delivery_text,
                ),
                value = VodovozAddressType.Company.value
            )
        )

        //todo - need to put original values
        _state.update { s ->
            s.copy(
                withoutSpaceWidgets = listOf(
                    RadioButtonGroupUi(
                        id = "tip",
                        options = radioOptions,
                        option = radioOptions.firstOrNull() ?: RadioOptionUi("", 1)
                    ),
                    SwitchUi(
                        id = "propusk",
                        name = resourcesProvider.getString(R.string.need_pass),
                        value = false,
                        enabled = true
                    )
                ),
                widgets = listOf(
                    FieldUi(
                        id = "polnadres",
                        label = resourcesProvider.getString(R.string.address_title),
                        value = "",
                        keyboardType = KeyboardType.Text,
                        isRequired = true,
                        isError = false,
                        readOnly = true,
                        supportingText = "",
                        hint = "",
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    ),
                    FieldUi(
                        id = "comment",
                        label = resourcesProvider.getString(R.string.comment),
                        value = "",
                        keyboardType = KeyboardType.Text,
                        isRequired = false,
                        isError = false,
                        readOnly = false,
                        supportingText = "",
                        hint = resourcesProvider.getString(R.string.enter_comment),
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    )
                ),
                fields = listOf(
                    FieldUi(
                        id = "flat",
                        label = resourcesProvider.getString(R.string.flat),
                        value = "",
                        keyboardType = KeyboardType.Text,
                        isRequired = false,
                        isError = false,
                        readOnly = false,
                        supportingText = "",
                        hint = "",
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    ),
                    FieldUi(
                        id = "entrance",
                        label = resourcesProvider.getString(R.string.entrance),
                        value = "",
                        keyboardType = KeyboardType.Number,
                        isRequired = false,
                        isError = false,
                        readOnly = false,
                        supportingText = "",
                        hint = "",
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    ),
                    FieldUi(
                        id = "domofon",
                        label = resourcesProvider.getString(R.string.intercom),
                        value = "",
                        keyboardType = KeyboardType.Text,
                        isRequired = false,
                        isError = false,
                        readOnly = false,
                        supportingText = "",
                        hint = "",
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    ),
                    FieldUi(
                        id = "floor",
                        label = resourcesProvider.getString(R.string.floor),
                        value = "",
                        keyboardType = KeyboardType.Number,
                        isRequired = false,
                        isError = false,
                        readOnly = false,
                        supportingText = "",
                        hint = "",
                        type = FieldTypeUi.Text,
                        isValueVisible = true
                    )

                )
            )
        }

    }

    fun checkWidgetOnAddress(widget: WidgetUi) = viewModelScope.launch {
        if (widget.id == "polnadres") {
            _events.emit(AddAddressEvent.GoToMap(addressId, stateSnapshot.addressName))
        }
    }

    fun changeAddressName(addressName: String) = viewModelScope.launch {
        _state.update { s -> s.copy(addressName = addressName) }
    }

}