package com.m.vodovoz.feature.questionnaires.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object QuestionnairesNavKey : NavKey {
    const val NAV_NAME: String = "feature/questionnaires/Questionnaires"
}
