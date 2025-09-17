package com.m.vodovoz.domain.general.model.product

data class SortModel(
    val name: String,
    val value: String,
    val order: String,
) {
    companion object {
        val Empty = SortModel("", "", "")
    }
}
