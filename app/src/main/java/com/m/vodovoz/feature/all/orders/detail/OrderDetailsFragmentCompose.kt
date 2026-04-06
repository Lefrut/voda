package com.m.vodovoz.feature.all.orders.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.mainFragment
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackBarVisuals
import com.m.vodovoz.ui.snackbar.snackBarHostState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderDetailsFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                OrderDetailsEntry(null!!)
            }
        }
    }
}
