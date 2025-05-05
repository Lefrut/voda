package com.vodovoz.app.feature.profile.waterapp

import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppActivityLevel
import com.vodovoz.app.feature.profile.waterapp.model.WaterAppUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaterAppViewModel @Inject constructor(
    private val waterAppHelper: WaterAppHelper,
    private val moshi: Moshi,
) : PagingContractViewModel<WaterAppViewModel.WaterAppState, WaterAppViewModel.WaterAppEvents>(
    WaterAppState()
) {

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(WaterAppEvents.GoBack)
    }

    fun moveToUserFields() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                uiState = WaterAppUiState.UserData.Gender
            )
        }
    }

    fun selectGender(man: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                userData = s.userData.copy(
                    gender = if (man) "man" else "girl"
                )
            )
        }
    }

    fun navigateToPreviousStage() = viewModelScope.launch {
        val prevUiState = when (val currentUiState = dataState.uiState) {
            is WaterAppUiState.UserData -> currentUiState.previous() ?: WaterAppUiState.Welcome
            else ->
                currentUiState
        }
        uiStateListener.updateData { s ->
            s.copy(uiState = prevUiState)
        }
    }

    fun navigateToNextStage() = viewModelScope.launch {
        val nextUiState = when (val currentUiState = dataState.uiState) {
            is WaterAppUiState.UserData -> currentUiState.next() ?: WaterAppUiState.WaterGoal
            else -> currentUiState
        }
        uiStateListener.updateData { s ->
            s.copy(uiState = nextUiState)
        }
    }

    fun selectActivityLevel(activityLevel: WaterAppActivityLevel) {
        uiStateListener.updateData { s ->
            s.copy(
                userData = s.userData.copy(sport = activityLevel.value.toString())
            )
        }
    }

    data class WaterAppState(
        val userData: WaterAppHelper.WaterAppUserData = WaterAppHelper.WaterAppUserData(),
        val uiState: WaterAppUiState = WaterAppUiState.Welcome,
    ) : State

    sealed class WaterAppEvents : Event {

        data object GoBack : WaterAppEvents()

    }


}