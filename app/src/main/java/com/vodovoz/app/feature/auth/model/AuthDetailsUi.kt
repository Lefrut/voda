package com.vodovoz.app.feature.auth.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.vodovoz.app.common.content.State
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.widgets.CheckboxUi
import com.vodovoz.app.design_system.model.widgets.EmailValidator
import com.vodovoz.app.design_system.model.widgets.EmptyTextValidator
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.NameValidator
import com.vodovoz.app.design_system.model.widgets.NoRequiredValidator
import com.vodovoz.app.design_system.model.widgets.PhoneNumberValidator
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.domain.general.model.user.AuthDetailsModel
import com.vodovoz.app.feature.auth.model.AuthDetailsUi.Companion.AGREEMENT_CHECKBOX_ID

@Immutable
data class AuthDetailsUi(
    val title: String,
    val description: String,
    val fields: List<FieldUi>,
    val buttons: List<ColorfulButtonUi>,
    val checkboxes: List<CheckboxUi>,
) {
    companion object {
        val Empty = AuthDetailsUi(
            title = "",
            description = "",
            fields = emptyList(),
            buttons = emptyList(),
            checkboxes = emptyList()
        )

        const val AGREEMENT_CHECKBOX_ID = "terms_and_policy"

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

fun List<CheckboxUi>.agreementIsCheckedWhenAvailable(): Boolean {
    val agreementChecked = firstOrNull { it.id == AGREEMENT_CHECKBOX_ID }?.checked
    return agreementChecked == true || agreementChecked == null
}

fun AuthDetailsUi.Companion.authValidators() = listOf(
    NoRequiredValidator,
    NameValidator,
    PhoneNumberValidator,
    EmailValidator,
    EmptyTextValidator
)

fun AuthDetailsModel.toUi(agreement: String): AuthDetailsUi {

    val agreementCheckbox = CheckboxUi(
        name = agreement,
        id = AGREEMENT_CHECKBOX_ID,
        checked = agreementChecked ?: false,
        isRequired = true
    )

    return AuthDetailsUi(
        title = title,
        description = description,
        fields = fields.mapToUi(),
        buttons = buttons.mapToUi(),
        checkboxes = buildList {
            if (agreement.isNotBlank() && agreementChecked != null) {
                add(agreementCheckbox)
            }
            addAll(checkboxes.mapToUi())
        }
    )
}