package com.m.vodovoz.domain.general.model.widgets


data class LabelModel(
    val name: String,
    val backgroundAlpha: Float = 1f,
    val backgroundColor: String,
    val textColor: String,
) {

    companion object {
        val Empty = LabelModel(
            name = "",
            backgroundAlpha = 0f,
            backgroundColor = "",
            textColor = ""
        )
    }
}
