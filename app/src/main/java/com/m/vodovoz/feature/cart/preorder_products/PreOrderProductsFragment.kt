package com.m.vodovoz.feature.cart.preorder_products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.m.vodovoz.common.tab.TabManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PreOrderProductsFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        tabManager.changeTabVisibility(false)
        super.onStart()
    }

    override fun onStop() {
        tabManager.changeTabVisibility(true)
        super.onStop()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PreOrderProductsEntry()
            }
        }
    }
}
