package com.vodovoz.app.feature.write_message.model

import androidx.compose.runtime.Stable
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi

@Stable
sealed interface WriteMessageUiState {

    data object Loading : WriteMessageUiState
    data object Body : WriteMessageUiState
    data class Success(val placeholder: VodovozPlaceholderUi) : WriteMessageUiState
    data object Error : WriteMessageUiState


}