package com.m.vodovoz.common.moshi.adapter

import androidx.annotation.Keep
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalTime

@Keep
class LocalTimeJsonAdapter {
    @ToJson
    fun toJson(value: LocalTime): String = value.toString()

    @FromJson
    fun fromJson(value: String): LocalTime = LocalTime.parse(value)
}