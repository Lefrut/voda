package com.m.vodovoz.domain.general.model.promotion

import com.m.vodovoz.common.model.VodovozAction

data class StoryModel(
    val id: Long,
    val previewImage: String,
    val pages: List<ActionWithButtonModel>,
    val viewed: Boolean
)

data class ActionWithButtonModel(
    val action: VodovozAction,
    val colorfulButton: ColorfulButtonModel,
    val image: String
)

data class ColorfulButtonModel(
    val name: String,
    val backgroundColor: String,
    val textColor: String,
    val id: String = "",
    val browser: Boolean? = null,
    val url: String? = null
){
    companion object{
        val Empty = ColorfulButtonModel("","", "", "")
    }
}



