package com.m.vodovoz.common.webview

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.webview.api.WebViewNavKey
import com.m.vodovoz.common.webview.model.WebViewEvents
import com.m.vodovoz.common.webview.model.WebViewState
import com.m.vodovoz.common.webview.model.WebViewUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = WebViewViewModel.Factory::class)
@Stable
class WebViewViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    @Assisted private val navKey: WebViewNavKey?,
) : MviViewModel<WebViewState, WebViewEvents>(WebViewState()) {

    private val title = navKey?.title ?: savedStateHandle.get<String>("title") ?: navigateBack().run { "" }
    private val url = navKey?.url ?: savedStateHandle.get<String>("url") ?: navigateBack().run { "" }

    init {
        setInitialData(title, url)
    }

    private fun setInitialData(title: String, url: String) = viewModelScope.launch {
        updateState { s -> s.copy(title = title, url = url, showTopBar = title.isNotEmpty()) }
    }

    fun setUiState(uiState: WebViewUiState) = viewModelScope.launch {
        updateState { s -> s.copy(uiState = uiState) }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WebViewEvents.GoBack)
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: WebViewNavKey?): WebViewViewModel
    }
}
