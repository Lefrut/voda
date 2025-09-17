package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel

@Immutable
data class VodovozPlaceholderUi(
    val title: String,
    val headerHtml: String,
    val descriptionHtml: String,
    val imageUrl: String,
    val button: ColorfulButtonUi? = null,
){
    companion object{
        val Empty = VodovozPlaceholderUi("", "","","")
    }
}

fun VodovozPlaceholderModel.toUi(): VodovozPlaceholderUi{
    return VodovozPlaceholderUi(
        title, headerHtml, descriptionHtml, imageUrl, button?.toUi()
    )
}