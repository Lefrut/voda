package com.m.vodovoz.feature.write_comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.write_comment.api.WriteCommentNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WriteCommentFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                WriteCommentEntry(
                    WriteCommentNavKey(
                        product_id = args.getLong("product_id"),
                        product_name = args.getString("product_name").orEmpty(),
                        product_image = args.getString("product_image").orEmpty(),
                        rating = args.getInt("rating"),
                        source = args.get("source") as? WriteCommentNavKey.Source
                            ?: WriteCommentNavKey.Source.None
                    )
                )
            }
        }
    }

}
