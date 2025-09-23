package com.m.vodovoz.feature.addresses.add.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.feature.map.model.MapAddressUi

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