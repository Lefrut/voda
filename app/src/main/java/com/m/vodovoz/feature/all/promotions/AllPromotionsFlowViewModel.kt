package com.m.vodovoz.feature.all.promotions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import com.m.vodovoz.R
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.AboutAdvertisingUi
import com.m.vodovoz.design_system.model.PromotionCategoryUi
import com.m.vodovoz.design_system.model.PromotionUi
import com.m.vodovoz.design_system.model.mapToDomain
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.promotion.PromotionsSectionModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.all.promotions.api.AllPromotionsNavKey
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.paging.PagingMviViewModel
import com.m.vodovoz.ui.paging.PagingState
import com.m.vodovoz.ui.paging.emptyCombinedLoadStates
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = AllPromotionsFlowViewModel.Factory::class)
class AllPromotionsFlowViewModel @AssistedInject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    @Assisted private val navKey: AllPromotionsNavKey,
) : PagingMviViewModel<PromotionUi, AllPromotionsFlowViewModel.AllPromotionsState, AllPromotionsFlowViewModel.AllPromotionsEvent>(
    AllPromotionsState()
) {

    private val dataSource = navKey.dataSource

    init {
        fetchPromotions()
    }


    private suspend fun getAllPromotions(): PromotionsSectionModel? {
        return when (dataSource) {
            AllPromotionsNavKey.DataSource.All -> vodovozServiceRepository.getAllPromotionsDetails()

            is AllPromotionsNavKey.DataSource.ByBanner -> vodovozServiceRepository.getBannerPromotions(
                bannerId = dataSource.bannerId,
                blockId = dataSource.blockId,
                categoryId = stateSnapshot.currentCategory.id
            )
        }.singleResult().getOrNull()
    }

    private fun getAllPromotionsPaged(categoryId: Int): Flow<PagingData<PromotionUi>> {
        return when (dataSource) {
            AllPromotionsNavKey.DataSource.All -> vodovozServiceRepository.getAllPromotionsPaged(
                categoryId = categoryId.takeIf { it > -1 }
            )

            is AllPromotionsNavKey.DataSource.ByBanner -> vodovozServiceRepository.getBannerPromotionsPaged(
                bannerId = dataSource.bannerId,
                blockId = dataSource.blockId,
                categoryId = categoryId.takeIf { it > -1 }
            )
        }.map { pagingData ->
            pagingData.map { promotionModel ->
                promotionModel.toUi()
            }
        }
    }

    fun fetchPromotions() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = UiState.Loading)
        }

        val sectionPromotions = if (stateSnapshot.categories.isEmpty()) {
            getAllPromotions()
        } else {
            PromotionsSectionModel(
                title = stateSnapshot.title,
                categories = stateSnapshot.categories.mapToDomain(),
                promotions = emptyList(),
                button = null
            )
        }


        if (sectionPromotions == null) {
            updateState { s ->
                s.copy(uiState = UiState.Error)
            }
            return@launch
        }


        val categories = listOf(
            PromotionCategoryUi(
                id = -1,
                code = "",
                name = resourcesProvider.getString(
                    R.string.all
                )
            )
        ) + sectionPromotions.categories.mapToUi()


        val currentCategory =
            if (stateSnapshot.currentCategory == PromotionCategoryUi.Empty) {
                (categories.firstOrNull() ?: PromotionCategoryUi.Empty)
            } else {
                stateSnapshot.currentCategory
            }

        updateState { s ->
            val title = sectionPromotions.title

            s.copy(
                title = title,
                categories = categories.distinct(),
                currentCategory = currentCategory,
                uiState = UiState.Success
            )
        }

        getAllPromotionsPaged(currentCategory.id).collectPagingData()
    }

    fun selectSection(category: PromotionCategoryUi) = viewModelScope.launch {
        if (category == stateSnapshot.currentCategory) return@launch

        sendEvent(AllPromotionsEvent.ScrollTop)
        updateState { s ->
            s.copy(currentCategory = category)
        }
        fetchPromotions()
    }

    fun closeAdvertisingBottomSheet() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                showAdvertisingBottomSheet = false
            )
        }
    }

    fun showAdvertisingBottomSheet(promotionUi: PromotionUi) = viewModelScope.launch {
        promotionUi.aboutAdvertisingUi?.let {
            updateState { s ->
                s.copy(
                    currentAdvertising = promotionUi.aboutAdvertisingUi,
                    showAdvertisingBottomSheet = true
                )
            }
        }
    }

    fun navigateToPromotionDetails(promotion: PromotionUi) = viewModelScope.launch {
        sendEvent(AllPromotionsEvent.GoToProductDetails(promotionId = promotion.id))
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(AllPromotionsEvent.GoBack)
    }

    @Immutable
    data class AllPromotionsState(
        val title: String = "",
        val categories: List<PromotionCategoryUi> = emptyList(),
        val currentCategory: PromotionCategoryUi = PromotionCategoryUi.Empty,
        val showAdvertisingBottomSheet: Boolean = false,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
        val uiState: UiState = UiState.Loading,
        val appendState: LoadState = LoadState.NotLoading(false),
        override val items: List<PromotionUi> = emptyList(),
        override val loadStates: CombinedLoadStates = emptyCombinedLoadStates,
    ) : PagingState<PromotionUi, AllPromotionsState>() {

        override fun copyPagingState(
            items: List<PromotionUi>,
            loadStates: CombinedLoadStates,
        ): AllPromotionsState = copy(items = items, loadStates = loadStates)
    }

    @Stable
    sealed interface UiState {
        data object Loading : UiState
        data object Success : UiState
        data object Error : UiState
    }

    sealed class AllPromotionsEvent : Event {

        data object ScrollTop : AllPromotionsEvent()

        data class GoToProductDetails(
            val promotionId: Long,
        ) : AllPromotionsEvent()

        data object GoBack : AllPromotionsEvent()

    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: AllPromotionsNavKey): AllPromotionsFlowViewModel
    }
}
