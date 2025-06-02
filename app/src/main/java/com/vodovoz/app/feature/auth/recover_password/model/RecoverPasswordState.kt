package com.vodovoz.app.feature.auth.recover_password.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.widgets.FieldUi

@Immutable
data class RecoverPasswordState(
    val title: String = "",
    val description: String = "",
    val fields: List<FieldUi> = emptyList(),
    val buttons: List<ColorfulButtonUi> = emptyList(),
    val agreementHtml: String = "",
    val showAgreement: Boolean = false,
    val agreementChecked: Boolean = false,
    val errorText: String = "",
    val uiState: RecoverPasswordUiState = RecoverPasswordUiState.Loading
)