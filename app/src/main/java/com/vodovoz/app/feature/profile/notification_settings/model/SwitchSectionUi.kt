package com.vodovoz.app.feature.profile.notification_settings.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.SwitchSectionModel

@Immutable
data class SwitchSectionUi(
    val title: String,
    val description: String,
    val switches: List<SwitchUi>,
)

fun List<SwitchSectionModel>.mapToUi(): List<SwitchSectionUi> {
    return map { it.toUi() }
}

fun SwitchSectionModel.toUi(): SwitchSectionUi {
    return SwitchSectionUi(
        title = title,
        description = description,
        switches = switches.mapToDomain()
    )
}
