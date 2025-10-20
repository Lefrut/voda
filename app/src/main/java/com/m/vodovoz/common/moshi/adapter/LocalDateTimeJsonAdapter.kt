package com.m.vodovoz.common.moshi.adapter

import androidx.annotation.Keep
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Keep
class LocalDateTimeJsonAdapter : NullableAdapter<LocalDateTime>() {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    override fun parse(reader: JsonReader): LocalDateTime {
        return LocalDateTime.parse(reader.nextString(), formatter)
    }

    override fun serialize(writer: JsonWriter, value: LocalDateTime) {
        writer.value(value.format(formatter))
    }
}