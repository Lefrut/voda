package com.vodovoz.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.domain.general.model.UserBlockedException
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.base.model.AppState
import com.vodovoz.app.util.extensions.singleResult
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
) : ViewModel() {

    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState = _appState.asStateFlow()

    private val _androidSplash = MutableStateFlow(true)
    val androidSplash = _androidSplash.asStateFlow()

    fun checkAppState() = viewModelScope.launch {
        _appState.update { AppState.Loading }

        val siteStateDeferred = async { siteStateManager.requestSiteState() }
        val reloginResultDeferred = async { vodovozServiceRepository.relogin().singleResult() }

        val siteState = siteStateDeferred.await()

        if (siteState == null) {
            _appState.update { AppState.ErrorLoading }
            return@launch
        } else if (!siteState.isActive) {
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

    fun finishAndroidSplash() = viewModelScope.launch {
        _androidSplash.update { false }
    }
}