package com.vodovoz.app.common.account

import com.vodovoz.app.common.like.LikeManager
import com.vodovoz.app.common.token.FirebaseTokenManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginManager @Inject constructor(
    private val accountManager: AccountManager,
    private val likeManager: LikeManager,
    private val firebaseTokenManager: FirebaseTokenManager
) {


    suspend fun initializeUserSession(
        userId: Long,
        userToken: String
    ) {
        accountManager.updateUserId(userId)
        accountManager.updateUserToken(userToken)
        likeManager.updateLikesAfterLogin()
        firebaseTokenManager.sendFirebaseToken()
    }

}