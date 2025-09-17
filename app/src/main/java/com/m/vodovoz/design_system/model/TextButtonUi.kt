package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.m.vodovoz.domain.general.model.user.TextButtonModel
import com.m.vodovoz.ui.graphics.fromHexOrUnspecified

@Immutable
data class TextButtonUi(
    val text: String,
    val textColor: Color
){
    companion object{
        val Empty = TextButtonUi("",Color.Unspecified)
    }
}

fun TextButtonModel.toUi(): TextButtonUi{
    return TextButtonUi(
        text = text,
        textColor = Color.fromHexOrUnspecified(textColor)
    )
}
