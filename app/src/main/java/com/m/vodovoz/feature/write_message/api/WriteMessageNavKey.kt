package com.m.vodovoz.feature.write_message.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object WriteMessageNavKey : NavKey {
    const val NAV_NAME: String = "feature/write_message/WriteMessage"
}
