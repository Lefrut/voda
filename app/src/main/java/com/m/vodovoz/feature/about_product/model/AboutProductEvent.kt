package com.m.vodovoz.feature.about_product.model

import com.m.vodovoz.design_system.model.DocumentUi

sealed class AboutProductEvent {
    data class GoToDocumentViewer(val document: DocumentUi) : AboutProductEvent() {

    }

    data class GoToProductAnalogs(val productId: Long) : AboutProductEvent()

    data object GoBack: AboutProductEvent()

}