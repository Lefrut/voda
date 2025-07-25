package com.vodovoz.app.feature.splash

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.LogoutManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.feature.splash.model.SplashEvent
import com.vodovoz.app.feature.splash.model.SplashState
import com.vodovoz.app.feature.splash.model.SplashUiState
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
@Stable
class SplashViewModel @Inject constructor(
    private val likeManager: LikeManager,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val logoutManager: LogoutManager,
) : MviViewModel<SplashState, SplashEvent>(SplashState()) {

    fun sendFirebaseToken() = viewModelScope.launch {
        firebaseTokenManager.sendFirebaseToken()
    }

    fun syncFavorites() = viewModelScope.launch {
        likeManager.syncFavoritesFromLocal()
    }

    fun setErrorUiState() = viewModelScope.launch {
        _state.update { s -> s.copy(uiState = SplashUiState.Error) }
    }

    fun refreshApp(showAnimation: Boolean) = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = if (showAnimation) SplashUiState.Animation else SplashUiState.Placeholder)
        }
        _events.emit(SplashEvent.RefreshApp)
    }

    fun changeToAnimation(splashFile: File) = viewModelScope.launch {

        _state.update { s ->
            s.copy(
                filePath = splashFile.absolutePath,
                uiState = if (s.uiState is SplashUiState.Error) SplashUiState.Error else SplashUiState.Animation
            )
        }
    }

    fun hideAndroidSplash() = viewModelScope.launch {
        _events.emit(SplashEvent.HideAndroidSplash)
    }

    fun logout() = viewModelScope.launch {
        logoutManager.logout().singleResult()
    }

}