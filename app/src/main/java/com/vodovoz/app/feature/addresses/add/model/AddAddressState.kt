package com.vodovoz.app.feature.addresses.add.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.AddressDetailsUi
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.SwitchUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi
import com.vodovoz.app.feature.map.model.MapAddressUi

@Immutable
data class AddAddressState(
    val mapAddress: MapAddressUi? = null,
    val addressField: FieldUi = FieldUi.Empty,
    val linearFields: List<FieldUi> = listOf(),
    val gridFields: List<FieldUi> = listOf(),
    val linearSwitches: List<SwitchUi> = emptyList(),
    val showRemoveAddressDialog: Boolean = false,
    val button: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val uiState: AddAddressUiState = AddAddressUiState.Loading,
    val addressId: Long? = null,
)
