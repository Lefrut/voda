package com.vodovoz.app.common.account

import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.util.extensions.catchResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutManager @Inject constructor(
    private val accountManager: AccountManager,
    private val tabManager: TabManager,
    private val cartManager: CartManager,
    private val waterAppHelper: WaterAppHelper,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val cookieManager: CookieManager,
    private val repository: MainRepository,
) {


    fun logout(): Flow<Result<Unit>> = flow {
        val userId = accountManager.fetchAccountId() ?: return@flow
        //todo - change to vodovozServiceRepository
        repository.logout(userId)
        cookieManager.removeCookieSessionId()
        accountManager.removeUserId()
        accountManager.removeUserToken()
        tabManager.clearBottomNavProfileState()
        cartManager.clearCart()
        waterAppHelper.clearData()
        firebaseTokenManager.removeFirebaseToken()

        emit(Result.success(Unit))
    }.catchResult()

}