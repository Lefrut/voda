package com.m.vodovoz.domain.general.model.location

data class AddressLabelModel(
    val id: String,
    val name: String,
    val isRemoveable: Boolean,
) {
    companion object {
        val Empty = AddressLabelModel("", "", false)
    }
}
