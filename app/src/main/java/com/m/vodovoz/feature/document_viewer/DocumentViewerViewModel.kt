package com.m.vodovoz.feature.document_viewer

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.feature.document_viewer.api.DocumentViewerNavKey
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerEvent
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerState
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = DocumentViewerViewModel.Factory::class)
@Stable
class DocumentViewerViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    @Assisted private val navKey: DocumentViewerNavKey,
) : MviViewModel<DocumentViewerState, DocumentViewerEvent>(DocumentViewerState()) {

    init {
        setDocument(navKey.documentId)
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

    @AssistedFactory
    interface Factory {
        fun create(navKey: DocumentViewerNavKey): DocumentViewerViewModel
    }
}
