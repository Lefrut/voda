package com.m.vodovoz.feature.document_viewer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.design_system.model.DocumentUi
import com.m.vodovoz.feature.document_viewer.api.DocumentViewerNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DocumentViewerFragment @Inject constructor() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                DocumentViewerEntry(DocumentViewerNavKey(documentId = args.get("documentId") as DocumentUi))
            }
        }
    }
}
