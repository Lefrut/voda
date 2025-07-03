package com.vodovoz.app.feature.document_viewer

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.design_system.model.DocumentUi
import com.vodovoz.app.feature.document_viewer.model.DocumentViewerEvent
import com.vodovoz.app.feature.document_viewer.model.DocumentViewerState
import com.vodovoz.app.feature.document_viewer.model.DocumentViewerUiState
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class DocumentViewerViewModel @Inject constructor() : MviViewModel<DocumentViewerState, DocumentViewerEvent>(DocumentViewerState()) {

    fun setDocument(document: DocumentUi) {
        _state.update { s ->
            s.copy(
                currentDocument = document
            )
        }
    }

    fun setUiState(uiState: DocumentViewerUiState.Success) = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = uiState)
        }
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(DocumentViewerEvent.GoBack)
    }

}