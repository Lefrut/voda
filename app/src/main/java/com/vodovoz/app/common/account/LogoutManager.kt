package com.vodovoz.app.common.account

import com.vodovoz.app.common.cart.CartManager
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.profile.waterapp.WaterAppHelper
import com.vodovoz.app.util.extensions.catchResult
import com.vodovoz.app.util.extensions.singleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutManager @Inject constructor(
    private val accountManager: AccountManager,
    private val cartManager: CartManager,
    private val waterAppHelper: WaterAppHelper,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val cookieManager: CookieManager
) {


    fun logout(): Flow<Result<Unit>> = flow {
        vodovozServiceRepository.logout().singleResult()
        firebaseTokenManager.removeFirebaseToken()
        cookieManager.removeCookieSessionId()
        accountManager.removeUserId()
        accountManager.removeUserToken()
        cartManager.clearCart()
        waterAppHelper.clearData()

        emit(Result.success(Unit))
    }.catchResult()

}