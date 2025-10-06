package com.m.vodovoz.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.domain.general.model.exceptions.UserBlockedException
import com.m.vodovoz.domain.general.model.exceptions.UserNotLoginException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.base.model.AppState
import com.m.vodovoz.ui.base.model.SplashFileState
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val siteStateManager: SiteStateManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val cookieManager: CookieManager,
) : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState = _appState.asStateFlow()

    private val _androidSplash = MutableStateFlow(true)
    val androidSplash = _androidSplash.asStateFlow()

    private val _fileState = MutableStateFlow<SplashFileState>(SplashFileState.Loading)
    val fileState = _fileState.asStateFlow()

    fun updateCookieIfNeeded() = viewModelScope.launch {
        if (cookieManager.isOldCookie() && appState.value == AppState.App) {
            vodovozServiceRepository.relogin().singleResult()
        }
    }

    fun fetchAppConfig() = viewModelScope.launch {
        _appState.update { AppState.Loading }

        val siteStateDeferred = async { siteStateManager.requestSiteState() }
        val reloginResultDeferred = async { vodovozServiceRepository.relogin().singleResult() }

        val siteState = siteStateDeferred.await()

        if (siteState == null) {
            _appState.update { AppState.ErrorLoading }
            return@launch
        } else if (!siteState.isActive || siteState.data != null) {
            _appState.update { AppState.Blocked }
            return@launch
        }

        val reloginResult = reloginResultDeferred.await()

        reloginResult.onFailure { t ->
            when (t) {
                is UserNotLoginException -> {
                    _appState.update { AppState.App }
                }

                is UserBlockedException -> {
                    _appState.update { AppState.UserError }
                }

                else -> {
                    _appState.update { AppState.ErrorLoading }
                }
            }
        }.onSuccess {
            _appState.update { AppState.App }
        }
    }

    fun setAppState() = viewModelScope.launch {
        _appState.update { AppState.App }
    }

    fun hideAndroidSplash() = viewModelScope.launch {
        _androidSplash.update { false }
    }

    fun setFileState(state: SplashFileState) {
        _fileState.update { state }
    }
}