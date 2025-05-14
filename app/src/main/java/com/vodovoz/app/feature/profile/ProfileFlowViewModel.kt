package com.vodovoz.app.feature.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.content.ErrorState
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.itemadapter.Item
import com.vodovoz.app.common.content.toErrorState
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.product.rating.RatingProductManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.data.model.common.ResponseEntity
import com.vodovoz.app.design_system.model.AboutAdvertisingUi
import com.vodovoz.app.design_system.model.BannerUi
import com.vodovoz.app.design_system.model.VodovozPlaceholderUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.design_system.model.toUi
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.model.VodovozAction
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.favorite.mapper.FavoritesMapper
import com.vodovoz.app.feature.home.viewholders.homeproducts.HomeProducts
import com.vodovoz.app.feature.home.viewholders.hometitle.HomeTitle
import com.vodovoz.app.feature.profile.model.ProfileCardUi
import com.vodovoz.app.feature.profile.model.ProfileChatItemUi
import com.vodovoz.app.feature.profile.model.ProfileChatsPopupWindowUi
import com.vodovoz.app.feature.profile.model.ProfileMenuItemUi
import com.vodovoz.app.feature.profile.model.ProfileWalletItemUi
import com.vodovoz.app.feature.profile.model.ProfileWalletPopupWindowUi
import com.vodovoz.app.feature.profile.model.UserInfoBlockUi
import com.vodovoz.app.feature.profile.model.mapToUi
import com.vodovoz.app.feature.profile.model.toUi
import com.vodovoz.app.feature.profile.viewholders.models.ProfileBestForYou
import com.vodovoz.app.feature.profile.viewholders.models.ProfileLogout
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.mapper.CategoryDetailMapper.mapToUI
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileFlowViewModel @Inject constructor(
    private val repository: MainRepository,
    private val cookieManager: CookieManager,
    private val cartManager: CartManager,
    private val likeManager: LikeManager,
    private val ratingProductManager: RatingProductManager,
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
    private val tabManager: TabManager,
    private val waterAppHelper: WaterAppHelper,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<ProfileFlowViewModel.ProfileState, ProfileFlowViewModel.ProfileEvents>(
    ProfileState.idle()
) {

    init {
        viewModelScope.launch {
            siteStateManager.requestSiteState()
        }
        //todo - check user auth else logout
        fetchProfileDetails()
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
                s.copy(uiState = uiState, )
            }
        }
    }


    private fun CoroutineScope.secondLoadTasks(userId: Long) = arrayOf(
        async(Dispatchers.IO) { fetchViewedProductsSlider(POSITION_6_TITLE, POSITION_7, userId) },
        async(Dispatchers.IO) { fetchPersonalProducts(POSITION_8, userId) },
    )

    private suspend fun fetchViewedProductsSlider(
        positionTitle: Int,
        position: Int,
        userId: Long,
    ): List<PositionItem> {
        return runCatching {
            val response = repository.fetchViewedProductsSlider(userId = userId)
            withContext(Dispatchers.Default) {
//                val response = responseBody.parseViewedProductsSliderResponse()
                if (response is ResponseEntity.Success) {
                    val data = listOf(response.data.mapToUI())
                    listOf(
                        PositionItem(
                            position,
                            HomeProducts.fetchHomeProductsByType(
                                data,
                                HomeProducts.VIEWED,
                                position
                            )
                        ),
                        PositionItem(
                            positionTitle,
                            HomeTitle(
                                id = positionTitle,
                                type = HomeTitle.VIEWED_TITLE,
                                name = "Вы смотрели",
                                showAll = false,
                                showAllName = "СМ.ВСЕ",
                                categoryProductsName = if (data.size == 1) {
                                    data.first().name
                                } else {
                                    ""
                                }
                            )
                        )
                    )
                } else {
                    emptyList()
                }
            }
        }
            .onFailure { showNetworkError(it) }
            .getOrDefault(emptyList())
    }

    private suspend fun fetchPersonalProducts(position: Int, userId: Long): List<PositionItem> {
        return runCatching {
            val response = repository.fetchPersonalProducts(
                userId = userId,
                page = 1
            )
            withContext(Dispatchers.Default) {
                if (response is ResponseEntity.Success) {
                    val data = response.data.mapToUI()
                    val item = PositionItem(
                        position,
                        ProfileBestForYou(
                            data = data.copy(
                                productUIList = FavoritesMapper.mapFavoritesListByManager(
                                    "grid",
                                    data.productUIList
                                )
                            )
                        )
                    )
                    listOf(item)
                } else {
                    emptyList()
                }
            }
        }
            .onFailure { showNetworkError(it) }
            .getOrDefault(emptyList())
    }

    private fun showNetworkError(throwable: Throwable) {
        val error = throwable.toErrorState()
        if (error is ErrorState.NetworkError) {
            uiStateListener.value = state.copy(error = error)
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

    fun logout() = viewModelScope.launch {
        val userId = accountManager.fetchAccountId() ?: return@launch
        flow { emit(repository.logout(userId)) }
            .onEach {
                cookieManager.removeCookieSessionId()
            }.firstOrNull()

        accountManager.removeUserId()
        accountManager.removeUserToken()
        tabManager.clearBottomNavProfileState()
        cartManager.clearCart()
        eventListener.emit(ProfileEvents.Logout)
        waterAppHelper.clearData()
    }

    fun checkLogin() {
        viewModelScope.launch {
            uiStateListener.value = state.copy(data = state.data.copy(isLogin = isLoginAlready()))
        }
    }

    fun isLoginAlready() = accountManager.isAlreadyLogin()

    fun changeCart(productId: Long, quantity: Int, oldQuan: Int) {
        viewModelScope.launch {
            cartManager.add(id = productId, oldCount = oldQuan, newCount = quantity)
        }
    }

    fun changeFavoriteStatus(productId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            likeManager.like(productId, !isFavorite)
        }
    }

    fun changeRating(productId: Long, rating: Float, oldRating: Float) {
        viewModelScope.launch {
            ratingProductManager.rate(productId, rating = rating, oldRating = oldRating)
        }
    }

    fun repeatOrder(orderId: Long) {
        val userId =
            accountManager.fetchAccountId() ?: return
        uiStateListener.value = state.copy(loadingPage = true, error = null)
        viewModelScope.launch {
            flow {
                emit(
                    repository.repeatOrder(
                        userId = userId,
                        orderId = orderId
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        cartManager.updateCartListState(true)
                        uiStateListener.value = state.copy(loadingPage = false, error = null)
                        eventListener.emit(ProfileEvents.GoToCart)
                    } else {
                        uiStateListener.value =
                            state.copy(
                                loadingPage = false,
                                error = ErrorState.Error()
                            )
                    }
                }
                .flowOn(Dispatchers.Default)
                .catch {
                    debugLog { "repeat order error ${it.localizedMessage}" }
                    uiStateListener.value =
                        state.copy(error = it.toErrorState(), loadingPage = false)
                }
                .collect()
        }
    }

    fun recyclerReady(isReady: Boolean = true) {
        uiStateListener.value = state.copy(state.data.copy(isSecondLoad = isReady))
    }

    fun loadMore() {
        val curPage = state.page
        if (curPage != null && state.data.isSecondLoad && !state.loadMore) {
            uiStateListener.value = state.copy(loadMore = true, page = curPage + 1)
            loadMoreProducts()
        }
    }

    private fun loadMoreProducts() {
        val userId =
            accountManager.fetchAccountId() ?: return
        viewModelScope.launch {

            flow {
                emit(
                    repository.fetchPersonalProducts(
                        userId = userId,
                        page = state.page
                    )
                )
            }
                .onEach { response ->
                    if (response is ResponseEntity.Success) {
                        val data = response.data.mapToUI()
                        val bestForYou = ProfileBestForYou(
                            data = data.copy(
                                productUIList = (state.data.items.last() as ProfileBestForYou).data.productUIList + FavoritesMapper.mapFavoritesListByManager(
                                    "grid",
                                    data.productUIList
                                )
                            )
                        )
                        uiStateListener.value = state.copy(
                            data = state.data.copy(
                                items = state.data.items.mapIndexed { index, item ->
                                    if (index == state.data.items.size - 1) {
                                        bestForYou
                                    } else {
                                        item
                                    }
                                }
                            ),
                            loadMore = false,
                        )
                    }

                }.collect()
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
                showBalanceBottomSheet(walletItem)
            }

            "bonus" -> {

            }

            else -> {}
        }
    }

    private fun showBalanceBottomSheet(walletItem: ProfileWalletItemUi) = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showBalanceBS = true,
                currentBalanceBSData = walletItem.popupWindow
            )
        }
    }

    fun closeBalanceBottomSheet() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showBalanceBS = false)
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
            "anketa" -> {

            }
        }
    }


    @Immutable
    data class ProfileState(
        val positionItems: List<PositionItem>,
        val items: List<Item>,
        val isLogin: Boolean = true,
        val isSecondLoad: Boolean = false,

        val uiState: ProfileUiState = ProfileUiState.Loading,
        val userInfoBlock: UserInfoBlockUi = UserInfoBlockUi.Empty,
        val banners: List<BannerUi> = emptyList(),
        val cards: List<ProfileCardUi> = emptyList(),
        val walletItems: List<ProfileWalletItemUi> = emptyList(),
        val smallMenu: List<ProfileMenuItemUi> = emptyList(),
        val normalMenu: List<ProfileMenuItemUi> = emptyList(),
        val showAdvertisingBS: Boolean = false,
        val showSupportingBS: Boolean = false,
        val showBalanceBS: Boolean = false,
        val showRefreshIndicator: Boolean =false,

        val currentBalanceBSData: ProfileWalletPopupWindowUi? = null,
        val currentSupportingBSData: ProfileChatsPopupWindowUi = ProfileChatsPopupWindowUi.Empty,
        val currentAdvertising: AboutAdvertisingUi = AboutAdvertisingUi.Empty,

        ) : State {
        companion object {
            fun idle(): ProfileState {
                return ProfileState(
                    positionItems = emptyList(),
                    items = emptyList()
                )
            }

            fun fetchStaticItems(): List<PositionItem> {
                return listOf(
                    PositionItem(
                        POSITION_5,
                        ProfileLogout()
                    )
                )
            }
        }
    }

    @Immutable
    sealed interface ProfileUiState {

        data object Loading : ProfileUiState
        data object Profile : ProfileUiState
        data class UserNotFound(val placeholder: VodovozPlaceholderUi) : ProfileUiState

        data object Error : ProfileUiState

    }

    sealed class ProfileEvents : Event {
        data object Logout : ProfileEvents()
        data object GoToCart : ProfileEvents()
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

    data class PositionItem(
        val position: Int,
        val item: Item,
    )

    companion object {
        const val POSITION_1 = 1
        const val POSITION_2 = 2
        const val POSITION_3 = 3
        const val POSITION_4 = 4
        const val POSITION_5 = 5
        const val POSITION_6_TITLE = 6
        const val POSITION_7 = 7
        const val POSITION_8 = 8
    }
}