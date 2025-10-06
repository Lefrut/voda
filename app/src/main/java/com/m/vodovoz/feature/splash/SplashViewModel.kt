package com.m.vodovoz.feature.splash

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.account.LogoutManager
import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.token.FirebaseTokenManager
import com.m.vodovoz.feature.splash.model.SplashEvent
import com.m.vodovoz.feature.splash.model.SplashState
import com.m.vodovoz.feature.splash.model.SplashUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
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
        updateState { s -> s.copy(uiState = SplashUiState.Error) }
    }

    fun refreshApp(showAnimation: Boolean) = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = if (showAnimation) SplashUiState.Animation else SplashUiState.Placeholder)
        }
        sendEvent(SplashEvent.RefreshApp)
    }

    fun changeToAnimation() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = SplashUiState.Animation)
        }
//        updateState { s ->
//            s.copy(
//                filePath = splashFile.absolutePath,
//                uiState = if (s.uiState is SplashUiState.Error) SplashUiState.Error else SplashUiState.Animation
//            )
//        }
    }

    fun hideAndroidSplash() = viewModelScope.launch {
        sendEvent(SplashEvent.HideAndroidSplash)
    }

    fun logout() = viewModelScope.launch {
        logoutManager.logout().singleResult()
    }

}