package com.m.vodovoz.feature.write_comment.model

import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface WriteCommentUiState {

    data object Comment: WriteCommentUiState
    data class Success(val placeholder: VodovozPlaceholderUi): WriteCommentUiState

}