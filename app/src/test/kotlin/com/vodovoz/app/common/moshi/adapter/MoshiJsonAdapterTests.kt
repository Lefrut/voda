package com.vodovoz.app.common.moshi.adapter

import CoroutineTestBase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

class MoshiJsonAdapterTests: CoroutineTestBase() {

    @Test
    fun durationAdapter_roundtrip_and_null() {
        val adapter = DurationJsonAdapter()

        val json = adapter.toJson(2500.milliseconds)
        assertEquals("2500", json)

        // deserialize
        val obj = adapter.fromJson("1500")
        assertEquals(1500.milliseconds, obj)

        assertNull(adapter.fromJson("null"))
        assertEquals("null", adapter.toJson(null))
    }


}