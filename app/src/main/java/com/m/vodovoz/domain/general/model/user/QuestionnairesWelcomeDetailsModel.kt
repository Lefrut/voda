package com.m.vodovoz.domain.general.model.user

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

data class QuestionnairesWelcomeDetailsModel(
    val title: String,
    val image: String,
    val header: String,
    val description: String,
    val buttons: List<ColorfulButtonModel>
)
