package com.m.vodovoz.feature.document_viewer.model

import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.feature.about_product.model.AboutProductEvent

sealed class DocumentViewerEvent {
    data object GoBack : DocumentViewerEvent()
}