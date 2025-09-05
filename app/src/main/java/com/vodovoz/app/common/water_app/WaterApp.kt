package com.vodovoz.app.common.water_app

import androidx.annotation.Keep
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data object WaterApp {

    fun calculateDailyGoal(userInfo: UserInfo): DailyGoal {
        val weight = BigDecimal(userInfo.weight.toDouble())
        val sport = BigDecimal(userInfo.activityLevel.sport.toDouble())

        val base = BigDecimal("1.5")
        val coefficient = BigDecimal("0.02")
        val thousand = BigDecimal("1000")

        val result = base
            .add(weight.subtract(BigDecimal("20")).multiply(coefficient))
            .add(sport)
            .multiply(thousand)
            .setScale(0, RoundingMode.HALF_UP)

        return DailyGoal.create(result.intValueExact())
    }


    @Keep
    data class NotificationSettings(
        val hasNotifications: Boolean,
        val notificationsDelay: Duration,
        val wakeUpTime: LocalTime,
        val sleepTime: LocalTime,
    ) {
        companion object {
            val Default = NotificationSettings(
                hasNotifications = false,
                notificationsDelay = 2.hours,
                wakeUpTime = LocalTime.of(9, 0, 0),
                sleepTime = LocalTime.of(23, 0, 0)
            )
        }
    }

    @Keep
    data class UserInfo(
        val gender: Gender,
        val height: Float,
        val weight: Float,
        val activityLevel: ActivityLevel,
    ) {

        @Keep
        enum class Gender {
            Man, Girl
        }

        @Keep
        enum class ActivityLevel(val sport: Float) {
            Low(0.25f), Medium(0.375f), High(0.5f)
        }

        companion object {
            val ManDefault = UserInfo(Gender.Man, 175f, 80f, ActivityLevel.Low)
            val GirlDefault = UserInfo(Gender.Girl, 165f, 65f, ActivityLevel.Low)
        }

    }

    @Keep
    data class DailyGoal(
        val totalMl: Int,
        val currentMl: Int,
        val wasCompleted: Boolean,
    ) {

        init {
            require(totalMl > 0 && currentMl >= 0)
        }

        fun plusMl(ml: Int): DailyGoal {
            val newCurrentMl = (currentMl + ml).coerceAtMost(totalMl)
            return DailyGoal(totalMl, newCurrentMl, wasCompleted || newCurrentMl >= totalMl)
        }

        companion object {
            fun create(totalMl: Int) = DailyGoal(
                totalMl,
                0,
                false
            )
        }


    }

    @JvmInline
    @Keep
    value class Stage(val name: String)

}
