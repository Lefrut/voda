package com.vodovoz.app.common.moshi.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

abstract class NullableAdapter<T> : JsonAdapter<T>() {

    abstract fun parse(reader: JsonReader): T
    abstract fun serialize(writer: JsonWriter, value: T)

    @FromJson
    final override fun fromJson(reader: JsonReader): T? {
        return if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
            null
        } else parse(reader)
    }

    @ToJson
    final override fun toJson(writer: JsonWriter, value: T?) {
        if (value == null) {
            writer.nullValue()
        } else serialize(writer, value)
    }
}
