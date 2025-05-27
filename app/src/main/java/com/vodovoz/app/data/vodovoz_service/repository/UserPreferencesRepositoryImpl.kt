package com.vodovoz.app.data.vodovoz_service.repository

import com.vodovoz.app.data.vodovoz_service.datastore.ForAdultsDataStore
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val forAdultsDataStore: ForAdultsDataStore
) : UserPreferencesRepository {

    override val canViewAdultProducts: Flow<Boolean> = forAdultsDataStore.canViewFlow

    override suspend fun setCanViewAdultProducts(canView: Boolean) {
        forAdultsDataStore.setCanView(canView)
    }

    override suspend fun getCanViewAdultProducts(): Boolean {
        return forAdultsDataStore.getCanView()
    }
}