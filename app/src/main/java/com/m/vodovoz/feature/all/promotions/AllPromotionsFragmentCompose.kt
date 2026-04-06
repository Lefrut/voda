package com.m.vodovoz.feature.all.promotions

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.all.promotions.api.AllPromotionsNavKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class AllPromotionsFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                AllPromotionsEntry(
                    AllPromotionsNavKey(
                        dataSource = args.get("dataSource") as? AllPromotionsFragment.DataSource
                            ?: AllPromotionsFragment.DataSource.All
                    )
                )
            }
        }
    }

    @Stable
    sealed class DataSource : Parcelable {
        @Parcelize
        class ByBanner(val bannerId: Long, val blockId: Long) : DataSource()

        @Parcelize
        data object All : DataSource()
    }
}
