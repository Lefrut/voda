package com.m.vodovoz.feature.profile.notification_settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object NotificationSettingsNavKey : NavKey {
    const val NAV_NAME: String = "feature/profile/notification_settings/NotificationSettings"
}
