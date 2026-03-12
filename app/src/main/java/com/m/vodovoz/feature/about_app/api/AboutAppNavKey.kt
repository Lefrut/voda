package com.m.vodovoz.feature.about_app.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object AboutAppNavKey : NavKey {
    const val NAV_NAME: String = "feature/about_app/AboutApp"
}
