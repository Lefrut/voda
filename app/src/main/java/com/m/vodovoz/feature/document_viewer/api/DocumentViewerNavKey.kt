package com.m.vodovoz.feature.document_viewer.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object DocumentViewerNavKey : NavKey {
    const val NAV_NAME: String = "feature/document_viewer/DocumentViewer"
}
