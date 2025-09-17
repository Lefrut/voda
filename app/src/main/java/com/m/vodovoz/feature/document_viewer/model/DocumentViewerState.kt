package com.m.vodovoz.feature.document_viewer.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.DocumentUi

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