package com.m.vodovoz.feature.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.categories.api.CategoriesNavKey
import com.m.vodovoz.feature.home.model.CategoryUi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesFragment @Inject constructor() : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val args = requireArguments()
        return ComposeView(requireContext()).apply {
            setContent {
                CategoriesEntry(
                    CategoriesNavKey(
                        categoryList = args.get("categoryList") as? Array<CategoryUi> ?: emptyArray(),
                        category = args.get("category") as? CategoryUi ?: CategoryUi.Empty,
                        source = args.get("source") as? CategoriesNavKey.Source
                            ?: CategoriesNavKey.Source.ProductCatalog
                    )
                )
            }
        }
    }

}
