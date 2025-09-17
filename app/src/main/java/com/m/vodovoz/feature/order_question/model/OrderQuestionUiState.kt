package com.m.vodovoz.feature.order_question.model

import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface OrderQuestionUiState {

    data object Loading : OrderQuestionUiState
    data object Fields : OrderQuestionUiState
    data class Success(
        val placeholderData: VodovozPlaceholderUi,
    ) : OrderQuestionUiState

    data object Error : OrderQuestionUiState

}