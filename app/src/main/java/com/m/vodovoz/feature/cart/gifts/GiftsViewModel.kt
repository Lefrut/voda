package com.m.vodovoz.feature.cart.gifts

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.design_system.model.ForAdultsUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.cart.gifts.model.GiftsEvent
import com.m.vodovoz.feature.cart.gifts.model.GiftsState
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.ui.paging.ProductsMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@Stable
class GiftsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ProductsMviViewModel<CartPresentItemUi, GiftsState, GiftsEvent>(
    state = GiftsState(),
    blockedProductsFlow = emptyFlow(),
    favoritesFlow = emptyFlow(),
    cartFlow = emptyFlow(),
    canViewAdultProducts = userPreferencesRepository.canViewAdultProducts
) {

    private val present: CartPresentUi? = savedStateHandle.get<CartPresentUi>("present")

    private val giftDetails: CartPresentPopupWindowUi? =
        savedStateHandle.get<CartPresentPopupWindowUi>("popupWindow")

    init {
        initializeData()
    }

    private fun initializeData() = viewModelScope.launch {
        if (giftDetails == null) {
            navigateBack()
            return@launch
        }

        updateState { s ->
            s.copy(
                button = giftDetails.button,
                items = giftDetails.items,
                currentGift = giftDetails.items.firstOrNull() ?: s.currentGift,
                present = present
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(GiftsEvent.GoBack)
    }

    fun tryToChooseGift() = viewModelScope.launch {
        val forAdults = stateSnapshot.currentGift.forAdults
        if (forAdults != null) {
            showForAdultsDialog(forAdults)
        } else {
            navigateToCart()
        }
    }

    private fun showForAdultsDialog(forAdults: ForAdultsUi) {
        updateState { s ->
            s.copy(showForAdultsDialog = true, forAdultsDialog = forAdults)
        }
    }

    fun closeForAdultsDialog() {
        updateState { s ->
            s.copy(showForAdultsDialog = false)
        }
    }


    fun selectGift(presentItem: CartPresentItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(currentGift = presentItem)
        }
    }

    fun acceptForAdults() = viewModelScope.launch {
        userPreferencesRepository.setCanViewAdultProducts(true)
        navigateToCart()
    }

    private suspend fun navigateToCart() {
        sendEvent(GiftsEvent.GoToCart(stateSnapshot.currentGift))
    }

    fun showPreviewImageDialog(image: String) {
        updateState { s ->
            s.copy(previewImage = image)
        }
    }

    fun closePreviewImageDialog() {
        updateState { state ->
            state.copy(previewImage = null)
        }
    }


}