package com.vodovoz.app.feature.auth.login

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToLoginByEmail
import com.vodovoz.app.core.navigation.navigateToLoginByPhone
import com.vodovoz.app.core.navigation.navigateToRegister
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.cart.CartFlowViewModel
import com.vodovoz.app.feature.favorite.FavoriteFlowViewModel
import com.vodovoz.app.feature.home.HomeFlowViewModel
import com.vodovoz.app.feature.profile.ProfileFlowViewModel
import com.vodovoz.app.util.extensions.snack
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager

    private val executor: Executor by lazy { ContextCompat.getMainExecutor(requireContext()) }

    private val biometricManager by lazy { BiometricManager.from(requireContext()) }

    private val biometricPrompt: BiometricPrompt by lazy {
        BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    requireActivity().snack(getString(R.string.biometric_fault))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    //todo - put auth method
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    requireActivity().snack(getString(R.string.biometric_fault))
                }
            })
    }

    private val promptInfo: BiometricPrompt.PromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()
    }


    private val viewModel: LoginFlowViewModel by viewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()
    private val flowViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()

    private val biometricResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                biometricPrompt.authenticate(promptInfo)
                accountManager.saveUseBio(true)
            } else {
                accountManager.saveUseBio(false)
            }
        }

    override fun onStart() {
        super.onStart()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        tabManager.changeTabVisibility(true)
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
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(pagingState.data)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //todo - mb do something
        //checkShowFingerPrint()
    }

    private fun checkShowFingerPrint() {
        val userSettings = accountManager.fetchUserSettings()
        val isSettingsCorrect =
            userSettings.email.isNotEmpty() && userSettings.password.isNotEmpty()
        if (isSettingsCorrect) checkBiometric()
    }

    private suspend fun observeEvents(): Unit = viewModel.observeEvent().collect { events ->
        when (events) {
            LoginFlowViewModel.LoginEvents.AuthSuccess -> {
                profileViewModel.refresh()
                flowViewModel.refresh()
                cartFlowViewModel.refresh()
                favoriteViewModel.refresh()

                val redirect = tabManager.fetchAuthRedirect()
                if (redirect == TabManager.DEFAULT_AUTH_REDIRECT) {
                    findNavController().popBackStack()
                } else {
                    tabManager.selectTab(redirect)
                    tabManager.setDefaultAuthRedirect()
                }
            }

            LoginFlowViewModel.LoginEvents.GoBack -> {
                findNavController().popBackStack()
            }

            is LoginFlowViewModel.LoginEvents.GoToWebView -> {
                findNavController().navigateToWebView(
                    url = events.url,
                    title = events.title,
                )
            }

            LoginFlowViewModel.LoginEvents.GoToLoginByEmail -> {
                findNavController().navigateToLoginByEmail()
            }

            LoginFlowViewModel.LoginEvents.GoToRegister -> {
                findNavController().navigateToRegister()
            }

            is LoginFlowViewModel.LoginEvents.GoToLoginByPhone -> {
                findNavController().navigateToLoginByPhone(events.phone, events.waitSeconds)
            }
        }

    }


    private fun checkBiometric() {
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
                accountManager.saveUseBio(true)
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                accountManager.saveUseBio(false)
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                accountManager.saveUseBio(false)
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                accountManager.saveUseBio(false)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val enrollIntent = Intent(ACTION_BIOMETRIC_ENROLL).apply {
                        putExtra(
                            EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                        )
                    }
                    biometricResultLauncher.launch(enrollIntent)
                }
            }
            else -> {
                accountManager.saveUseBio(false)
            }
        }
    }
}