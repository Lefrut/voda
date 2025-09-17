package com.m.vodovoz.feature.cart.gifts

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.feature.cart.gifts.model.GiftsEvent
import com.m.vodovoz.feature.cart.gifts.model.GiftsState
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@Stable
class GiftsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : MviViewModel<GiftsState, GiftsEvent>(GiftsState()) {

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
                gifts = giftDetails.items,
                currentGift = giftDetails.items.firstOrNull() ?: s.currentGift,
                present = present
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(GiftsEvent.GoBack)
    }

    fun chooseGift() = viewModelScope.launch {
        sendEvent(GiftsEvent.GoToCart(stateSnapshot.currentGift))
    }

    fun selectGift(presentItem: CartPresentItemUi) = viewModelScope.launch {
        updateState { s ->
            s.copy(currentGift = presentItem)
        }
    }


}