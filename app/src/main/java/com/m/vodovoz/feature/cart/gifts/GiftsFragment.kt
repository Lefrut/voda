package com.m.vodovoz.feature.cart.gifts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.cart.gifts.api.GiftsNavKey
import com.m.vodovoz.feature.cart.model.CartPresentPopupWindowUi
import com.m.vodovoz.feature.cart.model.CartPresentUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GiftsFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                GiftsEntry(
                    GiftsNavKey(
                        present = args.get("present") as? CartPresentUi,
                        popupWindow = args.get("popupWindow") as CartPresentPopupWindowUi
                    )
                )
            }
        }
    }

}
