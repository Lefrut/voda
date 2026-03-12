package com.m.vodovoz.feature.questionnaires.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WelcomeQuestionnairesNavKey : NavKey {
    const val NAV_NAME: String = "feature/questionnaires/WelcomeQuestionnaires"
}
