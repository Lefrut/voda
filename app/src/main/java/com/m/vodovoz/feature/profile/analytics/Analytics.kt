package com.m.vodovoz.feature.profile.analytics


import com.m.vodovoz.core.analytics.AnalyticsEvents


fun AnalyticsEvents.reportProfileEvent(screenName: String) {
    if (screenName.isNotEmpty()) {
        reportEvent("Профиль $screenName")
    }
}