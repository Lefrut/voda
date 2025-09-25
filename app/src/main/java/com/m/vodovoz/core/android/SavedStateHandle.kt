package com.m.vodovoz.core.android

import androidx.lifecycle.SavedStateHandle


fun<T> SavedStateHandle.getList(key: String): List<T> {
    return get<List<T>>(key) ?: emptyList()
}

fun SavedStateHandle.getString(key: String): String {
    return get(key) ?: ""
}