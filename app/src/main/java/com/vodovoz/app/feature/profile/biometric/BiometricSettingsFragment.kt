package com.vodovoz.app.feature.profile.biometric

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import com.vodovoz.app.R
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.databinding.FragmentBiometricSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BiometricSettingsFragment : Fragment(R.layout.fragment_biometric_settings) {

    @Inject
    lateinit var accountManager: AccountManager

    private val biometricManager by lazy { BiometricManager.from(requireContext()) }

    private val binding: FragmentBiometricSettingsBinding by lazy {
        FragmentBiometricSettingsBinding.bind(view ?: View(requireContext()))

    }

    private val biometricResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val resultCode = result.resultCode
            if (resultCode == Activity.RESULT_OK) {
                accountManager.saveUseBio(true)
                binding.personalDataSwitch.isChecked = true
            } else {
                binding.personalDataSwitch.isChecked = false
                accountManager.saveUseBio(false)
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.personalDataSwitch.isChecked = accountManager.fetchUseBio()

        binding.personalDataSwitch.setOnCheckedChangeListener { _, p1 ->
            if (p1) {
                checkBiometric()
            } else {
                accountManager.saveUseBio(false)
            }
        }

    }

    private fun checkBiometric() {
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> accountManager.saveUseBio(true)
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> accountManager.saveUseBio(false)
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> accountManager.saveUseBio(false)
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Prompts the user to create credentials that your app accepts.
                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                    putExtra(
                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                }
                biometricResultLauncher.launch(enrollIntent)
            }

            else -> {

            }
        }

    }
}