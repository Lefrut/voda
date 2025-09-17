package com.m.vodovoz.feature.write_comment.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Immutable
data class WriteCommentState(
    val takePhotos: Boolean = false,
    val productName: String = "",
    val productImage: String = "",
    val rating: Int = 0,
    val buttonIsLoading: Boolean = false,
    val field: FieldUi = FieldUi.Empty,
    val imagesUri: List<String> = emptyList(),
    val uiState: WriteCommentUiState = WriteCommentUiState.Comment
)