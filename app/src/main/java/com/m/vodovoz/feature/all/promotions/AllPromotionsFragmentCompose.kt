package com.m.vodovoz.feature.all.promotions

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
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
        return ComposeView(requireContext()).apply {
            setContent {
                AllPromotionsEntry()
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
