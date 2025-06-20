package com.vodovoz.app.data.vodovoz_service.repository

import com.vodovoz.app.data.vodovoz_service.datastore.ForAdultsDataStore
import com.vodovoz.app.data.vodovoz_service.datastore.StoriesDataStore
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val forAdultsDataStore: ForAdultsDataStore,
    private val storiesDataStore: StoriesDataStore,
) : UserPreferencesRepository {

    override val canViewAdultProducts: Flow<Boolean> = forAdultsDataStore.canViewFlow

    override suspend fun setCanViewAdultProducts(canView: Boolean) {
        kotlin.runCatching { forAdultsDataStore.setCanView(canView) }
    }

    override suspend fun getCanViewAdultProducts(): Boolean {
        return kotlin.runCatching { forAdultsDataStore.getCanView() }.getOrNull() ?: false
    }

    override val viewedStoryIds: Flow<List<Long>> = storiesDataStore.viewedIdsFlow

    override suspend fun addViewedStoryId(storyId: Long) {
        kotlin.runCatching { storiesDataStore.addViewedId(storyId) }
    }

    override suspend fun getViewedStoryIds(): List<Long> {
        return kotlin.runCatching { storiesDataStore.getViewedIds() }.getOrNull() ?: emptyList()
    }

    override suspend fun clearAll(): Result<Unit> {
        return runCatching {
            storiesDataStore.clear()
        }
    }
}