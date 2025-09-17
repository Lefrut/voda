package com.m.vodovoz.feature.profile.user_data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.findRootNavController
import com.m.vodovoz.core.navigation.slideAnim
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UserDataFragment : Fragment() {

    private val viewModel: UserDataFlowViewModel by viewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartViewModel: CartFlowViewModel by activityViewModels()
    private val catalogViewModel: CatalogFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()

    @Inject
    internal lateinit var insetsVisibilityState: InsetsVisibilityState

    override fun onStart() {
        insetsVisibilityState.consumeSystemBarInsets(true)
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    
                    val snackbarHostState = remember { SnackbarHostState() }

                    when (viewState.uiState) {
                        UserDataFlowViewModel.UserDataUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchUserData() }
                        }

                        UserDataFlowViewModel.UserDataUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        UserDataFlowViewModel.UserDataUiState.Success -> {
                            UserDataScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                snackbarHostState = snackbarHostState
                            )
                        }
                    }


                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                UserDataFlowViewModel.UserDataEvents.RefreshAllAndGoBack -> {
                                    homeViewModel.refresh()
                                    cartViewModel.refresh()
                                    catalogViewModel.refresh()
                                    profileViewModel.refresh()
                                    favoriteViewModel.refresh()

                                    delay(200)

                                    findNavController().navigate(
                                        resId = R.id.profileFragment,
                                        args = Bundle.EMPTY,
                                        navOptions = navOptions {
                                            popUpTo(R.id.profileFragment) { inclusive = true }
                                            anim {
                                                enter = R.anim.fade_in
                                                exit = R.anim.fade_out
                                            }
                                        }
                                    )
                                }

                                UserDataFlowViewModel.UserDataEvents.UpdateProfile -> {
                                    profileViewModel.fetchProfileDetails()
                                }

                                UserDataFlowViewModel.UserDataEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is UserDataFlowViewModel.UserDataEvents.ShowSnackbar -> {
                                    launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar(event.message)
                                    }
                                }

                                UserDataFlowViewModel.UserDataEvents.OpenImagePicker -> {
                                    findRootNavController()?.navigate(
                                        R.id.imagePickerFragment,
                                        Bundle.EMPTY,
                                        navOptions { slideAnim() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}