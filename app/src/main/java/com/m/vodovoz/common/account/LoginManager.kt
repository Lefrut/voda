package com.m.vodovoz.common.account

import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.token.FirebaseTokenManager
import com.m.vodovoz.core.network.VodovozUrlManager
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginManager @Inject constructor(
    private val accountManager: AccountManager,
    private val likeManager: LikeManager,
    private val firebaseTokenManager: FirebaseTokenManager,
    private val tabManager: TabManager,
    private val urlManager: VodovozUrlManager
) {


    suspend fun initializeUserSession(
        userId: Long,
        userToken: String,
        userUrl: String
    ) {
        urlManager.setUrl(userUrl)
        tabManager.updateBottomNavCartState()
        accountManager.updateUserUrl(userUrl)
        accountManager.updateUserId(userId)
        accountManager.updateUserToken(userToken)
        likeManager.updateLikesAfterLogin()
        firebaseTokenManager.sendFirebaseToken()
    }

}