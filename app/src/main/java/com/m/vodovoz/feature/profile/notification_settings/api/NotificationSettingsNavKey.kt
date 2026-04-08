package com.m.vodovoz.feature.profile.notification_settings.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object NotificationSettingsNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/profile/notification_settings/NotificationSettings"
}
