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
sealed interface QuizComponentUi {
    val id: String
    val error: Boolean
    val label: String

    fun updateByValidation(
        warnings: QuizWarningsVisitor,
    ): QuizComponentUi

    fun value(): String
}

class QuizWarningsVisitor(
    val getString: (Int) -> String,
) {
    fun fieldWarning(resId: Int) = getString(resId)
}

@Immutable
sealed interface QuizOptionComponentUi : QuizComponentUi {
    val options: List<ComponentOptionUi>

    fun withOptions(updatedOptions: List<ComponentOptionUi>): QuizOptionComponentUi = this

    override fun value(): String {
        return options.filter { it.value }.joinToString(",") { it.label }
    }
}

fun <T : QuizOptionComponentUi> T.withOption(
    option: ComponentOptionUi,
): T {
    val updatedOptions = options.map { opt ->
        if (opt.label == option.label) opt.copy(value = option.value)
        else opt
    }
    @Suppress("UNCHECKED_CAST")
    return withOptions(updatedOptions) as T
}


@Immutable
data class FieldComponentUi(
    val ui: FieldUi,
) : QuizComponentUi {
    override val id: String = ui.id
    override val error: Boolean = ui.isError
    override val label: String = ui.label

    override fun updateByValidation(warnings: QuizWarningsVisitor): QuizComponentUi {
        return if (listOf(ui).checkFields(validators = vodovozValidators)) {
            this
        } else copy(
            ui = ui.copy(
                isError = true,
                supportingText = ui.getErrorText {
                    warnings.fieldWarning(it)
                }
            )
        )
    }

    override fun value(): String = ui.value
}

@Immutable
data class SwitchUi(
    override val id: String,
    override val label: String,
    override val error: Boolean = false,
    val options: List<String>,
    val selectedOption: String,
    val isRequired: Boolean,
) : QuizComponentUi {
    override fun updateByValidation(warnings: QuizWarningsVisitor): QuizComponentUi {
        return if (selectedOption !in options) {
            copy(error = true)
        } else this
    }

    override fun value(): String = selectedOption
}

@Immutable
data class ConditionsCheckboxListUi(
    override val id: String,
    override val options: List<ComponentOptionUi>,
    override val error: Boolean = false,
    override val label: String,
    val conditions: List<ConditionUi>,
) : QuizOptionComponentUi {

    override fun updateByValidation(warnings: QuizWarningsVisitor): QuizComponentUi {
        return if (options.any { !it.value }) {
            copy(error = true)
        } else this

    }

    override fun withOptions(updatedOptions: List<ComponentOptionUi>): QuizOptionComponentUi {
        return copy(options = updatedOptions)
    }
}

@Immutable
data class ConditionUi(
    val id: String,
    val text: String,
    val url: String,
)

@Immutable
data class ToggleListUi(
    override val error: Boolean = false,
    override val options: List<ComponentOptionUi>,
    override val id: String,
    override val label: String,
    val isRequired: Boolean,
) : QuizOptionComponentUi {

    override fun updateByValidation(warnings: QuizWarningsVisitor): QuizComponentUi {
        return if (options.none { it.value }) {
            copy(error = true)
        } else this
    }

    override fun withOptions(updatedOptions: List<ComponentOptionUi>): QuizOptionComponentUi {
        return copy(options = updatedOptions, error = false)
    }
}

@Immutable
data class CheckboxListUi(
    override val id: String,
    override val options: List<ComponentOptionUi>,
    override val error: Boolean = false,
    override val label: String,
    val isRequired: Boolean,
) : QuizOptionComponentUi {

    override fun updateByValidation(warnings: QuizWarningsVisitor): QuizComponentUi {
        return if (options.none { it.value }) {
            copy(error = true)
        } else this
    }

    override fun withOptions(updatedOptions: List<ComponentOptionUi>): QuizOptionComponentUi {
        return copy(options = updatedOptions, error = false)
    }
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

fun QuestionnairesItemModel.toUi(): QuizComponentUi? {
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

fun List<QuizComponentUi>.applyOtherVisibilityRules(): List<QuizComponentUi> =
    mapIndexed { index, component ->
        if (component is FieldComponentUi && component.id.contains("DRUGOE", ignoreCase = true)) {

            val isOtherOptionSelected = runCatching {
                val prev = getOrElse(index - 1) { component } as QuizOptionComponentUi
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

