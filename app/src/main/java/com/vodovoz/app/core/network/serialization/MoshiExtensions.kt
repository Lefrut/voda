package com.vodovoz.app.core.network.serialization

import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

fun <T : Any> Moshi.fromJson(json: String, type: Type): T {
    val adapter = adapter<T>(type)
    return adapter.fromJson(json)!!
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> Moshi.fromJson(json: String): T {
    val adapter = adapter<T>(typeOf<T>().javaType)
    return adapter.fromJson(json)!!
}