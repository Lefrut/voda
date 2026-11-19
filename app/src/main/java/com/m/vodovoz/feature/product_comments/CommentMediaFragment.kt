package com.m.vodovoz.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.tab.hideTab
import com.m.vodovoz.common.tab.showTab
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.VerticalImagePager
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.product_comments.model.CommentMediaUi
import com.m.vodovoz.ui.compose.player.MediaComposePlayer
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CommentMediaFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var insetsVisibilityState: InsetsVisibilityState

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = arguments
        val media = args?.let {
            BundleCompat.getParcelable(it, "media", CommentMediaUi::class.java)
        }?.takeIf { it.url.isNotBlank() }
        val images = args?.let {
            BundleCompat.getParcelableArrayList(it, "images", CommentMediaUi.Image::class.java)
        }.orEmpty()
        val onClose: () -> Unit = { findNavController().popBackStack() }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                if (media == null) {
                    LifecycleEffect {
                        val navController = findNavController()
                        if (navController.currentDestination?.id == R.id.commentMediaFragment) {
                            navController.popBackStack()
                        }
                    }
                    return@setContent
                }

                LifecycleStartEffect(Unit) {
                    tabManager.hideTab()
                    insetsVisibilityState.consumeSystemBarInsets(false)
                    onStopOrDispose {
                        insetsVisibilityState.consumeSystemBarInsets(true)
                        tabManager.showTab()
                    }
                }

                VodovozTheme {
                    when (media) {
                        is CommentMediaUi.Image -> {
                            val initialPage = images.indexOf(media)
                            VerticalImagePager(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.onBackground)
                                    .windowInsetsPadding(WindowInsets.systemBars),
                                initialPage = initialPage.coerceAtLeast(0),
                                images = if (initialPage >= 0) images else listOf(media),
                                onCloseClick = onClose
                            )
                        }

                        is CommentMediaUi.Video -> {
                            MediaComposePlayer(
                                url = media.url,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                                    .windowInsetsPadding(WindowInsets.systemBars),
                                onCloseClick = onClose
                            )
                        }
                    }
                }
            }
        }
    }
}
