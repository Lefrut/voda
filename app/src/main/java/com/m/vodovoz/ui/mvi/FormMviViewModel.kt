package com.m.vodovoz.ui.mvi

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.design_system.model.widgets.vodovozValidators
import com.m.vodovoz.feature.preorder.model.FormUi
import kotlinx.coroutines.launch

abstract class FormMviViewModel<S : FormState, E>(
    state: S,
) : MviViewModel<S, E>(state) {

    protected fun updateForm(block: FormUi.() -> FormUi) {
        updateState { s -> s.withForm(form = s.form.block()) }
    }

    abstract fun S.withForm(form: FormUi): S

    fun changeFieldValue(field: FieldUi, newValue: String) = viewModelScope.launch {
        updateForm {
            val fieldIndex = fields.indexOfFirst { field.id == it.id }
            copy(
                fields = fields.toMutableList().apply {
                    set(fieldIndex, field.copy(value = newValue))
                }.map { it.copy(isError = false) }
            )
        }
    }

    fun changeCheckbox(checked: Boolean) {
        updateForm {
            copy(
                checkbox = checkbox?.copy(
                    checked = checked,
                    error = false
                )
            )
        }
    }

    protected fun validateWidgets(getString: (resId: Int) -> String): Boolean {
        val form = stateSnapshot.form
        val fields = form.fields


        val isValidCheckbox = form.checkbox?.run {
            !isRequired || checked
        } ?: true

        return fields.checkFields(
            putErrors = true,
            validators = vodovozValidators,
            getSupportingText = { f ->
                f.getErrorText { id -> getString(id) }
            }
        ) { updatedFields, _ ->
            updateForm {
                copy(
                    fields = updatedFields,
                    checkbox = checkbox?.copy(error = !isValidCheckbox)
                )
            }
        } && isValidCheckbox
    }


}


abstract class FormState {
    abstract val form: FormUi
}