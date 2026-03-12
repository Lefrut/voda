package com.m.vodovoz.feature.stories_fragment.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object StoriesNavKey : NavKey {
    const val NAV_NAME: String = "feature/stories_fragment/Stories"
}
