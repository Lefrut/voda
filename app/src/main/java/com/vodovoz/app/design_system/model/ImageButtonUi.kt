package com.vodovoz.app.design_system.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.vodovoz.app.domain.general.model.widgets.ImageButtonModel
import com.vodovoz.app.ui.graphics.fromHexOrUnspecified

@Immutable
data class ImageButtonUi(
    val id: String,
    val name: String,
    val image: String,
    val contentColor: Color,
    val containerColor: Color,
)

fun List<ImageButtonModel>.mapToUi(): List<ImageButtonUi> {
    return map { it.toUi() }
}

fun ImageButtonModel.toUi(): ImageButtonUi {
    return ImageButtonUi(
        id = id,
        name = name,
        image = image,
        contentColor = Color.fromHexOrUnspecified(textColor),
        containerColor = Color.fromHexOrUnspecified(backgroundColor)
    )
}
