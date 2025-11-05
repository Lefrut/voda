package com.m.vodovoz.design_system.model.widgets

import androidx.compose.runtime.Stable

@Stable
sealed class WidgetUi(open val id: String) {

    abstract fun value(): String

}


fun WidgetUi?.toQueryMap(): Map<String, String> {
    return if (this == null) {
        emptyMap()
    } else mapOf(id to value())
}


fun List<WidgetUi>.toQueryMap(): Map<String, String> {
    return associate { it.id to it.value() }
}

fun List<WidgetUi>.toMap(): Map<String, String> {
    return associate { it.id to it.value() }
}

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
        return widgets.map { mapWidget ->
            if (mapWidget.id == updatedWidget.id) {
                updatedWidget
            } else {
                mapWidget
            }
        }
    }
}

class SingleCheckboxGroupUpdater : WidgetUpdater {
    override fun canHandle(widget: WidgetUi, updatedWidget: WidgetUi): Boolean {
        return widget is SingleCheckboxGroup<*> && updatedWidget is SingleCheckboxGroup<*>
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


class WidgetUpdaterKeeperImpl(
    private val updaters: List<WidgetUpdater>,
    private val getString: (Int) -> String,
) : WidgetUpdaterKeeper {
    override fun updateWidget(
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

interface WidgetUpdaterKeeper {
    fun updateWidget(
        widgets: List<WidgetUi>,
        widget: WidgetUi,
        updatedWidget: WidgetUi,
    ): List<WidgetUi>
}

interface WidgetUpdaterKeeperFactory {


    companion object {

        val baseUpdaters = listOf(
            FieldWidgetUpdater(),
            SwitchWidgetUpdater(),
            RadioGroupUpdater(),
            SingleCheckboxGroupUpdater()
        )

        fun createWidgetUpdateKeeper(
            updaters: List<WidgetUpdater> = baseUpdaters,
            getString: (Int) -> String,
        ): WidgetUpdaterKeeper {
            return WidgetUpdaterKeeperImpl(updaters, getString)
        }
    }
}
