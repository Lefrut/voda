package com.vodovoz.app.domain.general.respository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val canViewAdultProducts: Flow<Boolean>

    suspend fun setCanViewAdultProducts(canView: Boolean)

    suspend fun getCanViewAdultProducts(): Boolean

}