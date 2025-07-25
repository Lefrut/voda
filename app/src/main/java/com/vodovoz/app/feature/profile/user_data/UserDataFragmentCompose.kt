package com.vodovoz.app.feature.profile.user_data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.vodovoz.app.R
import com.vodovoz.app.common.media.ImagePickerFragment
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.catalog.CatalogFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)
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
                        viewModel.observeEvent().collect { event ->
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
                                    findNavController().navigate(
                                        R.id.imagePickerFragment,
                                        bundleOf(ImagePickerFragment.IMAGE_PICKER_RECEIVER to ImagePickerFragment.AVATAR)
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