package com.m.vodovoz.design_system.model.widgets

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.widgets.LabelModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class LabelUi(
    val name: String,
    val textColorValue: ULong,
    val backgroundColorValue: ULong,
) : Parcelable {
    @IgnoredOnParcel
    val textColor: Color = Color(textColorValue)

    @IgnoredOnParcel
    val backgroundColor: Color = Color(backgroundColorValue)

    companion object {
        fun from(
            name: String,
            textColor: Color = Color.Unspecified,
            backgroundColor: Color = Color.Unspecified
        ) = LabelUi(
            name = name,
            textColorValue = textColor.value,
            backgroundColorValue = backgroundColor.value
        )
    }
}

fun List<LabelModel>.toUi(): List<LabelUi> {
    return mapNotNull { labelModel ->
        labelModel.toUi()
    }
}

fun LabelModel.toUi(): LabelUi {
    return LabelUi(
        name = name,
        textColorValue = Color.fromHexOrUnspecified(textColor).value,
        backgroundColorValue = Color.fromHexOrUnspecified(
            backgroundColor
        ).copy(alpha = backgroundAlpha).value,
    )
}