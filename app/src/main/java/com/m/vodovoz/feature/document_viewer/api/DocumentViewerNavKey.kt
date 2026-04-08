package com.m.vodovoz.feature.document_viewer.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.DocumentUi

data class DocumentViewerNavKey(
    val documentId: DocumentUi,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/document_viewer/DocumentViewer"
    }
}
