package com.m.vodovoz.domain.general.respository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {

    val canViewAdultProducts: Flow<Boolean>

    suspend fun setCanViewAdultProducts(canView: Boolean)

    suspend fun getCanViewAdultProducts(): Boolean

    val viewedStoryIds: Flow<List<Long>>

    suspend fun addViewedStoryId(storyId: Long)

    suspend fun getViewedStoryIds(): List<Long>

    suspend fun clearAll(): Result<Unit>

}