package com.vodovoz.app.feature.profile.notification_settings.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.SwitchModel

@Immutable
data class SwitchUi(
    val id: String,
    val name: String,
    val checked: Boolean,
)

fun List<SwitchModel>.mapToDomain(): List<SwitchUi> {
    return map { it.toUi() }
}

fun SwitchModel.toUi(): SwitchUi {
    return SwitchUi(
        id = id,
        name = name,
        checked = active
    )
}