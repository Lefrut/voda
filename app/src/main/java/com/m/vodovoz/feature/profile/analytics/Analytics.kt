package com.m.vodovoz.feature.profile.analytics

import com.m.vodovoz.core.analytics.Analytics


fun Analytics.reportProfileEvent(screenName: String) {
    if (screenName.isNotEmpty()) {
        reportEvent("Профиль $screenName")
    }
}