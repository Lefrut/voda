package com.vodovoz.app.domain.general.model.promotion

import com.vodovoz.app.common.model.VodovozAction

data class StoryModel(
    val id: Long,
    val image: String,
    val actionWithButtonList: List<ActionWithButtonModel>,
    val viewed: Boolean
)

data class ActionWithButtonModel(
    val action: VodovozAction,
    val colorfulButton: ColorfulButtonModel
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



