package com.m.vodovoz.feature.document_viewer

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerEvent
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerState
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class DocumentViewerViewModel @Inject constructor(
    val tabManager: TabManager,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<DocumentViewerState, DocumentViewerEvent>(DocumentViewerState()) {

    init {
        savedStateHandle.get<DocumentUi>("documentId")?.let { document ->
            setDocument(document)
        }
    }

    fun setDocument(document: DocumentUi) {
        updateState { s ->
            s.copy(
                currentDocument = document
            )
        }
    }

    fun setUiState(uiState: DocumentViewerUiState.Success) = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = uiState)
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(DocumentViewerEvent.GoBack)
    }

}
