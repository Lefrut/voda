package com.m.vodovoz.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update


inline fun <K, V> MutableMap<K, V>.setIfPresent(key: K, value: (V) -> V): Unit {
    get(key)?.let { put(key, value(it)) }
}


@Suppress("NOTHING_TO_INLINE")
inline fun <K, V> MutableStateFlow<Map<K, V>>.set(key: K, value: V) {
    update {
        it.toMutableMap().apply { set(key, value) }
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun <K, V> MutableStateFlow<Map<K, V>>.setAll(map: Map<K, V>) {
    update {
        it.toMutableMap().apply {
            map.forEach { entry -> set(entry.key, entry.value) }
        }
    }
}

