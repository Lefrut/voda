package com.m.vodovoz.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.common.model.toBoolean
import com.m.vodovoz.domain.general.model.widgets.SwitchModel

@Immutable
data class SwitchUi(
    override val id: String,
    val name: String,
    val value: Boolean,
    val enabled: Boolean,
) : WidgetUi(id) {
    override fun value(): String {
        return VodovozBoolean.from(value).value
    }
}

fun SwitchModel.toUi(): SwitchUi {
    return SwitchUi(
        id = id,
        name = name,
        value = VodovozBoolean.from(value).toBoolean(),
        enabled = enabled
    )
}

fun List<SwitchModel>.mapToUi(): List<SwitchUi> {
    return map { switch ->
        switch.toUi()
    }
}
