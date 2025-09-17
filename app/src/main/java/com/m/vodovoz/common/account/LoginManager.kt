package com.m.vodovoz.common.account

import com.m.vodovoz.common.like.LikeManager
import com.m.vodovoz.common.token.FirebaseTokenManager
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