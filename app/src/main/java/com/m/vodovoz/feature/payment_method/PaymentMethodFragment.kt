package com.m.vodovoz.feature.payment_method

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.payment_method.api.PaymentMethodNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentMethodFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                PaymentMethodEntry(
                    PaymentMethodNavKey(
                        addressId = args.getLong("addressId"),
                        date = args.getLong("date"),
                        paymentMethodId = args.getString("paymentMethodId"),
                        paymentChange = args.getString("paymentChange"),
                        balance = args.get("balance") as? Boolean,
                        bonuses = args.get("bonuses") as? Boolean,
                        bonusesValue = args.get("bonusesValue") as? Int
                    )
                )
            }
        }
    }

}
