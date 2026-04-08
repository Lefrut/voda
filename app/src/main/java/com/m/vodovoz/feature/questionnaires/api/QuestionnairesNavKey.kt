package com.m.vodovoz.feature.questionnaires.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object QuestionnairesNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/questionnaires/Questionnaires"
}
