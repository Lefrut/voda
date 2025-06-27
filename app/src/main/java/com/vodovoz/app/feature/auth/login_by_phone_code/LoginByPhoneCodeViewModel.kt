package com.vodovoz.app.feature.auth.login_by_phone_code

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.account.LoginManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.auth.login_by_phone_code.model.LoginByPhoneCodeEvent
import com.vodovoz.app.feature.auth.login_by_phone_code.model.LoginByPhoneCodeState
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@Stable
@HiltViewModel
class LoginByPhoneCodeViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val siteStateManager: SiteStateManager,
    private val loginManager: LoginManager,
    savedStateHandle: SavedStateHandle,
) : MviViewModel<LoginByPhoneCodeState, LoginByPhoneCodeEvent>(
    LoginByPhoneCodeState(phone = formatPhone(savedStateHandle["phoneNumber"] ?: ""))
) {

    private val waitRequestCodeSeconds: Int = savedStateHandle["waitRequestCodeSeconds"] ?: 60

    init {
        startWaiting(waitRequestCodeSeconds.toLong())
    }

    fun navigateBack() = viewModelScope.launch {
        _events.emit(LoginByPhoneCodeEvent.GoBack)
    }

    fun changeCode(code: String) = viewModelScope.launch {
        val newCode = code.filter { c -> c.isDigit() }.take(4)
        _state.update { s ->
            s.copy(code = newCode)
        }

        if(newCode.length == 4){ sendCode() }

    }

    fun sendCode() = viewModelScope.launch {
        val currentCode = stateSnapshot.code.take(4)
        loadingByPhone(currentCode)
    }

    fun requestCode() = viewModelScope.launch {
        _state.update { s ->
            s.copy(requestCodeLoading = true)
        }

        val smsUrl = siteStateManager.siteStateFlow.value?.smsUrl ?: ""

        val requestPhoneCodeResult = vodovozServiceRepository
            .requestPhoneCode(smsUrl, stateSnapshot.phone)
            .singleResult()

        _state.update { s ->
            s.copy(requestCodeLoading = false)
        }

        requestPhoneCodeResult.onSuccess { requestCodeModel ->
            startWaiting(requestCodeModel.remainingSeconds.toLong())
        }

    }

    private fun loadingByPhone(code: String) = viewModelScope.launch {
        _state.update { s ->
            s.copy(blockScreen = true)
        }

        val smsUrl = siteStateManager.siteStateFlow.value?.smsUrl ?: ""

        val loginByPhoneResult = vodovozServiceRepository.loginByPhone(
            url = smsUrl,
            code = code,
            phone = stateSnapshot.phone
        ).singleResult()

        loginByPhoneResult.onSuccess { userAuthInfo ->
            loginManager.initializeUserSession(
                userAuthInfo.userId,
                userAuthInfo.token
            )

            _state.update { s ->
                s.copy(blockScreen = false)
            }

            _events.emit(LoginByPhoneCodeEvent.RefreshProfile)
        }.onFailure {
            _state.update { s ->
                s.copy(
                    code = "",
                    blockScreen = false
                )
            }
        }


    }

    private fun startWaiting(timerDurationSeconds: Long) =
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { s ->
                s.copy(canRequestCode = false)
            }

            val totalMillis = timerDurationSeconds.seconds.inWholeMilliseconds
            val formatter = DateTimeFormatter.ofPattern("mm:ss")
            val startNano = System.nanoTime()

            while (isActive) {
                val currentNano = System.nanoTime()
                val elapsedMillis = (currentNano - startNano) / 1_000_000
                val remainingMillis =
                    (totalMillis - (elapsedMillis - elapsedMillis % 1000)).coerceAtLeast(0)
                val remainingSeconds = (remainingMillis) / 1_000


                val time = LocalTime.ofSecondOfDay(remainingSeconds)
                _state.update { s ->
                    s.copy(waitSecondsText = time.format(formatter))
                }

                if (remainingMillis <= 0L) {
                    _state.update { s ->
                        s.copy(canRequestCode = true)
                    }
                    break
                }

                val delayToNextSecond = 1_000 - (elapsedMillis % 1_000)
                delay(delayToNextSecond)
            }
        }

    companion object {

        private fun formatPhone(phone: String): String {
            val digits = phone.filter { c -> c.isDigit() }
            return if (digits.length == 11) {
                "+${digits[0]} (${digits.substring(1, 4)}) ${
                    digits.substring(4, 9)
                }-${digits.substring(9)}"
            } else {
                phone
            }
        }
    }

}