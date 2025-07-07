package com.vodovoz.app.core.network.converters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.time.LocalDateTime

class UnicodeJsonAdapter : JsonAdapter<String>() {
    override fun fromJson(reader: JsonReader): String? {
        return reader.nextString()?.let(::normalizeUnicode)
    }

    override fun toJson(writer: JsonWriter, value: String?) {
        writer.value(value)
    }

    private fun normalizeUnicode(text: String): String {
        return text
            .replace("\u2028", "\n")
            .replace("\u2029", "\n")
            .replace("\u00A0", " ")
    }
}