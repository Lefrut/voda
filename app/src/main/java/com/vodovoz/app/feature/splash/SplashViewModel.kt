package com.vodovoz.app.feature.splash

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.feature.splash.model.SplashEvent
import com.vodovoz.app.feature.splash.model.SplashState
import com.vodovoz.app.feature.splash.model.SplashUiState
import com.vodovoz.app.ui.mvi.MviViewModel
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
) : MviViewModel<SplashState, SplashEvent>(SplashState()) {

    init {
        syncFavorites()
    }

    fun sendFirebaseToken() = viewModelScope.launch {
        firebaseTokenManager.sendFirebaseToken()
    }

    private fun syncFavorites() = viewModelScope.launch {
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
                uiState = SplashUiState.Animation
            )
        }
    }

    fun hideAndroidSplash() = viewModelScope.launch {
        _events.emit(SplashEvent.HideAndroidSplash)
    }

}