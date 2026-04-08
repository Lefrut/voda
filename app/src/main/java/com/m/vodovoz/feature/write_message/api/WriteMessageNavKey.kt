package com.m.vodovoz.feature.write_message.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object WriteMessageNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/write_message/WriteMessage"
}
