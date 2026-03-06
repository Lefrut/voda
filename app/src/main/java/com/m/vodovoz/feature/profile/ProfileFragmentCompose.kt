package com.m.vodovoz.feature.profile

import android.os.Bundle
import android.view.View
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.m.vodovoz.R
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    internal val viewModel: ProfileFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var cookieManager: CookieManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeTabReselect()
    }

    @Inject
    lateinit var insertVisibilityState: InsetsVisibilityState

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            delay(300)
            insertVisibilityState.consumeSystemBarInsets(true)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    ProfileEntry(
                        viewModel = viewModel,
                        tabManager = tabManager,
                    )
                }
            }
        }
    }


    private fun observeTabReselect() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            tabManager.observeTabReselect()
                .collect {
                    if (it != TabManager.DEFAULT_STATE && it == R.id.profileFragment) {
                        tabManager.setDefaultState()
                    }
                }

        }
    }

}
