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
)

interface OptionComponentUi {

    val options: List<ComponentOptionUi>

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
    override val options: List<ComponentOptionUi>,
    val conditions: List<ConditionUi>,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error), OptionComponentUi

@Immutable
data class ConditionUi(
    val id: String,
    val text: String,
    val url: String,
)

@Immutable
data class ToggleListUi(
    override val id: String,
    val label: String,
    override val options: List<ComponentOptionUi>,
    val isRequired: Boolean,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error), OptionComponentUi


@Immutable
data class CheckboxListUi(
    override val id: String,
    val label: String,
    override val options: List<ComponentOptionUi>,
    val isRequired: Boolean,
    override val error: Boolean = false,
) : QuestionnaireComponentUi(id, error), OptionComponentUi

fun CheckboxListUi.update(option: ComponentOptionUi): CheckboxListUi {
    val updatedOptions = options.map { opt ->
        if (opt.label == option.label) opt.copy(value = option.value)
        else opt
    }
    return copy(options = updatedOptions, error = false)
}

fun ConditionsCheckboxListUi.update(option: ComponentOptionUi): ConditionsCheckboxListUi {
    val updatedOptions = options.map { opt ->
        if (opt.label == option.label) opt.copy(value = option.value)
        else opt
    }
    return copy(options = updatedOptions, error = false)
}

fun ToggleListUi.update(option: ComponentOptionUi): ToggleListUi {
    val updatedOptions = options.map { opt ->
        opt.copy(value = (opt.label == option.label))
    }
    return copy(options = updatedOptions, error = false)
}




@Immutable
data class ComponentOptionUi(
    val label: String,
    val value: Boolean,
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
                options = values.map { ComponentOptionUi(it, false) },
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
                options = values.map { ComponentOptionUi(it, false) },
                isRequired = required
            )
        }

        values.size > 2 && !multiple -> {
            ToggleListUi(
                id = code,
                label = name,
                options = values.map { ComponentOptionUi(it, it == value) },
                isRequired = required
            )
        }

        else -> null
    }
}

fun List<QuestionnaireComponentUi>.applyOtherVisibilityRules(): List<QuestionnaireComponentUi> =
    mapIndexed { index, component ->
        if (component is FieldComponentUi && component.id.contains("DRUGOE", ignoreCase = true)) {

            val isOtherOptionSelected = runCatching {
                val prev = getOrElse(index - 1) { component } as OptionComponentUi
                prev.options.any {
                    it.value && it.label.contains("другое", ignoreCase = true)
                }
            }.getOrElse { true }

            component.copy(
                ui = component.ui.copy(
                    isVisible = isOtherOptionSelected
                )
            )
        } else {
            component
        }
    }


fun QuestionnaireComponentUi.errorIfInvalid(
    getString: (Int) -> String,
): QuestionnaireComponentUi {
    val isInvalidAndErrorComponent = when (this) {
        is CheckboxListUi -> {
            options.none { it.value } to copy(error = true)
        }

        is ConditionsCheckboxListUi -> {
            options.any { !it.value } to copy(error = true)
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
            options.none { it.value } to copy(error = true)
        }
    }

    return if (isInvalidAndErrorComponent.first) {
        isInvalidAndErrorComponent.second
    } else this
}


inline fun <reified T : QuestionnaireComponentUi> QuestionnaireComponentUi.updateIfSame(
    id: String,
    onSame: T.() -> T,
): QuestionnaireComponentUi {
    return if (this is T && this.id == id) {
        onSame()
    } else this
}

inline fun <reified T : QuestionnaireComponentUi, reified T2 : QuestionnaireComponentUi> QuestionnaireComponentUi.updateIfSame(
    id: String,
    onSame1: T.() -> T,
    onSame2: T2.() -> T2,
): QuestionnaireComponentUi {
    val initial = this

    listOf(
        updateIfSame<T>(id, onSame1),
        updateIfSame<T2>(id, onSame2)
    ).forEach { updatedComponent ->
        if (updatedComponent != initial) return updatedComponent
    }
    return initial
}

inline fun <reified T : QuestionnaireComponentUi, reified T2 : QuestionnaireComponentUi, reified T3 : QuestionnaireComponentUi> QuestionnaireComponentUi.updateIfSame(
    id: String,
    onSame1: T.() -> T,
    onSame2: T2.() -> T2,
    onSame3: T3.() -> T3,
): QuestionnaireComponentUi {
    val initial = this

    listOf(
        updateIfSame<T>(id, onSame1),
        updateIfSame<T2>(id, onSame2),
        updateIfSame<T3>(id, onSame3)
    ).forEach { updatedComponent ->
        if (updatedComponent != initial) return updatedComponent
    }
    return initial
}

