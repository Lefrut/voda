package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable

@Immutable
sealed class WidgetUi(open val id: String)

interface WidgetUpdater {
    fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean

    fun update(
        currentList: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String
    ): List<WidgetUi>
}


class FieldWidgetUpdater(
    private val validators: List<FieldValidator> = vodovozValidators
) : WidgetUpdater {

    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is FieldUi && updatedWidget is FieldUi
    }

    override fun update(
        currentList: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String
    ): List<WidgetUi> {
        val field = updatedWidget as FieldUi
        val isLocallyValid = field.checkField(validators)

        val withLocalError = if (!isLocallyValid) {
            field.copy(
                isError = true,
                supportingText = field.getErrorText(getString)
            )
        } else {
            field.resetError()
        }

        var newList: List<WidgetUi> = currentList
            .map { if (it.id == withLocalError.id) withLocalError else it }

        val allFields = newList.filterIsInstance<FieldUi>()
        allFields.checkFields(
            putErrors = true,
            validators = validators,
            getSupportingText = { it.getErrorText(getString) }
        ) { checkedFields, _ ->
            checkedFields.forEach { cf ->
                newList = newList.map { if (it.id == cf.id) cf else it }
            }
        }

        return newList
    }
}


class SwitchWidgetUpdater : WidgetUpdater {
    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is SwitchUi && updatedWidget is SwitchUi
    }

    override fun update(
        currentList: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String
    ): List<WidgetUi> {
        return currentList
            .map { mapWidget ->
                if (mapWidget.id == updatedWidget.id) {
                    updatedWidget
                } else { mapWidget }
            }
    }
}


class WidgetUpdaterHandler(
    private val updaters: List<WidgetUpdater> = listOf(FieldWidgetUpdater(), SwitchWidgetUpdater()),
    private val getString: (Int) -> String
) {
    fun updateWidget(
        currentList: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi
    ): List<WidgetUi> {
        val updater = updaters.firstOrNull { widgetUpdater ->
            widgetUpdater.canHandle(widget, updatedWidget)
        } ?: return currentList

        return updater.update(currentList, widget, updatedWidget, getString)
    }
}