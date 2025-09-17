package com.m.vodovoz.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable

@Immutable
data class ReminderIntervalUi(
    val minutes: Long,
) {

    fun shouldDisplayIntervalAsHours(): Boolean = minutes > 91

    fun format(): String {
        return if (!shouldDisplayIntervalAsHours()) minutes.toString()
        else ((minutes / 60) + (minutes % 60).toFloat() / 60).toString()
    }
}


fun List<Long>.mapToReminderIntervalUi(): List<ReminderIntervalUi> {
    return map { it.toReminderIntervalUi() }
}

fun Long.toReminderIntervalUi(): ReminderIntervalUi {
    return ReminderIntervalUi(this)
}

