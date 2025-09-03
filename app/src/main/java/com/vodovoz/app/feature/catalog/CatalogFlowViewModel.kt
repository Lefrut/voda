package com.vodovoz.app.feature.catalog

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.ParentCategoryUi
import com.vodovoz.app.common.model.DataAllAction
import com.vodovoz.app.common.model.VodovozAction
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.catalog.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogFlowViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<CatalogFlowViewModel.CatalogState, CatalogFlowViewModel.CatalogEvents>(
    CatalogState()
) {
    fun refresh() {
        fetchCatalogDetails()
    }

    fun fetchCatalogDetails() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = CatalogUiState.Loading)
        }
        vodovozServiceRepository.getCatalogDetails().collect { catalogDetailsResult ->
            catalogDetailsResult.onSuccess { catalogDetails ->
                val (banners, categories) = catalogDetails.toUi()
                updateState { s ->
                    s.copy(
                        categories = categories,
                        banners = banners,
                        uiState = CatalogUiState.Success
                    )
                }
            }.onFailure {
                updateState { s ->
                    s.copy(uiState = CatalogUiState.Error)
                }
            }
        }
    }


    fun navigateToSearch() = viewModelScope.launch {
        sendEvent(CatalogEvents.GoToSearch)
    }

    fun chooseCategory(catalogCategory: ParentCategoryUi) = viewModelScope.launch {
        if (catalogCategory.childCategories.isNotEmpty()) {
            sendEvent(CatalogEvents.GoToSubCategories(catalogCategory))
        } else if (catalogCategory.action != null) {
            sendEvent(CatalogEvents.ActivateDataAllAction(catalogCategory.action))
        } else {
            sendEvent(CatalogEvents.GoToProductList(catalogCategory))
        }
    }

    fun showAdvertisingBottomSheet(aboutAdvertisingUi: AboutAdvertisingUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentAdvertising = aboutAdvertisingUi,
                showAdvertisingBS = true
            )
        }
    }

    fun closeAdvertisingBottomSheet() {
        updateState { s ->
            s.copy(showAdvertisingBS = false)
        }
    }

    fun navigateToScanner() = viewModelScope.launch {
        sendEvent(CatalogEvents.GoToScanner)
    }

    fun showSpeechRecognizer() = viewModelScope.launch {
        sendEvent(CatalogEvents.ShowSpeechRecognizer)
    }

    fun activateBannerAction(banner: BannerUi) = viewModelScope.launch {
        sendEvent(CatalogEvents.ActivateVodovozAction(banner.action))
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
        val uiState: CatalogUiState = CatalogUiState.Loading,
        val showAdvertisingBS: Boolean = false,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
    ) : State

    @Stable
    sealed interface CatalogUiState {
        data object Loading: CatalogUiState
        data object Success : CatalogUiState
        data object Error : CatalogUiState
    }
}