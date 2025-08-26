package com.vodovoz.app.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import java.util.Locale

@Stable
sealed interface WaterAppUiState {

    data object Welcome : WaterAppUiState

    @Immutable
    enum class UserData : WaterAppUiState {
        Gender,
        Height,
        Weight,
        WakeUpTime,
        SleepTime,
        ActivityLevel;

        fun next(): UserData? {
            return entries.getOrNull(ordinal + 1)
        }

        fun previous(): UserData? {
            return entries.getOrNull(ordinal - 1)
        }

        companion object {
            val weights: List<Float>
                get() = (0..((300f - 20f) / 0.2f).toInt())
                    .map { i ->
                        val value = String.format(Locale.US, "%.1f", 20f + i * 0.2f)
                        value.toFloatOrNull() ?: 0f
                    }

            val heights get() = (50..240).toList()
        }
    }

    data object WaterGoal : WaterAppUiState

    data object Settings : WaterAppUiState

    data object Main : WaterAppUiState

    data object GoalCompleted : WaterAppUiState

}