package com.vodovoz.app.feature.product_details.detail_media

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.ProductMediaUi
import com.vodovoz.app.feature.product_details.detail_media.model.DetailMediaEvent
import com.vodovoz.app.feature.product_details.detail_media.model.DetailMediaState
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class DetailMediaViewModel @Inject constructor(
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
        _state.update { s ->
            s.copy(
                currentMedia = s.mediaList.getOrElse(index){ s.currentMedia }
            )
        }
    }

    fun makePortrait() = viewModelScope.launch {
        _state.update{ s -> s.copy(portraitOrientation = true) }
        sendEvent(DetailMediaEvent.MakePortrait)
    }

    fun makeLandscape() = viewModelScope.launch {
        _state.update{ s -> s.copy(portraitOrientation = false) }
        sendEvent(DetailMediaEvent.MakeLandscape)
    }

}