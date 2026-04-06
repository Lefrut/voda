package com.m.vodovoz.feature.delivery_date

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.delivery_date.api.DeliveryDateNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeliveryDateFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                DeliveryDateEntry(
                    DeliveryDateNavKey(
                        addressId = args.getLong("addressId"),
                        earlierDelivery = args.getBoolean("earlierDelivery"),
                        date = args.getString("date"),
                        timeInterval = args.getString("timeInterval")
                    )
                )
            }
        }
    }

}
