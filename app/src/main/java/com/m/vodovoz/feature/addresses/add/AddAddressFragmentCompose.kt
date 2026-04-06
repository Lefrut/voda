package com.m.vodovoz.feature.addresses.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.addresses.add.api.AddAddressNavKey
import com.m.vodovoz.feature.map.model.MapAddressUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddAddressFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                AddAddressEntry(
                    AddAddressNavKey(
                        mapAddress = args.get("mapAddress") as? MapAddressUi,
                        addressId = args.getLong("addressId").takeIf { args.containsKey("addressId") },
                        addressName = args.getString("addressName"),
                        addressType = args.getInt("addressType").takeIf { args.containsKey("addressType") }
                    )
                )
            }
        }
    }
}
