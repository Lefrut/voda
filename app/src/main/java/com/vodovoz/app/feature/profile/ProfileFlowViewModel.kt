package com.vodovoz.app.feature.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.model.VodovozAction
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.profile.model.ProfileCardUi
import com.vodovoz.app.feature.profile.model.ProfileChatItemUi
import com.vodovoz.app.feature.profile.model.ProfileChatsPopupWindowUi
import com.vodovoz.app.feature.profile.model.ProfileMenuItemUi
import com.vodovoz.app.feature.profile.model.ProfileWalletItemUi
import com.vodovoz.app.feature.profile.model.ProfilePopupWindowUi
import com.vodovoz.app.feature.profile.model.UserInfoBlockUi
import com.vodovoz.app.feature.profile.model.mapToUi
import com.vodovoz.app.feature.profile.model.toUi
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileFlowViewModel @Inject constructor(
    private val repository: MainRepository,
    private val cookieManager: CookieManager,
    private val cartManager: CartManager,
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
    private val tabManager: TabManager,
    private val waterAppHelper: WaterAppHelper,
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
            s.copy(uiState = if (s.normalMenu.isEmpty()) ProfileUiState.Loading else s.uiState)
        }
        val profileDetailsResult = vodovozServiceRepository.getProfileDetails().singleResult()

        profileDetailsResult.onSuccess { profileDetails ->
            uiStateListener.updateData { s ->
                s.copy(
                    uiState = ProfileUiState.Profile,
                    banners = profileDetails.banners.mapToUi(),
                    userInfoBlock = profileDetails.userInfoBlock.toUi(),
                    cards = profileDetails.cards.mapToUi(),
                    smallMenu = profileDetails.smallMenu.mapToUi(),
                    normalMenu = profileDetails.normalMenu.mapToUi(),
                    walletItems = profileDetails.walletItems.mapToUi()
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
        uiStateListener.updateData {s ->
            s.copy(showRefreshIndicator = true)
        }
        fetchProfileDetails().join()

        uiStateListener.updateData {s ->
            s.copy(showRefreshIndicator = false)
        }
    }

    fun navigateToLoginOrRegister() = viewModelScope.launch {
        if (!siteStateManager.smsEnabled()) {
            eventListener.emit(ProfileEvents.GoToRegister)
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

    fun closeSupportingBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showSupportingBS = false
            )
        }
    }

    fun copyUserId(text: String) = viewModelScope.launch {
        val userId = text.trim().filter { c -> c.isDigit() }
        eventListener.emit(ProfileEvents.Copy(userId))
    }

    fun navigateByChatItem(chatItem: ProfileChatItemUi) = viewModelScope.launch {
        eventListener.emit(ProfileEvents.GoByChatItemId(chatItem.id, chatItem.navigationData))
    }

    fun activateWalletItem(walletItem: ProfileWalletItemUi) = viewModelScope.launch {
        when (walletItem.id) {
            "balance" -> {
                showTextBottomSheet(walletItem.popupWindow ?: return@launch)
            }

            "bonus" -> {
                showTextBottomSheet(walletItem.popupWindow ?: return@launch)
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
        when(profileCard.id){
            "otziv" ->{
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
        val showTextBS: Boolean = false,
        val showRefreshIndicator: Boolean =false,

        val currentTextBSData: ProfilePopupWindowUi? = null,
        val currentSupportingBSData: ProfileChatsPopupWindowUi = ProfileChatsPopupWindowUi.Empty,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,
        ) : State

    @Immutable
    sealed interface ProfileUiState {

        data object Loading : ProfileUiState
        data object Profile : ProfileUiState
        data class UserNotFound(val placeholder: VodovozPlaceholderUi) : ProfileUiState

        data object Error : ProfileUiState

    }

    sealed class ProfileEvents : Event {
        data object GoToLogin : ProfileEvents()
        data object GoToUserData : ProfileEvents()
        data object GoToRegister : ProfileEvents()
        data object GoToWaterApp : ProfileEvents()
        data object GoToWaitFeedbackProducts : ProfileEvents()

        data class GoByMenuItemId(val itemId: String) : ProfileEvents()
        data class ActivateVodovozAction(val action: VodovozAction) : ProfileEvents()
        data class Copy(val value: String) : ProfileEvents()
        data class GoByChatItemId(val chatId: String, val data: String) : ProfileEvents()
    }

}