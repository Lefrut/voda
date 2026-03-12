package com.m.vodovoz.feature.stories_fragment.api

import androidx.navigation3.runtime.NavKey
import com.m.vodovoz.design_system.model.StoryUi

data class StoriesNavKey(
    val storyId: Long,
    val stories: List<StoryUi>,
) : NavKey {
    companion object {
        const val NAV_NAME: String = "feature/stories_fragment/Stories"
    }
}
