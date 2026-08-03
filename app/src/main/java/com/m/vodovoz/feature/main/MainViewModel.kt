package com.m.vodovoz.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

internal const val FLOATING_PROMO_CLICK_COOLDOWN_MS = 60L * 60L * 1_000L

internal fun floatingPromoCooldownRemainingMillis(
    clickedAtElapsedRealtime: Long,
    nowElapsedRealtime: Long,
    cooldownMillis: Long = FLOATING_PROMO_CLICK_COOLDOWN_MS,
): Long {
    val elapsedMillis = (nowElapsedRealtime - clickedAtElapsedRealtime).coerceAtLeast(0L)
    return (cooldownMillis - elapsedMillis).coerceAtLeast(0L)
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : ViewModel() {

    var isBottomBarInitialized = false

    private val _floatingPromoState = MutableStateFlow(FloatingPromoState())
    val floatingPromoState = _floatingPromoState.asStateFlow()

    private var floatingPromoClickedAtElapsedRealtime: Long? = null
    private var floatingPromoCooldownJob: Job? = null

    init {
        fetchFloatingPromoButton()
    }

    fun fetchFloatingPromoButton() = viewModelScope.launch {
        _floatingPromoState.update { it.copy(isLoading = true, error = null) }

        vodovozServiceRepository.getFloatingPromoButtons().singleResult()
            .onSuccess { buttons ->
                val button = buttons.firstOrNull()

                _floatingPromoState.update {
                    it.copy(
                        button = button,
                        isLoading = false,
                        error = null,
                        leftScreenNames = button?.leftScreenNames.orEmpty() ,
                        rightScreenNames = button?.rightScreenNames.orEmpty(),
                    )
                }
            }
            .onFailure { throwable ->
                _floatingPromoState.update {
                    it.copy(
                        button = null,
                        isLoading = false,
                        error = throwable,
                        leftScreenNames = emptySet(),
                        rightScreenNames = emptySet(),
                    )
                }
            }
    }

    fun hideFloatingPromoAfterClick(nowElapsedRealtime: Long) {
        floatingPromoClickedAtElapsedRealtime = nowElapsedRealtime
        _floatingPromoState.update { it.copy(isHiddenByClickCooldown = true) }
        scheduleFloatingPromoRestore(FLOATING_PROMO_CLICK_COOLDOWN_MS)
    }

    fun refreshFloatingPromoCooldown(nowElapsedRealtime: Long) {
        val clickedAt = floatingPromoClickedAtElapsedRealtime ?: return
        val remainingMillis = floatingPromoCooldownRemainingMillis(
            clickedAtElapsedRealtime = clickedAt,
            nowElapsedRealtime = nowElapsedRealtime,
        )

        if (remainingMillis == 0L) {
            clearFloatingPromoCooldown()
        } else {
            _floatingPromoState.update { it.copy(isHiddenByClickCooldown = true) }
            scheduleFloatingPromoRestore(remainingMillis)
        }
    }

    private fun scheduleFloatingPromoRestore(delayMillis: Long) {
        floatingPromoCooldownJob?.cancel()
        floatingPromoCooldownJob = viewModelScope.launch {
            delay(delayMillis)
            floatingPromoClickedAtElapsedRealtime = null
            floatingPromoCooldownJob = null
            _floatingPromoState.update { it.copy(isHiddenByClickCooldown = false) }
        }
    }

    private fun clearFloatingPromoCooldown() {
        floatingPromoCooldownJob?.cancel()
        floatingPromoCooldownJob = null
        floatingPromoClickedAtElapsedRealtime = null
        _floatingPromoState.update { it.copy(isHiddenByClickCooldown = false) }
    }

    data class FloatingPromoState(
        val button: FloatingPromoButtonModel? = null,
        val isLoading: Boolean = true,
        val error: Throwable? = null,
        val leftScreenNames: Set<String> = emptySet(),
        val rightScreenNames: Set<String> = emptySet(),
        val isHiddenByClickCooldown: Boolean = false,
    )
}
