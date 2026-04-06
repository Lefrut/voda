package com.m.vodovoz.feature.order_call_you

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.order_call_you.api.OrderCallYouNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderCallYouFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                OrderCallYouEntry(
                    OrderCallYouNavKey(
                        addressId = args.getLong("addressId"),
                        callYouId = args.getString("callYouId"),
                        queryParams = args.get("queryParams") as? Map<String, String> ?: emptyMap()
                    )
                )
            }
        }
    }

}
