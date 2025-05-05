package com.vodovoz.app.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface WaterAppUiState {

    data object Welcome: WaterAppUiState

    @Immutable
    enum class UserData: WaterAppUiState {
        Gender, Height, Weight, WakeUpTime, SleepTime, ActivityLevel;

        fun next(): UserData? {
            val values = entries
            val nextIndex = ordinal + 1
            return values.getOrNull(nextIndex)
        }

        fun previous(): UserData? {
            val values = entries
            val prevIndex = ordinal - 1
            return values.getOrNull(prevIndex)
        }
    }

    data object WaterGoal: WaterAppUiState

    data object Settings: WaterAppUiState

    data object Main: WaterAppUiState

    data object GoalCompleted: WaterAppUiState

}