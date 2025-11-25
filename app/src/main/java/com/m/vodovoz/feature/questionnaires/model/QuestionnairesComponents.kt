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
import kotlin.reflect.KClass


@Immutable
sealed interface QuizComponentUi {
    val id: String
    val error: Boolean
    val label: String

    fun updateByValidation(
        warnings: QuizWarningsVisitor,
    ): QuizComponentUi

}


interface QuizComponentsUpdater<T : QuizComponentUi> {

    val clazz: KClass<T>
    val id: String

    fun updateList(
        components: List<QuizComponentUi>
    ): List<QuizComponentUi> {
        return components.map {
            if (it.id == id && clazz == it.javaClass) {
                @Suppress("UNCHECKED_CAST")
                update(it as T)
            } else it
        }
    }

    fun update(component: T): T


}

class CheckListUpdater(
    override val id: String,
    private val option: ComponentOptionUi,
) : QuizComponentsUpdater<CheckboxListUi> {

    override val clazz: KClass<CheckboxListUi> = CheckboxListUi::class

    override fun update(component: CheckboxListUi): CheckboxListUi {
        val updatedOptions = component.options.map { opt ->
            if (opt.label == option.label) opt.copy(value = option.value)
            else opt
        }
        return component.copy(options = updatedOptions)
    }
}


class QuizWarningsVisitor(
    val getString: (Int) -> String,
) {
    fun fieldWarning(resId: Int) = getString(resId)
}

@Immutable
sealed interface QuizOptionComponentUi : QuizComponentUi {
    val options: List<ComponentOptionUi>

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
}

inline fun <T : QuizOptionComponentUi> T.update(
    option: ComponentOptionUi,
    copy: (options: List<ComponentOptionUi>) -> T,
): T {
    val updatedOptions = options.map { opt ->
        if (opt.label == option.label) opt.copy(value = option.value)
        else opt
    }
    return copy(updatedOptions)
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

inline fun <reified T : QuizComponentUi> QuizComponentUi.updateIfSame(
    id: String,
    onSame: T.() -> T,
): QuizComponentUi {
    return if (this is T && this.id == id) {
        onSame()
    } else this
}

inline fun <reified T : QuizComponentUi, reified T2 : QuizComponentUi> QuizComponentUi.updateIfSame(
    id: String,
    onSame1: T.() -> T,
    onSame2: T2.() -> T2,
): QuizComponentUi {
    val initial = this

    listOf(
        updateIfSame<T>(id, onSame1),
        updateIfSame<T2>(id, onSame2)
    ).forEach { updatedComponent ->
        if (updatedComponent != initial) return updatedComponent
    }
    return initial
}

inline fun <reified T : QuizComponentUi, reified T2 : QuizComponentUi, reified T3 : QuizComponentUi> QuizComponentUi.updateIfSame(
    id: String,
    onSame1: T.() -> T,
    onSame2: T2.() -> T2,
    onSame3: T3.() -> T3,
): QuizComponentUi {
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

