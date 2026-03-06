package com.m.vodovoz.feature.product_catalog

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.m.vodovoz.feature.home.model.CategoryUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@AndroidEntryPoint
class ProductCatalogFragment @Inject constructor() : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ProductCatalogEntry()
            }
        }
    }


    @Stable
    sealed class DataSource : Parcelable {
        @Parcelize
        @Immutable
        class Brand(val brandId: Long) : DataSource()

        @Parcelize
        data object HurryBuyUpProducts : DataSource()

        @Parcelize
        data object NewProducts : DataSource()

        @Parcelize
        data object ViewedProducts : DataSource()

        @Parcelize
        data object PastPurchases : DataSource()

        @Parcelize
        @Immutable
        data class ButtonProducts(val buttonId: Int) : DataSource()

        @Parcelize
        @Immutable
        data class Products(val bannerId: Long, val blockId: Long) : DataSource()

        @Parcelize
        @Immutable
        data class Search(val query: String) : DataSource()

        @Parcelize
        @Immutable
        data class Category(val categoryId: Long) : DataSource()

        @Parcelize
        @Immutable
        data object Missing : DataSource()
    }

}
