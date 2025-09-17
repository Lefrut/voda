package com.m.vodovoz.feature.about_app.model

import com.m.vodovoz.BuildConfig

data class AboutAppState(
    val version: String = BuildConfig.VERSION_NAME,
    val showDeveloperBS: Boolean = false
)