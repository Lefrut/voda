package com.vodovoz.app.design_system.model.widgets

import androidx.compose.runtime.Immutable
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.from
import com.vodovoz.app.common.model.toBoolean
import com.vodovoz.app.domain.general.model.SwitchModel

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
        enabled = true
    )
}

fun List<SwitchModel>.mapToUi(): List<SwitchUi> {
    return map { switch ->
        switch.toUi()
    }
}
