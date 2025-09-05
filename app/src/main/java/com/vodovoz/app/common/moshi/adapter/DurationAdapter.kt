package com.vodovoz.app.common.moshi.adapter

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DurationAdapter : NullableAdapter<Duration>() {
    override fun parse(reader: JsonReader): Duration =
        reader.nextLong().milliseconds

    override fun serialize(writer: JsonWriter, value: Duration) {
        writer.value(value.inWholeMilliseconds)
    }
}
