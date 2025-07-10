package com.vodovoz.app.feature.block_app

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import by.kirich1409.viewbindingdelegate.viewBinding
import coil3.load
import com.vodovoz.app.R
import com.vodovoz.app.databinding.FragmentBlockAppBinding
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.base.MainActivityViewModel
import com.vodovoz.app.util.extensions.addOnBackPressedCallback
import com.vodovoz.app.util.extensions.dialPhoneNumber
import com.vodovoz.app.util.extensions.fromHtml
import com.vodovoz.app.util.extensions.startJivo
import com.vodovoz.app.util.extensions.startTelegram
import com.vodovoz.app.util.extensions.startViber
import com.vodovoz.app.util.extensions.startWhatsUpWithUri
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class BlockAppFragment : Fragment(R.layout.fragment_block_app) {

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private val binding: FragmentBlockAppBinding by viewBinding {
        FragmentBlockAppBinding.bind(requireView())
    }

    @Inject
    lateinit var siteStateManager: SiteStateManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSiteState()
        addOnBackPressedCallback {

        }
    }

    private fun observeSiteState() = viewLifecycleOwner.lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            siteStateManager.siteStateFlow.collect { siteState ->

                if (siteState == null || siteState.isActive) {
                    countdownJob?.cancel()
                    mainActivityViewModel.checkAppState().join()
                    findNavController().navigate(
                        R.id.splashFragment,
                        null,
                        navOptions {
                            popUpTo(R.id.blockAppFragment) { inclusive = true }
                            launchSingleTop = true
                        }
                    )
                    return@collect
                }

                val time = siteState.data?.time ?: ""
                startCountDown(time)

                binding.imageBlockApp.load(siteState.data?.logo ?: "")

                binding.txtBlockApp.text = if (siteState.data?.desc == null) {
                    binding.txtBlockApp.isVisible = false
                    ""
                } else {
                    binding.txtBlockApp.isVisible = true
                    siteState.data.desc.fromHtml()
                }

                val siteData = siteState.data

                siteData?.apply {
                    binding.whatsUp.load(whatsUp?.image)
                    binding.viber.load(viber?.image)
                    binding.telegram.load(telegram?.image)
                    binding.chat.load(chat?.image)
                    binding.imageCall.load(phone?.image)
                }

                binding.whatsUp.setOnClickListener {
                    val url = siteData?.whatsUp?.url ?: return@setOnClickListener
                    requireActivity().startWhatsUpWithUri(url)
                }

                binding.viber.setOnClickListener {
                    val url = siteData?.viber?.url ?: return@setOnClickListener
                    requireActivity().startViber(url)
                }

                binding.telegram.setOnClickListener {
                    val url = siteData?.telegram?.url ?: return@setOnClickListener
                    requireActivity().startTelegram(url)
                }

                binding.telegram.setOnClickListener {
                    val url = siteData?.telegram?.url ?: return@setOnClickListener
                    requireActivity().startTelegram(url)
                }

                binding.chat.setOnClickListener {
                    val url = siteData?.chat?.url ?: return@setOnClickListener
                    requireActivity().startJivo(url)
                }

                binding.imageCall.setOnClickListener {
                    val phone = siteData?.phone?.url ?: return@setOnClickListener
                    requireContext().dialPhoneNumber(phone)
                }
            }
        }
    }

    private var countdownJob: Job? = null

    @Keep
    private fun startCountDown(localDateTime: String) {
        countdownJob?.cancel()

        countdownJob = viewLifecycleOwner.lifecycleScope.launch {
            val locale = Locale.getDefault()
            val formatter = VodovozDateFormatters.DMY_HMS
            val futureDateTime = try {
                LocalDateTime.parse(localDateTime, formatter)
            } catch (e: Exception) {
                LocalDateTime.now()
            }

            val zoneId = ZoneId.of("Europe/Moscow")
            val futureMillis = futureDateTime.atZone(zoneId).toInstant().toEpochMilli()

            while (isActive) {
                delay(1000)
                val nowMillis = Instant.now().toEpochMilli()

                if (nowMillis < futureMillis) {
                    binding.linearTimeData.visibility = View.VISIBLE

                    val now = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
                    val duration = Duration.between(now, futureDateTime)

                    val totalSeconds = duration.seconds
                    val days = totalSeconds / (24 * 3600)
                    val hours = (totalSeconds % (24 * 3600)) / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    binding.txtDays.text = String.format(locale, "%02d", days)
                    binding.txtHours.text = String.format(locale, "%02d", hours)
                    binding.txtMinute.text = String.format(locale, "%02d", minutes)
                    binding.txtSecond.text = String.format(locale, "%02d", seconds)
                } else {
                    binding.linearTimeData.visibility = View.GONE
                    val siteState = siteStateManager.requestSiteState()
                    val siteStateTime = siteState?.data?.time ?: ""
                    startCountDown(siteStateTime)
                }
            }
        }
    }

}