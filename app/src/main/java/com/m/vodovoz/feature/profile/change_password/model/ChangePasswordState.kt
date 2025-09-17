package com.m.vodovoz.feature.profile.change_password.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Immutable
data class ChangePasswordState(
    val fields: List<FieldUi> = emptyList(),
    val title: String = "",
    val uiState: ChangePasswordUiState = ChangePasswordUiState.Loading,
    val buttonEnabled: Boolean = false,
    val buttonLoading: Boolean = false
)
