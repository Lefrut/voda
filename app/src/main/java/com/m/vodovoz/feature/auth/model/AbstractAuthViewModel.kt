package com.m.vodovoz.feature.auth.model

import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.updateCheckbox
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.design_system.model.widgets.withUpdatedSwitch
import com.m.vodovoz.ui.mvi.MviViewModel

abstract class AbstractAuthViewModel<State : AuthState<State>, Event>(
    state: State, private val blockingButtonId: String
) :
    MviViewModel<State, Event>(state), AuthContentOperations {

    companion object {

    }

    override suspend fun listenAuthDetailsChanges() {
        state.collect { s ->
            updateAuthDetails {
                copy(
                    buttons = buttons.updateButton(blockingButtonId) { button ->
                        button.copy(
                            enabled = fields.checkFields()
                                    && s.checkboxes.agreementIsCheckedWhenAvailable(
                                agreementCheckboxId
                            ) && (accountTypeSwitches.any { it.value } || accountTypeSwitches.isEmpty())
                        )
                    }

                )
            }
        }
    }

    protected fun updateAuthDetails(block: AuthDetailsUi.() -> AuthDetailsUi) = updateState {
        it.withAuthDetails(
            authDetails = it.authDetails.block()
        )
    }

    override fun changeField(field: FieldUi, updatedField: FieldUi) {
        updateAuthDetails {
            copy(
                fields = fields.updateFieldAndResetError(
                    field, updatedField
                )
            )
        }

        updateAuthDetails {
            copy(fields = fields.withLastFieldErrorText(""))
        }

    }

    override fun changeCheckbox(checkbox: CheckboxUi, updatedCheckbox: CheckboxUi) {
        updateAuthDetails {
            copy(
                checkboxes = checkboxes.updateCheckbox(
                    checkbox, updatedCheckbox
                )
            )
        }
    }

    override fun changeSwitch(switch: SwitchUi, updatedSwitchUi: SwitchUi) {
        updateAuthDetails {
            copy(
                accountTypeSwitches = accountTypeSwitches.map { it.copy(value = false) }
                    .withUpdatedSwitch(updatedSwitchUi)
            )
        }
    }


    @Suppress("SameParameterValue")
    protected fun List<FieldUi>.withLastFieldErrorText(error: String = ""): List<FieldUi> {
        return lastOrNull()?.let {
            updateField(
                field = it,
                newField = it.copy(
                    supportingText = error,
                    isError = error.isNotBlank()
                )
            )
        } ?: this
    }


}