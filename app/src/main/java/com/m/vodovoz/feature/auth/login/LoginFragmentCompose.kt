package com.m.vodovoz.feature.auth.login

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_BIOMETRIC_ENROLL
import android.provider.Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.AuthArgs
import com.m.vodovoz.core.navigation.navigateToLoginByEmail
import com.m.vodovoz.core.navigation.navigateToLoginByPhone
import com.m.vodovoz.core.navigation.navigateToRegister
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.auth.model.withAccountTypeSelection
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.snack
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager

    private val viewModel: LoginFlowViewModel by viewModels()

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


                    when (viewState.uiState) {
                        LoginFlowViewModel.LoginUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchLoginDetails() }
                        }

                        LoginFlowViewModel.LoginUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        LoginFlowViewModel.LoginUiState.Success -> {
                            LoginScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }

                    LifecycleEffect {
                        observeEvents()
                    }
                }
            }
        }
    }

    private suspend fun observeEvents(): Unit = viewModel.events.onStart {
        val nav = findNavController()
        val savedStateHandle = (nav.currentBackStackEntry ?: return@onStart).savedStateHandle

        val accountTypeId = savedStateHandle.remove<String>(
            AuthArgs.ACCOUNT_TYPE_ID
        )
        viewModel.setAccountTypeById(accountTypeId)

    }.collect { events ->
        val navController = findNavController()
        when (events) {
            LoginFlowViewModel.LoginEvents.GoBack -> {
                navController.popBackStack()
            }

            is LoginFlowViewModel.LoginEvents.GoToWebView -> {
                navController.navigateToWebView(
                    url = events.url,
                    title = events.title,
                )
            }

            is LoginFlowViewModel.LoginEvents.GoToLoginByEmail -> {
                navController.navigateToLoginByEmail(events.selectedAccountTypeId)
            }

            LoginFlowViewModel.LoginEvents.GoToRegister -> {
                navController.navigateToRegister()
            }

            is LoginFlowViewModel.LoginEvents.GoToLoginByPhone -> {
                navController.navigateToLoginByPhone(
                    phone = events.phone,
                    waitSeconds = events.waitSeconds,
                    userUrl = events.userUrl
                )
            }
        }

    }


//    private fun checkShowFingerPrint() {
//        val userSettings = accountManager.fetchUserSettings()
//        val isSettingsCorrect =
//            userSettings.email.isNotEmpty() && userSettings.password.isNotEmpty()
//        if (isSettingsCorrect) checkBiometric()
//    }

//    private fun checkBiometric() {
//        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
//            BiometricManager.BIOMETRIC_SUCCESS -> {
//                biometricPrompt.authenticate(promptInfo)
//                accountManager.saveUseBio(true)
//            }
//
//            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
//                accountManager.saveUseBio(false)
//            }
//
//            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
//                accountManager.saveUseBio(false)
//            }
//
//            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
//                accountManager.saveUseBio(false)
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                    val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
//                        putExtra(
//                            EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
//                        )
//                    }
//                    biometricResultLauncher.launch(enrollIntent)
//                }
//            }
//            else -> {
//                accountManager.saveUseBio(false)
//            }
//        }
//    }
}
