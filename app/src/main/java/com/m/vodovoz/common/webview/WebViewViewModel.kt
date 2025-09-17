package com.m.vodovoz.common.webview

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.webview.model.WebViewEvents
import com.m.vodovoz.common.webview.model.WebViewState
import com.m.vodovoz.common.webview.model.WebViewUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class WebViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : MviViewModel<WebViewState, WebViewEvents>(WebViewState()) {

    private val title = savedStateHandle.get<String>("title") ?: navigateBack().run { "" }
    private val url = savedStateHandle.get<String>("url") ?: navigateBack().run { "" }

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

}