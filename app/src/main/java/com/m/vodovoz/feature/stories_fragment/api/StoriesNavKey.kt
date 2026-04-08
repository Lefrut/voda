package com.m.vodovoz.feature.stories_fragment.api

import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.design_system.model.StoryUi

data class StoriesNavKey(
    val storyId: Long,
    val stories: List<StoryUi>,
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "feature/stories_fragment/Stories"
    }
}
