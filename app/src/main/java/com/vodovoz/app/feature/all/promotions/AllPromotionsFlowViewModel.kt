package com.vodovoz.app.feature.all.promotions

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.map
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.PromotionCategoryUi
import com.vodovoz.app.design_system.model.PromotionUi
import com.vodovoz.app.design_system.model.mapToDomain
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.promotion.PromotionsSectionModel
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.ui.paging.PagingDataListener
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllPromotionsFlowViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<AllPromotionsFlowViewModel.AllPromotionsState, AllPromotionsFlowViewModel.AllPromotionsEvent>(
    AllPromotionsState()
) {

    private val dataSource = savedState.get<AllPromotionsFragment.DataSource>("dataSource")
        ?: AllPromotionsFragment.DataSource.All

    private val pagingListener = PagingDataListener { snapshotList ->
        uiStateListener.updateData { s ->
            s.copy(
                promotions = snapshotList.mapNotNull { promotion -> promotion }
            )
        }
    }

    init {
        listenProductsLoadStates()
        fetchPromotions()
    }

    private fun listenProductsLoadStates() = viewModelScope.launch {
        pagingListener.collectLoadState { combinedLoadStates ->
            uiStateListener.updateData { s ->
                s.copy(

                    appendState = combinedLoadStates.append
                )
            }
        }

    }

    private suspend fun getAllPromotions(): PromotionsSectionModel? {
        return when (dataSource) {
            AllPromotionsFragment.DataSource.All -> vodovozServiceRepository.getAllPromotionsDetails()
                .singleResult().getOrNull()

            is AllPromotionsFragment.DataSource.ByBanner -> vodovozServiceRepository.getBannerPromotions(
                bannerId = dataSource.bannerId,
                blockId = dataSource.blockId,
                categoryId = stateSnapshot.currentCategory.id
            ).singleResult().getOrNull()
        }
    }

    private fun getAllPromotionsPaged(categoryId: Int): Flow<PagingData<PromotionUi>> {
        return when (dataSource) {
            AllPromotionsFragment.DataSource.All -> vodovozServiceRepository.getAllPromotionsPaged(
                categoryId = categoryId
            )

            is AllPromotionsFragment.DataSource.ByBanner -> vodovozServiceRepository.getBannerPromotionsPaged(
                bannerId = dataSource.bannerId,
                blockId = dataSource.blockId,
                categoryId = categoryId
            )
        }.map { pagingData ->
            pagingData.map { promotionModel -> promotionModel.toUi() }
        }
    }

    fun fetchPromotions() = viewModelScope.launch {
        uiStateListener.updateData { s ->
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
            uiStateListener.updateData { s ->
                s.copy(uiState = UiState.Error)
            }
            return@launch
        }


        val categories = sectionPromotions.categories.mapToUi()
        val currentCategory =
            if (stateSnapshot.currentCategory == PromotionCategoryUi.Empty) {
                (categories.firstOrNull() ?: PromotionCategoryUi.Empty)
            } else {
                stateSnapshot.currentCategory
            }

        uiStateListener.updateData { s ->
            val title = sectionPromotions.title

            s.copy(
                title = title,
                categories = categories,
                currentCategory = currentCategory,
                uiState = UiState.Success
            )
        }

        getAllPromotionsPaged(currentCategory.id).collect { pagingData ->
            pagingListener.collectPagingData(pagingData)
        }

    }

    fun selectSection(category: PromotionCategoryUi) = viewModelScope.launch {
        if (category == stateSnapshot.currentCategory) return@launch

        eventListener.emit(AllPromotionsEvent.ScrollTop)
        uiStateListener.updateData { s ->
            s.copy(currentCategory = category)
        }
        fetchPromotions()
    }

    fun closeAdvertisingBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showAdvertisingBottomSheet = false
            )
        }
    }

    fun showAdvertisingBottomSheet(promotionUi: PromotionUi) = viewModelScope.launch {
        promotionUi.aboutAdvertisingUi?.let {
            uiStateListener.updateData { s ->
                s.copy(
                    currentAdvertising = promotionUi.aboutAdvertisingUi,
                    showAdvertisingBottomSheet = true
                )
            }
        }
    }

    fun navigateToPromotionDetails(promotion: PromotionUi) = viewModelScope.launch {
        eventListener.emit(AllPromotionsEvent.GoToProductDetails(promotionId = promotion.id))
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(AllPromotionsEvent.GoBack)
    }

    fun notifyPagingPromotions(index: Int) = viewModelScope.launch {
        kotlin.runCatching { pagingListener[index] }
    }

    @Immutable
    data class AllPromotionsState(
        val title: String = "",
        val categories: List<PromotionCategoryUi> = emptyList(),
        val currentCategory: PromotionCategoryUi = PromotionCategoryUi.Empty,
        val showAdvertisingBottomSheet: Boolean = false,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
        val uiState: UiState = UiState.Loading,
        val promotions: List<PromotionUi> = emptyList(),
        val appendState: LoadState = LoadState.NotLoading(false),
    ) : State

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
}