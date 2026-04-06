package com.m.vodovoz.feature.filter_values

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.design_system.model.filters.FilterUi
import com.m.vodovoz.feature.filter_values.api.FilterValuesNavKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterValuesFlowFragment @Inject constructor() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                FilterValuesEntry(
                    FilterValuesNavKey(
                        categoryId = args.getLong("categoryId"),
                        filter = args.get("filter") as FilterUi
                    )
                )
            }
        }
    }

}
