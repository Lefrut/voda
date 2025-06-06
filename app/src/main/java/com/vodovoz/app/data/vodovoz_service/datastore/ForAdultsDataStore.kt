package com.vodovoz.app.data.vodovoz_service.datastore
import kotlinx.coroutines.flow.Flow

interface ForAdultsDataStore {

    val canViewFlow: Flow<Boolean>
    suspend fun getCanView(): Boolean
    suspend fun setCanView(canView: Boolean)
}