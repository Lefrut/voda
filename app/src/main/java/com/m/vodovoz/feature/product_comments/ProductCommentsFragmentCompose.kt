package com.m.vodovoz.feature.product_comments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.product_comments.api.ProductCommentsNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductCommentsFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                ProductCommentsEntry(
                    ProductCommentsNavKey(
                        productId = args.getLong("productId"),
                        productName = args.getString("productName").orEmpty(),
                        productImage = args.getString("productImage").orEmpty()
                    )
                )
            }
        }
    }
}
