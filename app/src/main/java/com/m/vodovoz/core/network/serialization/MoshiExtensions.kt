package com.m.vodovoz.core.network.serialization

import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

fun <T : Any> Moshi.fromJson(json: String, type: Type): T {
    val adapter = adapter<T>(type)
    return adapter.lenient().fromJson(json)!!
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T : Any> Moshi.fromJson(json: String): T {
    val adapter = adapter<T>(typeOf<T>().javaType)
    return adapter.lenient().fromJson(json)!!
}

@OptIn(ExperimentalStdlibApi::class)
inline fun <reified T: Any> Moshi.toJson(data: T): String {
    val adapter = adapter<T>(typeOf<T>().javaType)
    return adapter.lenient().toJson(data)!!
}

