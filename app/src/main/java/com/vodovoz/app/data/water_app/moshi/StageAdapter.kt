package com.vodovoz.app.data.water_app.moshi

import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.vodovoz.app.common.moshi.adapter.NullableAdapter
import com.vodovoz.app.common.water_app.WaterApp

class StageAdapter : NullableAdapter<WaterApp.Stage>() {
    override fun parse(reader: JsonReader): WaterApp.Stage =
        WaterApp.Stage(reader.nextString())

    override fun serialize(writer: JsonWriter, value: WaterApp.Stage) {
        writer.value(value.name)
    }
}