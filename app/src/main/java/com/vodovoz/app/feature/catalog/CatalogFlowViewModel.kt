package com.vodovoz.app.feature.catalog

import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.stringToErrorState
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.domain.general.model.DataAllAction
import com.vodovoz.app.domain.general.model.VodovozAction
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.catalog.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<CatalogFlowViewModel.CatalogState, CatalogFlowViewModel.CatalogEvents>(
    CatalogState()
) {


    fun refresh() {
        fetchCatalogDetails()
    }

    fun fetchCatalogDetails() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = UiState.Loading)
        }
        vodovozServiceRepository.getCatalogDetails().collect { catalogDetailsResult ->
            catalogDetailsResult.onSuccess { catalogDetails ->
                val (banners, categories) = catalogDetails.toUi()
                uiStateListener.updateData { s ->
                    s.copy(
                        categories = categories,
                        banners = banners,
                        uiState = UiState.Success
                    )
                }
            }.onFailure {
                uiStateListener.updateData { s ->
                    s.copy(uiState = UiState.Error)
                }
            }
        }
    }


    fun navigateToSearch() = viewModelScope.launch {
        eventListener.emit(CatalogEvents.GoToSearch)
    }

    fun chooseCategory(catalogCategory: ParentCategoryUi) = viewModelScope.launch {
        if (catalogCategory.childCategories.isNotEmpty()) {
            eventListener.emit(CatalogEvents.GoToSubCategories(catalogCategory))
        } else if (catalogCategory.action != null) {
            eventListener.emit(CatalogEvents.ActivateDataAllAction(catalogCategory.action))
        } else {
            eventListener.emit(CatalogEvents.GoToProductList(catalogCategory))
        }
    }

    fun showAdvertisingBottomSheet(aboutAdvertisingUi: AboutAdvertisingUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentAdvertising = aboutAdvertisingUi,
                showAdvertisingBS = true
            )
        }
    }

    fun closeAdvertisingBottomSheet() {
        uiStateListener.updateData { s ->
            s.copy(showAdvertisingBS = false)
        }
    }

    fun navigateToScanner() = viewModelScope.launch {
        eventListener.emit(CatalogEvents.GoToScanner)
    }

    fun showSpeechRecognizer() = viewModelScope.launch {
        eventListener.emit(CatalogEvents.ShowSpeechRecognizer)
    }

    fun activateBannerAction(banner: BannerUi) = viewModelScope.launch {
        eventListener.emit(CatalogEvents.ActivateVodovozAction(banner.action))
    }

    sealed class CatalogEvents : Event {
        data class GoToSubCategories(val catalogCategory: ParentCategoryUi) : CatalogEvents()
        data class GoToProductList(val catalogCategory: ParentCategoryUi) : CatalogEvents()
        data class ActivateDataAllAction(val action: DataAllAction) : CatalogEvents()
        data class ActivateVodovozAction(val action: VodovozAction) : CatalogEvents()

        data object GoToProfile : CatalogEvents()
        data object GoToSearch : CatalogEvents()
        data object GoToScanner : CatalogEvents()
        data object ShowSpeechRecognizer : CatalogEvents()
    }

    data class CatalogState(
        val categories: List<ParentCategoryUi> = emptyList(),
        val banners: List<BannerUi> = emptyList(),
        val uiState: UiState = UiState.Loading,
        val showAdvertisingBS: Boolean = false,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
    ) : State

    sealed interface UiState {
        data object Loading: UiState
        data object Success : UiState
        data object Error : UiState
    }
}