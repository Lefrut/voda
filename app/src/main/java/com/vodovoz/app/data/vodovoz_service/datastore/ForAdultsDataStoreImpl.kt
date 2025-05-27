package com.vodovoz.app.data.vodovoz_service.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

private val Context.adultsPrefsDataStore by preferencesDataStore(
    name = "ForAdultsPrefs"
)

@Singleton
class ForAdultsDataStoreImpl @Inject constructor(
    @ApplicationContext
    private val context: Context,
) : ForAdultsDataStore {

    override val canViewFlow: Flow<Boolean> = context.adultsPrefsDataStore.data.onStart {
        ensureFresh()
    }.map { prefs ->
        val last = prefs[keyLastCheck] ?: 0L
        val now = System.currentTimeMillis()
        if (now - last > TIMEOUT_MS) false
        else prefs[keyCanView] ?: false
    }

    override suspend fun getCanView(): Boolean {
        return canViewFlow.firstOrNull() ?: false
    }

    override suspend fun setCanView(canView: Boolean) {
        context.adultsPrefsDataStore.edit { prefs ->
            prefs[keyCanView] = canView
            prefs[keyLastCheck] = System.currentTimeMillis()
        }
    }

    private suspend fun ensureFresh() {
        val prefs = context.adultsPrefsDataStore.data.firstOrNull() ?: return
        val last = prefs[keyLastCheck] ?: 0L
        if (System.currentTimeMillis() - last > TIMEOUT_MS) {
            context.adultsPrefsDataStore.edit {
                it[keyCanView] = false
                it[keyLastCheck] = System.currentTimeMillis()
            }
        }
    }

    companion object {
        private const val TIMEOUT_MS = 24 * 60 * 60 * 1000

        private val keyCanView = booleanPreferencesKey("can_view_adult_content")
        private val keyLastCheck = longPreferencesKey("last_check_time")
    }
}