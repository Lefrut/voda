package com.m.vodovoz.domain.general.model.location

data class AddressLabelsModel(
    val labels: List<AddressLabelModel>,
    val popupWindow: AddAddressLabelBSModel?,
)
