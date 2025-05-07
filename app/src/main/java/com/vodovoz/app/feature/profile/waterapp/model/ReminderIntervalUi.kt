package com.vodovoz.app.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable

@Immutable
data class ReminderIntervalUi(
    val minutes: Long,
    val selected: Boolean,
)


fun List<Long>.mapToReminderIntervalUi(): List<ReminderIntervalUi> {
    return map { it.toReminderIntervalUi() }
}

fun Long.toReminderIntervalUi(): ReminderIntervalUi {
    return ReminderIntervalUi(this, false)
}

