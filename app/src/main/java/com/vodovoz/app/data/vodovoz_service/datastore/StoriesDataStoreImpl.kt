package com.vodovoz.app.data.vodovoz_service.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton


private val Context.storiesDataStore by preferencesDataStore(name = "stories_prefs")

@Singleton
class StoriesDataStoreImpl @Inject constructor(
    @ApplicationContext context: Context,
) : StoriesDataStore {

    private val dataStore = context.storiesDataStore

    companion object {
        private val VIEWED_IDS_KEY = stringSetPreferencesKey("viewed_story_ids")
    }

    override val viewedIdsFlow: Flow<List<Long>> = dataStore.data.map { prefs ->
        prefs[VIEWED_IDS_KEY]?.mapNotNull { it.toLongOrNull() } ?: emptyList()
    }

    override suspend fun saveViewedIds(ids: List<Long>) {
        val stringSet = ids.map(Long::toString).toSet()
        dataStore.edit { prefs ->
            prefs[VIEWED_IDS_KEY] = stringSet
        }
    }

    override suspend fun getViewedIds(): List<Long> {
        return dataStore.data
            .map { prefs -> prefs[VIEWED_IDS_KEY]?.mapNotNull { it.toLongOrNull() } ?: emptyList() }
            .firstOrNull() ?: emptyList()
    }

    override suspend fun addViewedId(id: Long) {
        dataStore.edit { prefs ->
            val current = prefs[VIEWED_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add(id.toString())
            prefs[VIEWED_IDS_KEY] = current
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(VIEWED_IDS_KEY) }
    }
}