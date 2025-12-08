package com.m.vodovoz.feature.document_viewer

import android.graphics.Color
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.top_bar.VodovozTopBar
import com.m.vodovoz.design_system.composables.zoom.ZoomableContainer
import com.m.vodovoz.design_system.composables.zoom.rememberZoomableState
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerState
import com.m.vodovoz.feature.document_viewer.model.DocumentViewerUiState
import com.rajat.pdfviewer.HeaderData
import com.rajat.pdfviewer.PdfRendererView
import com.rajat.pdfviewer.compose.PdfRendererViewCompose
import com.rajat.pdfviewer.util.PdfSource


@Composable
fun DocumentViewerScreen(
    viewModel: DocumentViewerViewModel,
    viewState: DocumentViewerState,
) {
    val document = viewState.currentDocument
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        VodovozTopBar(onBack = { viewModel.navigateBack() }, title = document.description)

        when (document.type) {
            "pdf" -> {
                PdfRendererViewCompose(
                    source = PdfSource.Remote(document.src),
                    headers = HeaderData(emptyMap()),
                    statusCallBack = object : PdfRendererView.StatusCallBack {
                        override fun onPdfLoadSuccess(absolutePath: String) {
                            viewModel.setUiState(DocumentViewerUiState.Success)
                        }
                    },
                    onReady = { pdfView ->
                        runCatching {
                            val clazz = pdfView.javaClass
                            val field = clazz.getDeclaredField("pageNo")
                            field.isAccessible = true
                            val pageNoTextView = field.get(pdfView) as? TextView
                            pageNoTextView?.apply{
                                isVisible = false
                                textSize = 0f
                                setBackgroundColor(Color.TRANSPARENT)
                            }
                        }
                    }

                )
            }

            else -> {
                val painter = rememberAsyncImagePainter(document.src)
                val imageState by painter.state.collectAsStateWithLifecycle()

                if (imageState is AsyncImagePainter.State.Success) {
                    val state = rememberZoomableState(contentSize = painter.intrinsicSize)

                    ZoomableContainer(
                        state = state
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    LaunchedEffect(Unit) {
                        viewModel.setUiState(DocumentViewerUiState.Success)
                    }
                }


            }
        }
    }
    if (viewState.uiState is DocumentViewerUiState.Loading) {
        LoadingPlaceholder()
    }
}
