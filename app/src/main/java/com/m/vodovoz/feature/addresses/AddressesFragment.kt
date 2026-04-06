package com.m.vodovoz.feature.addresses

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.addresses.api.AddressesNavKey
import com.m.vodovoz.feature.addresses.model.AddressScreenTypeUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddressesFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                AddressesEntry(
                    AddressesNavKey(
                        screenType = args.get("screenType") as? AddressScreenTypeUi
                            ?: AddressScreenTypeUi.Add,
                        addressId = args.getLong("addressId").takeIf { args.containsKey("addressId") }
                    )
                )
            }
        }
    }
}
