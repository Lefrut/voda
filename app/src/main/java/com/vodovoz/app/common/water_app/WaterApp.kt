package com.vodovoz.app.common.water_app

import androidx.annotation.Keep
import androidx.compose.runtime.Stable
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

data object WaterApp {

    fun calculateDailyGoal(userInfo: UserInfo): DailyGoal {
        val result = calculateWaterNorm(userInfo)
        return DailyGoal.create(result)
    }

    fun calculateWaterNorm(
        userInfo: UserInfo,
    ): Int {
        val bigWeight = BigDecimal(userInfo.weight.toDouble())
        val sport = BigDecimal(userInfo.activityLevel.sport.toDouble())

        val base = BigDecimal("1.5")
        val coefficient = BigDecimal("0.02")
        val thousand = BigDecimal("1000")
        val result = base
            .add(bigWeight.subtract(BigDecimal("20")).multiply(coefficient))
            .add(sport)
            .multiply(thousand)
            .setScale(0, RoundingMode.HALF_UP)
            .intValueExact()

        return roundUpTo50(result)
    }

    private fun roundUpTo50(n: Int): Int {
        return if (n % 50 == 0) n else n + (50 - n % 50)
    }


    @Keep
    @Stable
    data class NotificationSettings(
        val enableNotifications: Boolean,
        val notificationsDelay: Duration,
        val wakeUpTime: LocalTime,
        val sleepTime: LocalTime,
    ) {
        companion object {
            val Default = NotificationSettings(
                enableNotifications = false,
                notificationsDelay = 2.hours,
                wakeUpTime = LocalTime.of(9, 0, 0),
                sleepTime = LocalTime.of(23, 0, 0),
            )
        }
    }

    @Keep
    @Stable
    data class UserInfo(
        val gender: Gender,
        val height: Float,
        val weight: Float,
        val activityLevel: ActivityLevel,
    ) {

        companion object {
            val ManDefault = UserInfo(Gender.Man, 175f, 80f, ActivityLevel.Low)
            val GirlDefault = UserInfo(Gender.Girl, 165f, 65f, ActivityLevel.Low)
        }

    }

    @Keep
    @Stable
    enum class Gender {
        Man, Girl;

        companion object {
            fun from(isMan: Boolean): Gender {
                return when (isMan) {
                    true -> Man
                    false -> Girl
                }
            }
        }
    }

    @Keep
    enum class ActivityLevel(val sport: Float) {
        Low(0.25f), Medium(0.375f), High(0.5f)
    }


    @Keep
    data class DailyGoal(
        val totalMl: Int,
        val currentMl: Int,
        val wasCompleted: Boolean,
        val date: LocalDate,
    ) {

        init {
            require(totalMl > 0 && currentMl >= 0)
        }

        fun withMl(ml: Int): DailyGoal {
            val newCurrentMl = (0 + ml).coerceAtMost(totalMl)
            return copy(
                currentMl = newCurrentMl,
                wasCompleted = wasCompleted || newCurrentMl >= totalMl,
            )
        }

        fun plusMl(ml: Int): DailyGoal {
            return withMl(currentMl + ml)
        }


        companion object {
            fun create(totalMl: Int) = DailyGoal(
                totalMl,
                0,
                false,
                LocalDate.now()
            )
        }


    }

    @JvmInline
    @Keep
    value class Stage(val name: String)

}
