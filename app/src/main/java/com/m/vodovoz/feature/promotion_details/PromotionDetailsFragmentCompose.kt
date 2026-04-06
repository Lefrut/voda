package com.m.vodovoz.feature.promotion_details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.promotion_details.api.PromotionDetailsNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PromotionDetailsFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                PromotionDetailsEntry(PromotionDetailsNavKey(promotionId = args.getLong("promotionId")))
            }
        }
    }
}
