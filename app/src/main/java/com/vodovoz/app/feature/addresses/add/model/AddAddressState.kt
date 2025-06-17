package com.vodovoz.app.feature.addresses.add.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.WidgetUi

@Immutable
data class AddAddressState(
    val widgets: List<WidgetUi> = emptyList(),
    val fields: List<FieldUi> = emptyList(),
    val showRemoveAddressDialog: Boolean = false
)
