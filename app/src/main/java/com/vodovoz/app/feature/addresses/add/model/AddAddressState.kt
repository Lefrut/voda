package com.vodovoz.app.feature.addresses.add.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.AddressDetailsUi
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi

@Immutable
data class AddAddressState(
    val widgets: List<WidgetUi> = listOf(),
    val fields: List<FieldUi> = listOf(),
    val withoutSpaceWidgets: List<WidgetUi> = emptyList(),
    val details: AddressDetailsUi? = null,
    val showRemoveAddressDialog: Boolean = false,
    val addressName: String = "",
)
