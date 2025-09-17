package com.m.vodovoz.feature.questionnaires.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.design_system.model.widgets.toUi
import com.m.vodovoz.design_system.model.widgets.vodovozValidators
import com.m.vodovoz.domain.general.model.user.ConditionModel
import com.m.vodovoz.domain.general.model.user.QuestionnairesItemModel
import com.m.vodovoz.domain.general.model.user.toFieldModel

@Immutable
sealed class QuestionnaireComponentUi(
    open val id: String,
    open val error: Boolean,
) {
}

@Immutable
data class FieldComponentUi(
    val ui: FieldUi,
) : QuestionnaireComponentUi(ui.id, ui.isError)

@Immutable
data class SwitchUi(
    override val id: String,
    val label: String,
    val options: List<String>,
    val selectedOption: String,
    val isRequired: Boolean,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error)

@Immutable
data class ConditionsCheckboxListUi(
    override val id: String,
    val label: String,
    val options: List<CheckOption>,
    val conditions: List<ConditionUi>,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error)

@Immutable
data class ConditionUi(
    val id: String,
    val text: String,
    val url: String,
)

@Immutable
data class CheckboxListUi(
    override val id: String,
    val label: String,
    val options: List<CheckOption>,
    val isRequired: Boolean,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error)

@Immutable
data class ToggleListUi(
    override val id: String,
    val label: String,
    val options: List<ToggleOption>,
    val isRequired: Boolean,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error)

@Immutable
data class CheckOption(
    val label: String,
    val isChecked: Boolean,
)

@Immutable
data class ToggleOption(
    val label: String,
    val isSelected: Boolean,
)

fun ConditionModel.toUi(): ConditionUi {
    return ConditionUi(
        id = id,
        text = text,
        url = url
    )
}

fun QuestionnairesItemModel.toUi(): QuestionnaireComponentUi? {
    return when {
        type.lowercase() == "text" -> {
            FieldComponentUi(toFieldModel().toUi())
        }

        conditions.isNotEmpty() -> {
            ConditionsCheckboxListUi(
                id = code,
                label = name,
                options = values.map { CheckOption(it, false) },
                conditions = conditions.map { it.toUi() },
            )
        }

        values.size == 2 && !multiple -> {
            SwitchUi(
                id = code,
                label = name,
                options = values,
                selectedOption = value,
                isRequired = required
            )
        }

        multiple -> {
            CheckboxListUi(
                id = code,
                label = name,
                options = values.map { CheckOption(it, false) },
                isRequired = required
            )
        }

        values.size > 2 && !multiple -> {
            ToggleListUi(
                id = code,
                label = name,
                options = values.map { ToggleOption(it, it == value) },
                isRequired = required
            )
        }

        else -> null
    }
}

fun QuestionnaireComponentUi.errorIfInvalid(
    getString: (Int) -> String,
): QuestionnaireComponentUi {
    val isInvalidAndErrorComponent = when (this) {
        is CheckboxListUi -> {
            options.none { it.isChecked } to copy(error = true)
        }

        is ConditionsCheckboxListUi -> {
            options.any { !it.isChecked } to copy(error = true)
        }

        is FieldComponentUi -> {
            !listOf(ui).checkFields(validators = vodovozValidators) to copy(
                ui = ui.copy(
                    isError = true,
                    supportingText = ui.getErrorText {
                        getString(it)
                    }
                )

            )

        }

        is SwitchUi -> {
            (selectedOption !in options) to copy(error = true)
        }

        is ToggleListUi -> {
            options.none { it.isSelected } to copy(error = true)
        }
    }

    return if (isInvalidAndErrorComponent.first) {
        isInvalidAndErrorComponent.second
    } else this
}


inline fun <reified T : QuestionnaireComponentUi> QuestionnaireComponentUi.ifSame(
    id: String,
    onSame: T.() -> QuestionnaireComponentUi,
): QuestionnaireComponentUi {
    return if (this is T && this.id == id) {
        onSame()
    } else this
}
