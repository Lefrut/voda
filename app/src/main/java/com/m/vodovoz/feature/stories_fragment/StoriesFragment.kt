package com.m.vodovoz.feature.stories_fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.design_system.model.StoryUi
import com.m.vodovoz.feature.stories_fragment.api.StoriesNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StoriesFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                StoriesEntry(
                    StoriesNavKey(
                        storyId = args.getLong("storyId"),
                        stories = args.get("stories") as? List<StoryUi> ?: emptyList()
                    )
                )
            }
        }
    }


}
