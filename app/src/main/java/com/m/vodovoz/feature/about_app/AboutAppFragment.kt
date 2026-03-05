package com.m.vodovoz.feature.about_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutAppFragment @Inject constructor() : Fragment() {

    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()
    private val catalogFlowViewModel: CatalogFlowViewModel by activityViewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                AboutAppEntry(
                    onRefreshApp = {
                        homeViewModel.fetchHomeDetails()
                        cartFlowViewModel.fetchCartDetails()
                        favoriteViewModel.fetchFavoriteProducts()
                        catalogFlowViewModel.fetchCatalogDetails()
                        profileViewModel.fetchProfileDetails()
                    }
                )
            }
        }
    }
}
