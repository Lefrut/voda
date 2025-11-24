package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.location.AddAddressLabelBSModel

@Immutable
data class AddAddressLabelBSUi(
    val title: String,
    val hint: String,
    val value: String,
    val button: ColorfulButtonUi,
)


fun AddAddressLabelBSModel.toUi(): AddAddressLabelBSUi {
    return AddAddressLabelBSUi(
        title = title, hint = hint, value = value, button = button.toUi()
    )
}
