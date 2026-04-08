package com.m.vodovoz.feature.about_app.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutAppNavKey : VodovozNavKey {
    const val NAV_NAME: String = "feature/about_app/AboutApp"
}
