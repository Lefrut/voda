package com.m.vodovoz.core.network.serialization

import androidx.annotation.Keep
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@Keep
fun <T : Any> Moshi.fromJson(json: String, type: Type): T {
    val adapter = adapter<T>(type)
    return adapter.lenient().fromJson(json)!!
}

@Keep
fun <T : Any> Moshi.fromJson(json: String, clazz: KClass<T>): T {
    val adapter = adapter(clazz.java)
    return adapter.lenient().fromJson(json)!!
}


@OptIn(ExperimentalStdlibApi::class)
@Keep
inline fun <reified T : Any> Moshi.fromJson(json: String): T {
    val adapter = adapter<T>(typeOf<T>().javaType)
    return adapter.lenient().fromJson(json)!!
}

@OptIn(ExperimentalStdlibApi::class)
@Keep
inline fun <reified T: Any> Moshi.toJson(data: T): String {
    val adapter = adapter<T>(typeOf<T>().javaType)
    return adapter.lenient().toJson(data)!!
}

@OptIn(ExperimentalStdlibApi::class)
@Keep
fun <T: Any> Moshi.toJson(data: T, type: Type): String {
    val adapter = adapter<T>(type)
    return adapter.lenient().toJson(data)!!
}


