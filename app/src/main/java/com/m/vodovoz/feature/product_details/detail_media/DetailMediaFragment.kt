package com.m.vodovoz.feature.product_details.detail_media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.design_system.model.ProductMediaUi
import com.m.vodovoz.feature.product_details.detail_media.api.DetailMediaNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailMediaFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                DetailMediaEntry(
                    DetailMediaNavKey(
                        media = args.get("media") as ProductMediaUi,
                        mediaList = args.get("mediaList") as? List<ProductMediaUi> ?: emptyList()
                    )
                )
            }
        }
    }
}
