package com.m.vodovoz.feature.product_details.detail_media

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ProductMediaUi
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaEvent
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaState
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class DetailMediaViewModel @Inject constructor(
    val tabManager: TabManager,
    val insetsVisibilityState: InsetsVisibilityState,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<DetailMediaState, DetailMediaEvent>(
    DetailMediaState(
        currentMedia = savedStateHandle["media"] ?: ProductMediaUi.Picture(""),
        mediaList = savedStateHandle["mediaList"] ?: emptyList()
    )
){

    fun navigateBack() = viewModelScope.launch {
        sendEvent(DetailMediaEvent.GoBack)
    }

    fun setMediaByIndex(index: Int) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                currentMedia = s.mediaList.getOrElse(index){ s.currentMedia }
            )
        }
    }

    fun makePortrait() = viewModelScope.launch {
        updateState{ s -> s.copy(portraitOrientation = true) }
        sendEvent(DetailMediaEvent.MakePortrait)
    }

    fun makeLandscape() = viewModelScope.launch {
        updateState{ s -> s.copy(portraitOrientation = false) }
        sendEvent(DetailMediaEvent.MakeLandscape)
    }

}
