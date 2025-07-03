package com.vodovoz.app.feature.document_viewer.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.DocumentUi

@Immutable
data class DocumentViewerState(
    val currentDocument: DocumentUi = DocumentUi("", 0f, "", "", "", ""),
    val uiState: DocumentViewerUiState = DocumentViewerUiState.Loading,
)

@Stable
sealed interface DocumentViewerUiState {
    data object Loading : DocumentViewerUiState
    data object Success : DocumentViewerUiState
}