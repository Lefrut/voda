package com.vodovoz.app.domain.general.model.user

import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel

data class QuestionnairesWelcomeDetailsModel(
    val title: String,
    val image: String,
    val header: String,
    val description: String,
    val buttons: List<ColorfulButtonModel>
)
