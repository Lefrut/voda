package com.vodovoz.app.feature.write_comment.model

import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface WriteCommentUiState {

    data object Comment: WriteCommentUiState
    data class Success(val placeholder: VodovozPlaceholderUi): WriteCommentUiState

}