package com.m.vodovoz.feature.document_viewer.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.DocumentUi

data class DocumentViewerNavKey(
    val documentId: DocumentUi,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/document_viewer/DocumentViewer"
    }
}
