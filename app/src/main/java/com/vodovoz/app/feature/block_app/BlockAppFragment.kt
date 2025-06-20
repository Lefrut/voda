package com.vodovoz.app.feature.block_app

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import coil3.load
import com.vodovoz.app.R
import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.databinding.FragmentBlockAppBinding
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.addOnBackPressedCallback
import com.vodovoz.app.util.extensions.dialPhoneNumber
import com.vodovoz.app.util.extensions.fromHtml
import com.vodovoz.app.util.extensions.startJivo
import com.vodovoz.app.util.extensions.startTelegram
import com.vodovoz.app.util.extensions.startViber
import com.vodovoz.app.util.extensions.startWhatsUpWithUri
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class BlockAppFragment : Fragment() {

    private val binding: FragmentBlockAppBinding by viewBinding {
        FragmentBlockAppBinding.bind(requireView())
    }

    @Inject
    lateinit var siteStateManager: SiteStateManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSiteState()
        addOnBackPressedCallback { }
    }

    private fun observeSiteState() = lifecycleScope.launch {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            siteStateManager.siteStateFlow.collectLatest { siteState ->
                if (siteState != null) {

                    if (siteState.isActive) {
                        findNavController().navigate(R.id.splashFragment)
                    }

                    val time = siteState.data?.time
                    if (time.isNullOrEmpty()) {
                        binding.linearTimeData.isVisible = false
                    } else {
                        binding.linearTimeData.isVisible = true
                        countDownStart(siteState.data.time)
                    }



                    binding.imageBlockApp.load(siteState.data?.logo?.toVodovozUrl())

                    binding.txtBlockApp.text = if (siteState.data?.desc == null) {
                        binding.txtBlockApp.isVisible = false
                        ""
                    } else {
                        binding.txtBlockApp.isVisible = true
                        siteState.data.desc.fromHtml()
                    }

                    binding.whatsUp.load(siteState.data?.whatsUp?.image?.toVodovozUrl())

                    binding.viber.load(siteState.data?.viber?.image?.toVodovozUrl())

                    binding.telegram.load(siteState.data?.telegram?.image?.toVodovozUrl())

                    binding.chat.load(siteState.data?.chat?.image?.toVodovozUrl())

                    binding.imageCall.load(siteState.data?.phone?.image?.toVodovozUrl())

                    binding.whatsUp.setOnClickListener {
                        val url = siteState.data?.whatsUp?.url ?: return@setOnClickListener
                        requireActivity().startWhatsUpWithUri(url)
                    }

                    binding.viber.setOnClickListener {
                        val url = siteState.data?.viber?.url ?: return@setOnClickListener
                        requireActivity().startViber(url)
                    }

                    binding.telegram.setOnClickListener {
                        val url = siteState.data?.telegram?.url ?: return@setOnClickListener
                        requireActivity().startTelegram(url)
                    }

                    binding.telegram.setOnClickListener {
                        val url = siteState.data?.telegram?.url ?: return@setOnClickListener
                        requireActivity().startTelegram(url)
                    }

                    binding.chat.setOnClickListener {
                        val url = siteState.data?.chat?.url ?: return@setOnClickListener
                        requireActivity().startJivo(url)
                    }

                    binding.imageCall.setOnClickListener {
                        val phone = siteState.data?.phone?.url ?: return@setOnClickListener
                        requireContext().dialPhoneNumber(phone)
                    }

                } else {
                    findNavController().navigate(R.id.splashFragment)
                }

            }
        }
    }

    private var countDownJob: Job? = null

    private fun countDownStart(time: String) {
        countDownJob?.cancel()

        countDownJob = lifecycleScope.launch {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val futureDateTime = try {
                LocalDateTime.parse(time, formatter)
            } catch (e: Exception) {
                return@launch
            }

            val zone = ZoneId.of("Europe/Moscow")
            val futureMillis = futureDateTime.atZone(zone).toInstant().toEpochMilli()


            while (isActive) {
                val nowMillis = Instant.now().toEpochMilli()

                if (nowMillis < futureMillis) {
                    binding.linearTimeData.visibility = View.VISIBLE

                    val diffMillis = (futureMillis - nowMillis).milliseconds

                    binding.linearTimeData.visibility = View.VISIBLE

                    val seconds = diffMillis.inWholeSeconds
                    val minutes = diffMillis.inWholeMinutes
                    val hours = diffMillis.inWholeHours
                    val days = diffMillis.inWholeDays

                    val locale = Locale.getDefault()
                    binding.txtDays.text = String.format(locale, "%02d", days)
                    binding.txtHours.text = String.format(locale, "%02d", hours)
                    binding.txtMinute.text = String.format(locale, "%02d", minutes)
                    binding.txtSecond.text = String.format(locale, "%02d", seconds)
                } else {
                    binding.linearTimeData.visibility = View.GONE

                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        siteStateManager.requestSiteState()
                    }

                    break
                }

                delay(1000)
            }
        }
    }

}