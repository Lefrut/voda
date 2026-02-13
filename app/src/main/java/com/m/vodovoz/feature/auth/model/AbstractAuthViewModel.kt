package com.m.vodovoz.feature.auth.model

import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.updateButton
import com.m.vodovoz.design_system.model.widgets.CheckboxUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.FieldValidator
import com.m.vodovoz.design_system.model.widgets.SwitchUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.updateCheckbox
import com.m.vodovoz.design_system.model.widgets.updateField
import com.m.vodovoz.design_system.model.widgets.updateFieldAndResetError
import com.m.vodovoz.design_system.model.widgets.withUpdatedSwitch
import com.m.vodovoz.ui.mvi.MviViewModel

abstract class AbstractAuthViewModel<State : AuthState<State>, Event>(
    state: State, private val blockingButtonId: String
) : MviViewModel<State, Event>(state), AuthContentOperations {

    private data class BlockingButtonInputSignature(
        val fields: List<Pair<String, String>>,
        val checkboxes: List<Pair<String, Boolean>>,
        val accountTypeSwitches: List<Pair<String, Boolean>>,
        val agreementCheckboxId: String?,
    )

    protected open val blockingButtonValidators: List<FieldValidator>? = null
    protected open val requireSelectedAccountTypeForBlockingButton: Boolean = true
    protected open val recalculateBlockingButtonOnlyOnInputChanges: Boolean = true

    override suspend fun listenAuthDetailsChanges() {
        var lastInputSignature: BlockingButtonInputSignature? = null

        state.collect { currentState ->
            val currentInputSignature = currentState.authDetails.blockingButtonInputSignature()
            val shouldRecalculate = !recalculateBlockingButtonOnlyOnInputChanges
                    || currentInputSignature != lastInputSignature

            if (!shouldRecalculate) return@collect
            lastInputSignature = currentInputSignature

            updateBlockingButton { button ->
                button.copy(
                    enabled = currentState.authDetails.isBlockingButtonEnabled(
                        validators = blockingButtonValidators,
                        requireSelectedAccountType = requireSelectedAccountTypeForBlockingButton
                    )
                )
            }
        }
    }

    protected fun updateAuthDetails(block: AuthDetailsUi.() -> AuthDetailsUi) = updateState {
        it.withAuthDetails(
            authDetails = it.authDetails.block()
        )
    }

    protected fun updateBlockingButton(block: (ColorfulButtonUi) -> ColorfulButtonUi) {
        updateAuthDetails {
            withBlockingButton(block)
        }
    }

    protected fun setBlockingButtonState(enabled: Boolean? = null, loading: Boolean? = null) {
        if (enabled == null && loading == null) return

        updateBlockingButton { button ->
            button.copy(
                enabled = enabled ?: button.enabled,
                loading = loading ?: button.loading
            )
        }
    }

    protected fun setLastFieldError(error: String) {
        updateAuthDetails {
            copy(fields = fields.withLastFieldErrorText(error))
        }
    }

    protected fun AuthDetailsUi.withBlockingButton(
        block: (ColorfulButtonUi) -> ColorfulButtonUi,
    ): AuthDetailsUi {
        return copy(buttons = buttons.updateButton(blockingButtonId, block))
    }

    protected fun AuthDetailsUi.isBlockingButtonEnabled(
        validators: List<FieldValidator>? = null,
        fields: List<FieldUi> = this.fields,
        checkboxes: List<CheckboxUi> = this.checkboxes,
        requireSelectedAccountType: Boolean = false,
    ): Boolean {
        val fieldsAreValid = validators?.let { fields.checkFields(validators = it) }
            ?: fields.checkFields()
        val agreementChecked = checkboxes.agreementIsCheckedWhenAvailable(agreementCheckboxId)
        val accountTypeSelected = !requireSelectedAccountType
                || accountTypeSwitches.any { it.value }
                || accountTypeSwitches.isEmpty()

        return fieldsAreValid && agreementChecked && accountTypeSelected
    }

    private fun AuthDetailsUi.blockingButtonInputSignature(): BlockingButtonInputSignature {
        return BlockingButtonInputSignature(
            fields = fields.map { field -> field.id to field.value },
            checkboxes = checkboxes.map { checkbox -> checkbox.id to checkbox.checked },
            accountTypeSwitches = accountTypeSwitches.map { switch -> switch.id to switch.value },
            agreementCheckboxId = agreementCheckboxId
        )
    }

    override fun changeField(field: FieldUi, updatedField: FieldUi) {
        updateAuthDetails {
            copy(
                fields = fields.updateFieldAndResetError(
                    field, updatedField
                ).withLastFieldErrorText("")
            )
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

    override fun changeSwitch(switch: SwitchUi, updatedSwitch: SwitchUi) {
        updateAuthDetails {
            copy(
                accountTypeSwitches = accountTypeSwitches.map { it.copy(value = false) }
                    .withUpdatedSwitch(updatedSwitch)
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
