package com.m.vodovoz.feature.certificate_activation.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.widgets.FieldUi

@Immutable
data class CertificateActivationState(
    val uiState: CertificateActivationUiState = CertificateActivationUiState.Loading,
    val title: String = "",
    val field: FieldUi = FieldUi.Empty,
    val activationButton: ColorfulButtonUi = ColorfulButtonUi.Empty,
    val activationButtonEnabled: Boolean = false,
    val activationButtonIsLoading: Boolean = false,
    val descriptionHtml: String = "",
    val secondDescriptionHtml: String = ""
)
