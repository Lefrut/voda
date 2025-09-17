package com.m.vodovoz.common.water_app

import CoroutineTestBase
import com.squareup.moshi.JsonAdapter
import com.m.vodovoz.data.water_app.moshi.WaterAppStageJsonAdapter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.reflect.KClass

class WaterAppTests : CoroutineTestBase() {

    private val man = WaterApp.UserInfo.ManDefault

    @Test
    fun stageAdapter_roundtrip_and_null() {
        val adapter: JsonAdapter<WaterApp.Stage> = WaterAppStageJsonAdapter()
        // serialize
        val json = adapter.toJson(WaterApp.Stage("MAIN"))
        assertEquals("\"MAIN\"", json)

        // deserialize
        val obj = adapter.fromJson("\"ONBOARDING\"")
        assertEquals(WaterApp.Stage("ONBOARDING"), obj)

        // null cases
        assertNull(adapter.fromJson("null"))
        assertEquals("null", adapter.toJson(null))
    }

    @Test
    fun calculateDailyGoal_variousInputs() = with(WaterApp) {
        assertEquals(
            2750,
            calculateWaterNorm(
                man.copy(
                    weight = 70f,
                    activityLevel = WaterApp.ActivityLevel.Low
                )
            )
        )
        assertEquals(
            2800,
            calculateWaterNorm(
                man.copy(
                    weight = 65f,
                    activityLevel = WaterApp.ActivityLevel.Medium
                )
            )
        )

        assertEquals(
            4400,
            calculateWaterNorm(
                man.copy(
                    weight = 140f,
                    activityLevel = WaterApp.ActivityLevel.High
                )
            )
        )
    }


    @Test
    fun `daily goal plus ml`() {
        val dailyGoal = WaterApp.DailyGoal.create(3000)

        assertEquals(dailyGoal.copy(currentMl = 250), dailyGoal.plusMl(250))
        assertEquals(dailyGoal, dailyGoal.plusMl(0))
        assertEquals(
            dailyGoal.copy(currentMl = 3000, wasCompleted = true),
            dailyGoal.plusMl(35000)
        )
    }

    @Test
    fun `erasure types class check`() = runTest {
        fun <T : Any> method(value: List<T>): KClass<out T> {
            return value.first()::class
        }

        assertEquals(1::class, method(listOf(2)))

    }

}

