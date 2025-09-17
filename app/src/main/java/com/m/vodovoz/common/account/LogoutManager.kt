package com.m.vodovoz.common.account

import com.m.vodovoz.common.cart.CartManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.token.FirebaseTokenManager
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import com.m.vodovoz.util.extensions.catchResult
import com.m.vodovoz.util.extensions.singleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutManager @Inject constructor(
    private val accountManager: AccountManager,
    private val cartManager: CartManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val cookieManager: CookieManager,
    private val waterAppRepository: WaterAppRepository,
    private val waterAppHelper: WaterAppHelper,
) {


    fun logout(): Flow<Result<Unit>> = flow {
        vodovozServiceRepository.logout().singleResult()
        firebaseTokenManager.removeFirebaseToken()
        cookieManager.removeCookieSessionId()
        accountManager.removeUserId()
        accountManager.removeUserToken()
        cartManager.clearCart()
        waterAppRepository.clear()
        waterAppHelper.runOrCancelWorkManager(
            WaterApp.NotificationSettings.Default
        )

        emit(Result.success(Unit))
    }.catchResult()

}