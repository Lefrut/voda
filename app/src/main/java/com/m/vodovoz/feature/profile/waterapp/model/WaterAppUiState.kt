package com.m.vodovoz.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import java.time.LocalTime
import java.util.Locale

@Stable
sealed interface WaterAppUiState {

    data object Loading : WaterAppUiState

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

            val times
                get() = (0 until 1440 step 15).map { minutes ->
                    try {
                        val sleepTime = LocalTime.ofSecondOfDay(minutes.toLong() * 60)
                        sleepTime.format(WaterAppHelper.timeFormatter)
                    } catch (_: Throwable) {
                        ""
                    }
                }

        }
    }

    data object WaterGoal : WaterAppUiState

    data object Settings : WaterAppUiState {

        val reminderIntervals = listOf(
            15L, 30L, 60L, 90L, 120L, 180L, 240L, 300L
        ).mapToReminderIntervalUi()

    }

    data object Main : WaterAppUiState {

        val waterSteps = listOf(
            100, 250, 500, 750, 1000
        ).map { WaterStepUi(it) }


    }

    data object GoalCompleted : WaterAppUiState

    companion object {
        val checkpoints: List<WaterAppUiState> = listOf(Welcome, Settings, Main)
    }


}

fun WaterAppUiState.toStage(): WaterApp.Stage {
    return WaterApp.Stage(toString())
}