package com.vodovoz.app.core.datastore

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

inline operator fun <reified T> Preferences.get(keyName: String): T? {
    asMap().forEach { (t, u) ->
        if (t.name == keyName) return u as? T
    }
    return null
}

@Suppress("UNCHECKED_CAST")
inline operator fun <reified T> MutablePreferences.set(keyName: String, value: T) {
    val key: Preferences.Key<T> = when (value) {
        is Int -> intPreferencesKey(keyName)
        is Double -> doublePreferencesKey(keyName)
        is String -> stringPreferencesKey(keyName)
        is Boolean -> booleanPreferencesKey(keyName)
        is Float -> floatPreferencesKey(keyName)
        is Long -> longPreferencesKey(keyName)
        is Set<*> -> stringSetPreferencesKey(keyName)
        is ByteArray -> byteArrayPreferencesKey(keyName)
        else -> throw IllegalArgumentException(
            "Unsupported type for Preferences key: ${value!!::class.java.name}"
        )
    } as Preferences.Key<T>
    set(key, value)
}