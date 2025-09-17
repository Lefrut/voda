package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.user.ForAdultsModel

@Immutable
data class ForAdultsUi(
    val title: String,
    val description: String,
    val textBlur: String,
    val button: ColorfulButtonUi
)

fun ForAdultsModel.toUi(): ForAdultsUi{
    return ForAdultsUi(
        title, description, textBlur, button.toUi()
    )
}
