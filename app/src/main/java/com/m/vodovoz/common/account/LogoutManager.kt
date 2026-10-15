package com.m.vodovoz.common.account

import com.m.vodovoz.common.cart.AppCartManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.token.FirebaseTokenManager
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import com.m.vodovoz.util.extensions.singleResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take

@Singleton
class LogoutManager @Inject constructor(
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val cookieManager: CookieManager,
    private val waterAppHelper: WaterAppHelper,
    private val tabManager: TabManager,
    private val cartManager: AppCartManager,
) {


    fun logout(): Flow<Result<Unit>> = flow {
        val logoutResult = vodovozServiceRepository.logout().singleResult().onSuccess {
            firebaseTokenManager.removeFirebaseToken()
            accountManager.removeUserId()
            accountManager.removeUserToken()
            // Clear the cookie last so an in-flight relogin cannot leave a session behind after
            // the account credentials have been removed.
            cookieManager.removeCookieSessionId()
            cartManager.requestRefresh()

            waterAppHelper.runOrCancelWorkManager(
                WaterApp.DefaultNotificationSettings
            )

            tabManager.updateBottomNavCartState()
        }



        emit(logoutResult)
    }.take(1)

}
