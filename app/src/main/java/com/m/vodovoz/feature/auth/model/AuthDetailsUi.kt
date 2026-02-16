package com.m.vodovoz.feature.auth.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.m.vodovoz.R
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.mapToUi
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.EmailValidator
import com.m.vodovoz.design_system.model.widgets.EmptyTextValidator
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.NameValidator
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.PhoneNumberValidator
import com.m.vodovoz.design_system.model.widgets.SwitchUi
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
    val waringTitles: List<String> = emptyList(),
    val accountTypeSwitches: List<SwitchUi> = emptyList(),
    val showForgotPassword: Boolean = false
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

enum class AccountTypeSwtichInfo(
    val id: String,
    @field:StringRes
    val nameId: Int,
    val value: Boolean = false
) {
    Individual(
        id = "1",
        nameId = R.string.personal
    ),
    Commercial(
        id = "2",
        nameId = R.string.for_buisnes
    )
}


fun List<AccountTypeSwtichInfo>.toSwitches(getStringResource: (Int) -> String): List<SwitchUi> {
    return map {
        SwitchUi(
            id = it.id,
            name = getStringResource(it.nameId),
            value = it.value,
            enabled = true
        )
    }
}

fun AuthDetailsUi.selectedAccountTypeId(): String {
    return accountTypeSwitches.firstOrNull { it.value }?.id.orEmpty()
}

fun AuthDetailsUi.withAccountTypeSelection(selectedAccountTypeId: String?): AuthDetailsUi {
    return copy(
        accountTypeSwitches = accountTypeSwitches.withAccountTypeSelection(selectedAccountTypeId)
    )
}

fun List<SwitchUi>.withAccountTypeSelection(selectedAccountTypeId: String?): List<SwitchUi> {
    return when {
        selectedAccountTypeId == null -> this
        selectedAccountTypeId.isBlank() -> map { switch ->
            switch.copy(value = false)
        }

        else -> map { switch ->
            switch.copy(value = switch.id == selectedAccountTypeId)
        }
    }
}

@Stable
abstract class AuthState<S : AuthState<S>>(
    open val authDetails: AuthDetailsUi,
) : State {

    val fields get() = authDetails.fields
    val buttons get() = authDetails.buttons
    val checkboxes get() = authDetails.checkboxes


    abstract fun withAuthDetails(authDetails: AuthDetailsUi): S

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
