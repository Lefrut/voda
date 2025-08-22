package com.vodovoz.app.feature.about_app.model

import com.vodovoz.app.BuildConfig

data class AboutAppState(
    val version: String = BuildConfig.VERSION_NAME,
    val showDeveloperBS: Boolean = false
)