package com.m.vodovoz.feature.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.feature.cart.model.CartPresentItemUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class CartFragment : Fragment() {

    internal val viewModel: CartFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager


    override fun onStart() {
        super.onStart()
        findNavController().currentBackStackEntry
            ?.savedStateHandle
            ?.remove<CartPresentItemUi>("gift")
            ?.let { gift ->
                viewModel.addGiftToCart(gift)
            }
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    CartEntry(
                        viewModel = viewModel,
                        tabManager = tabManager,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeTabReselect()
        accountManager.reportEvent("Зашел в корзину")

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            tabManager.changeTabVisibility(!imeVisible)
            return@setOnApplyWindowInsetsListener insets
        }
    }

    private fun observeTabReselect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                tabManager.observeTabReselect()
                    .collect { id ->
                        if (id != TabManager.DEFAULT_STATE && id == R.id.cartFragment) {
                            tabManager.setDefaultState()
                        }
                    }
            }
        }
    }
}
