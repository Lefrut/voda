package com.m.vodovoz.feature.auth.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.EmailValidator
import com.m.vodovoz.design_system.model.widgets.EmptyTextValidator
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.NameValidator
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.PhoneNumberValidator
import com.m.vodovoz.design_system.model.widgets.mapToUi
import com.m.vodovoz.domain.general.model.user.AuthDetailsModel
import com.m.vodovoz.ui.mvi.State

@Immutable
data class AuthDetailsUi(
    val title: String,
    val description: String,
    val fields: List<FieldUi>,
    val buttons: List<ColorfulButtonUi>,
    val agreementCheckboxId: String?,
    val checkboxes: List<CheckboxUi>,
    val warning: String = "",
    val waringTitles: List<String> = emptyList()
) {

    companion object {
        val Empty = AuthDetailsUi(
            title = "",
            description = "",
            fields = emptyList(),
            agreementCheckboxId = null,
            buttons = emptyList(),
            checkboxes = emptyList()
        )
    }


}

@Stable
abstract class AuthState(
    open val authDetails: AuthDetailsUi,
) : State {

    val fields get() = authDetails.fields
    val buttons get() = authDetails.buttons
    val checkboxes get() = authDetails.checkboxes
}

fun AuthDetailsUi.agreementIsCheckedWhenAvailable(): Boolean {
    val agreementChecked = checkboxes.firstOrNull { it.id == agreementCheckboxId }?.checked
    return agreementChecked == true || agreementChecked == null
}


fun List<CheckboxUi>.agreementIsCheckedWhenAvailable(agreementId: String?): Boolean {
    val agreementChecked = firstOrNull { it.id == agreementId }?.checked
    return agreementChecked == true || agreementChecked == null
}

fun AuthDetailsUi.Companion.authValidators() = listOf(
    NoRequiredValidator,
    NameValidator,
    PhoneNumberValidator,
    EmailValidator,
    EmptyTextValidator
)

fun AuthDetailsModel.toUi(): AuthDetailsUi {

    return AuthDetailsUi(
        title = title,
        description = description,
        fields = fields.mapToUi(),
        buttons = buttons.mapToUi(),
        checkboxes = checkboxes.mapToUi(),
        agreementCheckboxId = agreementCheckboxId
    )
}