package com.m.vodovoz.feature.product_details.detail_media

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.ProductMediaUi
import com.m.vodovoz.feature.product_details.detail_media.api.DetailMediaNavKey
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaEvent
import com.m.vodovoz.feature.product_details.detail_media.model.DetailMediaState
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DetailMediaViewModel.Factory::class)
@Stable
class DetailMediaViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    val insetsVisibilityState: InsetsVisibilityState,
    savedStateHandle: SavedStateHandle,
    @Assisted private val navKey: DetailMediaNavKey?,
) : MviViewModel<DetailMediaState, DetailMediaEvent>(
    DetailMediaState(
        currentMedia = navKey?.media ?: savedStateHandle["media"] ?: ProductMediaUi.Picture(""),
        mediaList = navKey?.mediaList ?: savedStateHandle["mediaList"] ?: emptyList()
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

    @AssistedFactory
    interface Factory {
        fun create(navKey: DetailMediaNavKey?): DetailMediaViewModel
    }

}
