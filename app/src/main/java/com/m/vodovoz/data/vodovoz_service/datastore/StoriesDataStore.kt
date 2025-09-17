package com.m.vodovoz.data.vodovoz_service.datastore

import android.content.Context
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow


interface StoriesDataStore {

    val viewedIdsFlow: Flow<List<Long>>

    suspend fun saveViewedIds(ids: List<Long>)

    suspend fun getViewedIds(): List<Long>

    suspend fun addViewedId(id: Long)

    suspend fun clear()

}