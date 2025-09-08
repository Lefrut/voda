package com.vodovoz.app.common.moshi.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate

class LocalDateJsonAdapter {

    @ToJson
    fun toJson(value: LocalDate): String = value.toString()

    @FromJson
    fun fromJson(value: String): LocalDate = LocalDate.parse(value)
}
