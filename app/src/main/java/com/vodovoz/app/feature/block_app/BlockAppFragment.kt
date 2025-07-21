package com.vodovoz.app.feature.block_app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.block_app.composables.BlockAppBody
import com.vodovoz.app.feature.block_app.model.BlockAppEvent
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.base.MainActivityViewModel
import com.vodovoz.app.util.extensions.dialPhoneNumber
import com.vodovoz.app.util.extensions.openUrl
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
class BlockAppFragment : Fragment() {

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()
    private val blockAppViewModel: BlockAppViewModel by viewModels()

    @Inject
    lateinit var siteStateManager: SiteStateManager

    @Inject
    lateinit var blockAppSignal: BlockAppSignal

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSiteState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                VodovozTheme {
                    val blockAppState by blockAppViewModel.state.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .systemBarsPadding()
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp),
                            text = blockAppState.title,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        BlockAppBody(
                            image = blockAppState.image,
                            description = blockAppState.description,
                            contacts = blockAppState.contacts,
                            showTime = blockAppState.showTime,
                            timeHours = blockAppState.hours,
                            timeMinutes = blockAppState.minutes,
                            timeSeconds = blockAppState.seconds,
                            timeDays = blockAppState.days,
                            onContactClick = { contact ->
                                blockAppViewModel.navigateByContact(contact)
                            }
                        )
                    }

                    LifecycleEffect {
                        blockAppViewModel.listenSiteState()
                    }

                    val context = LocalContext.current

                    LifecycleEffect {
                        blockAppViewModel.events.collect { event ->
                            when (event) {
                                is BlockAppEvent.DialPhoneNumber -> {
                                    context.dialPhoneNumber(event.phoneNumber)
                                }

                                BlockAppEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is BlockAppEvent.OpenUrl -> {
                                    context.openUrl(event.url)
                                }
                            }
                        }
                    }

                    BackHandler {}
                }
            }
        }
    }

    private fun observeSiteState() = viewLifecycleOwner.lifecycleScope.launch {
        delay(1000L)

        repeatOnLifecycle(Lifecycle.State.STARTED) {
            siteStateManager.siteStateFlow.collect { siteState ->

                val lastSignal = blockAppSignal.lastSignalOrNull()

                if (siteState == null || siteState.isActive) {
                    countdownJob?.cancel()
                    if (lastSignal == BlockAppSignal.Type.Block) {
                        blockAppSignal.setSignal(BlockAppSignal.Type.Reload)
                    } else {
                        mainActivityViewModel.checkAppState().join()
                    }
                    findNavController().popBackStack()
                    return@collect
                }

                val time = siteState.data?.time ?: ""
                startCountDown(time)
            }
        }
    }

    private var countdownJob: Job? = null

    @Keep
    private fun startCountDown(localDateTime: String) {
        countdownJob?.cancel()

        countdownJob = viewLifecycleOwner.lifecycleScope.launch {
            val locale = Locale.US
            val formatter = VodovozDateFormatters.DMY_HMS
            val futureDateTime = try {
                LocalDateTime.parse(localDateTime, formatter)
            } catch (e: Exception) {
                LocalDateTime.now()
            }

            val zoneId = ZoneId.of("Europe/Moscow")
            val futureMillis = futureDateTime.atZone(zoneId).toInstant().toEpochMilli()

            launch {
                while (isActive) {
                    delay(2000L)
                    siteStateManager.requestSiteState()
                }
            }

            while (isActive) {
                delay(999L)
                val nowMillis = Instant.now().toEpochMilli()

                if (nowMillis < futureMillis) {
                    blockAppViewModel.showTime()

                    val now = LocalDateTime.now(ZoneId.of("Europe/Moscow"))
                    val duration = Duration.between(now, futureDateTime)

                    val totalSeconds = duration.seconds
                    val days = totalSeconds / (24 * 3600)
                    val hours = (totalSeconds % (24 * 3600)) / 3600
                    val minutes = (totalSeconds % 3600) / 60
                    val seconds = totalSeconds % 60

                    blockAppViewModel.setTime(
                        String.format(locale, "%02d", days),
                        String.format(locale, "%02d", hours),
                        String.format(locale, "%02d", minutes),
                        String.format(locale, "%02d", seconds)
                    )
                } else {
                    blockAppViewModel.hideTime()
                    val siteState = siteStateManager.requestSiteState()
                    val siteStateTime = siteState?.data?.time ?: ""
                    startCountDown(siteStateTime)
                }
            }
        }
    }

}