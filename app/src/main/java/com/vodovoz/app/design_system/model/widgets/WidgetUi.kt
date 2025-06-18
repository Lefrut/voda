package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Stable

@Stable
sealed class WidgetUi(open val id: String)

interface WidgetUpdater {
    fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean

    fun update(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String,
    ): List<WidgetUi>
}


class FieldWidgetUpdater(
    private val validators: List<FieldValidator> = vodovozValidators,
) : WidgetUpdater {

    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is FieldUi && updatedWidget is FieldUi
    }

    override fun update(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String,
    ): List<WidgetUi> {
        val field = updatedWidget as? FieldUi ?: return widgets

        val isValid = field.checkField(validators)

        val fieldWithError = if (isValid) field.resetError() else field

        var newList: List<WidgetUi> = widgets.map { it ->
            if (it.id == fieldWithError.id) fieldWithError else it
        }

        val allFields = newList.filterIsInstance<FieldUi>()

        allFields.forEach { fieldOfAll ->
            newList = newList.map { if (it.id == fieldOfAll.id) fieldOfAll else it }
        }
        return newList

    }
}

class RadioGroupUpdater : WidgetUpdater {
    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is RadioButtonGroupUi<*> && updatedWidget is RadioButtonGroupUi<*>
    }

    override fun update(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String,
    ): List<WidgetUi> {
        return widgets.map { mappingWidget ->
            if (mappingWidget.id == widget.id) updatedWidget
            else mappingWidget
        }
    }

}

class SwitchWidgetUpdater : WidgetUpdater {
    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is SwitchUi && updatedWidget is SwitchUi
    }

    override fun update(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
        getString: (Int) -> String,
    ): List<WidgetUi> {
        return widgets
            .map { mapWidget ->
                if (mapWidget.id == updatedWidget.id) {
                    updatedWidget
                } else {
                    mapWidget
                }
            }
    }
}


class WidgetUpdaterHandler(
    private val updaters: List<WidgetUpdater> = listOf(
        FieldWidgetUpdater(),
        SwitchWidgetUpdater(),
        RadioGroupUpdater()
    ),
    private val getString: (Int) -> String,
) {
    fun updateWidget(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
    ): List<WidgetUi> {
        val updater = updaters.firstOrNull { widgetUpdater ->
            widgetUpdater.canHandle(widget, updatedWidget)
        } ?: return widgets

        return updater.update(widgets, widget, updatedWidget, getString)
    }
}