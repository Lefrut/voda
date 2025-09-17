package com.m.vodovoz.data.water_app.moshi

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.m.vodovoz.common.moshi.adapter.NullableAdapter
import com.m.vodovoz.common.water_app.WaterApp

class WaterAppStageJsonAdapter : NullableAdapter<WaterApp.Stage>() {
    override fun parse(reader: JsonReader): WaterApp.Stage =
        WaterApp.Stage(reader.nextString())

    override fun serialize(writer: JsonWriter, value: WaterApp.Stage) {
        writer.value(value.name)
    }
}