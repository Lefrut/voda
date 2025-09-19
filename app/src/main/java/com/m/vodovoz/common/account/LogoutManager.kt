package com.m.vodovoz.common.account

import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.token.FirebaseTokenManager
import com.m.vodovoz.common.water_app.WaterApp
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.domain.general.respository.WaterAppRepository
import com.m.vodovoz.feature.profile.waterapp.WaterAppHelper
import com.m.vodovoz.util.extensions.singleResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.take
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LogoutManager @Inject constructor(
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val cookieManager: CookieManager,
    private val waterAppHelper: WaterAppHelper,
) {


    fun logout(): Flow<Result<Unit>> = flow {
        val logoutResult = vodovozServiceRepository.logout().singleResult()

        val operations = listOf(
            suspend {
                firebaseTokenManager.removeFirebaseToken()
            },
            {
                cookieManager.removeCookieSessionId()
            },
            {
                accountManager.removeUserId()
            },
            {
                accountManager.removeUserToken()
            },
            {
                waterAppHelper.runOrCancelWorkManager(
                    WaterApp.NotificationSettings.Default
                )
            }
        )

        logoutResult.onSuccess {
            operations.forEach { suspendFunction0 ->
                suspendFunction0()
            }
        }


        emit(logoutResult)
    }.take(1)

}