package com.m.vodovoz.feature.questionnaires.model

import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.user.QuestionnairesDetailsModel


data class QuestionnairesDetailsUi(
    val title: String,
    val items: List<QuizComponentUi>,
    val button: ColorfulButtonUi
)

fun QuestionnairesDetailsModel.toUi(): QuestionnairesDetailsUi {
    return QuestionnairesDetailsUi(
        title = title,
        items = items.mapNotNull { it.toUi() },
        button = button.toUi()
    )
}
