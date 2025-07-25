package com.vodovoz.app.feature.profile

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.model.VodovozAction
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.profile.model.BonusesPopupWindowUi
import com.vodovoz.app.feature.profile.model.ProfileCardUi
import com.vodovoz.app.feature.profile.model.ProfileChatItemUi
import com.vodovoz.app.feature.profile.model.ProfileChatsPopupWindowUi
import com.vodovoz.app.feature.profile.model.ProfileMenuItemUi
import com.vodovoz.app.feature.profile.model.ProfilePopupWindowUi
import com.vodovoz.app.feature.profile.model.ProfileWalletItemUi
import com.vodovoz.app.feature.profile.model.UserInfoBlockUi
import com.vodovoz.app.feature.profile.model.mapToUi
import com.vodovoz.app.feature.profile.model.toUi
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class ProfileFlowViewModel @Inject constructor(
    private val siteStateManager: SiteStateManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<ProfileFlowViewModel.ProfileState, ProfileFlowViewModel.ProfileEvents>(
    ProfileState()
) {

    init {
        viewModelScope.launch { delay(150) }.invokeOnCompletion {
            fetchProfileDetails()
        }
    }

    fun fetchProfileDetails() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = if (dataState.uiState != ProfileUiState.Profile) ProfileUiState.Loading else s.uiState)
        }

        val bonusesPopupWindowDeferred =
            async { vodovozServiceRepository.getBonusesPopupWindow().singleResult() }
        val profileDetailsResult = vodovozServiceRepository.getProfileDetails().singleResult()
        val bonusesPopupWindow = bonusesPopupWindowDeferred.await().getOrNull()?.toUi()

        profileDetailsResult.onSuccess { profileDetails ->
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = ProfileUiState.Profile,
                    banners = profileDetails.banners.mapToUi(),
                    userInfoBlock = profileDetails.userInfoBlock.toUi(),
                    cards = profileDetails.cards.mapToUi(),
                    smallMenu = profileDetails.smallMenu.mapToUi(),
                    normalMenu = profileDetails.normalMenu.mapToUi(),
                    walletItems = profileDetails.walletItems.mapToUi(),
                    currentBonusesBSData = bonusesPopupWindow
                )
            }
        }.onFailure { t ->

            val uiState = when {
                t is UserNotLoginException && t.placeholder != null -> ProfileUiState.UserNotFound(
                    placeholder = t.placeholder.toUi(),
                )

                else -> ProfileUiState.Error
            }

            uiStateListener.updateData { s ->
                s.copy(uiState = uiState)
            }
        }
    }


    fun refresh() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = true)
        }
        fetchProfileDetails().join()

        uiStateListener.updateData { s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun navigateToLoginOrRegister() = viewModelScope.launch {
        if (!siteStateManager.smsEnabled()) {
            eventListener.emit(ProfileEvents.GoToLoginByEmail)
        } else {
            eventListener.emit(ProfileEvents.GoToLogin)
        }
    }

    fun navigateToUserData() = viewModelScope.launch {
        eventListener.emit(ProfileEvents.GoToUserData)
    }

    fun activateMenuItem(menuItem: ProfileMenuItemUi) = viewModelScope.launch {
        val popupWindow = menuItem.popupWindow

        if (popupWindow != null) {
            uiStateListener.updateData { s ->
                s.copy(
                    showSupportingBS = true,
                    currentSupportingBSData = popupWindow
                )
            }
        } else {
            eventListener.emit(ProfileEvents.GoByMenuItemId(menuItem.id))
        }
    }

    fun showAdvertisingBottomSheet(advertising: AboutAdvertisingUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentAdvertising = advertising,
                showAdvertisingBS = true
            )
        }
    }

    fun closeAdvertisingBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showAdvertisingBS = false
            )
        }
    }

    fun activateBannerAction(banner: BannerUi) = viewModelScope.launch {
        eventListener.emit(ProfileEvents.ActivateVodovozAction(banner.action))
    }

    fun closeSupportingBottomSheet() {
        uiStateListener.updateData { s ->
            s.copy(showSupportingBS = false)
        }
    }

    fun copyUserId(text: String) = viewModelScope.launch {
        val userId = text.trim().filter { c -> c.isDigit() }
        eventListener.emit(ProfileEvents.Copy(userId))
    }

    fun navigateByChatItem(chatItem: ProfileChatItemUi) = viewModelScope.launch {
        if(chatItem.id == "") closeSupportingBottomSheet()
        eventListener.emit(ProfileEvents.GoByChatItemId(chatItem.id, chatItem.navigationData))
    }

    fun activateWalletItem(walletItem: ProfileWalletItemUi) = viewModelScope.launch {
        when (walletItem.id) {
            "balance" -> {
                showTextBottomSheet(walletItem.popupWindow ?: return@launch)
            }

            "bonus" -> {
                uiStateListener.updateData { s ->
                    s.copy(showBonusesBS = true)
                }
            }

            else -> {}
        }
    }

    private fun showTextBottomSheet(data: ProfilePopupWindowUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showTextBS = true,
                currentTextBSData = data
            )
        }
    }

    fun closeTextBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showTextBS = false)
        }
    }

    fun activateProfileCard(profileCard: ProfileCardUi) = viewModelScope.launch {
        when (profileCard.id) {
            "otziv" -> {
                eventListener.emit(ProfileEvents.GoToWaitFeedbackProducts)
            }

            "treker" -> {
                eventListener.emit(ProfileEvents.GoToWaterApp)
            }

            "" -> {
                showTextBottomSheet(profileCard.popupWindow ?: return@launch)
            }
        }
    }

    fun hideBonusesBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showBonusesBS = false)
        }
    }

    fun copyText(text: String) = viewModelScope.launch {
        eventListener.emit(ProfileEvents.Copy(text))
    }

    fun navigateToBonusesConditions(bonusesPopupWindow: BonusesPopupWindowUi) =
        viewModelScope.launch {
            val url = bonusesPopupWindow.url
            if (bonusesPopupWindow.browser) {
                eventListener.emit(ProfileEvents.OpenUrl(url))
            } else {
                eventListener.emit(ProfileEvents.GoToWebView(url, bonusesPopupWindow.button.name))
            }
        }

    fun changeBonusesSubscribe(subscribe: Boolean) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                currentBonusesBSData = s.currentBonusesBSData?.copy(warmAboutExpiration = subscribe)
            )
        }
        vodovozServiceRepository.updateBonusesSubscribe(subscribe).singleResult()
    }


    @Immutable
    data class ProfileState(
        val uiState: ProfileUiState = ProfileUiState.Loading,
        val userInfoBlock: UserInfoBlockUi = UserInfoBlockUi.Empty,
        val banners: List<BannerUi> = emptyList(),
        val cards: List<ProfileCardUi> = emptyList(),
        val walletItems: List<ProfileWalletItemUi> = emptyList(),
        val smallMenu: List<ProfileMenuItemUi> = emptyList(),
        val normalMenu: List<ProfileMenuItemUi> = emptyList(),
        val showAdvertisingBS: Boolean = false,
        val showSupportingBS: Boolean = false,
        val showBonusesBS: Boolean = false,
        val showTextBS: Boolean = false,
        val showRefreshIndicator: Boolean = false,

        val currentTextBSData: ProfilePopupWindowUi? = null,
        val currentSupportingBSData: ProfileChatsPopupWindowUi = ProfileChatsPopupWindowUi.Empty,
        val currentBonusesBSData: BonusesPopupWindowUi? = null,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
    ) : State

    @Stable
    sealed interface ProfileUiState {

        data object Loading : ProfileUiState
        data object Profile : ProfileUiState
        data class UserNotFound(val placeholder: VodovozPlaceholderUi) : ProfileUiState

        data object Error : ProfileUiState

    }

    sealed class ProfileEvents : Event {
        data object GoToLogin : ProfileEvents()
        data object GoToUserData : ProfileEvents()
        data object GoToLoginByEmail : ProfileEvents()
        data object GoToWaterApp : ProfileEvents()
        data object GoToWaitFeedbackProducts : ProfileEvents()

        data class GoByMenuItemId(val itemId: String) : ProfileEvents()
        data class ActivateVodovozAction(val action: VodovozAction) : ProfileEvents()
        data class Copy(val value: String) : ProfileEvents()
        data class GoByChatItemId(val chatId: String, val data: String) : ProfileEvents()
        data class OpenUrl(val url: String) : ProfileEvents()
        data class GoToWebView(val url: String, val title: String) : ProfileEvents()
    }

}