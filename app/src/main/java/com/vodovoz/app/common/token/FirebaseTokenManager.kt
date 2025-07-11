package com.vodovoz.app.common.token

import com.google.firebase.messaging.FirebaseMessaging
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.notification.NotificationConfig
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.singleResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Singleton
class FirebaseTokenManager @Inject constructor(
    private val accountManager: AccountManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
) {

    suspend fun sendFirebaseToken() {
        val token = fetchFirebaseToken()
        val userId = accountManager.fetchAccountId()
        debugLog { "sendFirebaseToken: token $token" }
        if (token != null && userId != null) {
            vodovozServiceRepository.sendFirebaseToken(token).singleResult()
        }
    }

    suspend fun removeFirebaseToken() {
        val token = fetchFirebaseToken()
        val userId = accountManager.fetchAccountId()
        debugLog { "removeFirebaseToken: token $token" }
        if (token != null && userId != null) {
            vodovozServiceRepository.sendFirebaseToken(token).singleResult()
        }
    }

    private suspend fun fetchFirebaseToken(): String? {
        return suspendCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    FirebaseMessaging.getInstance().subscribeToTopic(
                        NotificationConfig.TOPIC_GLOBAL
                    )
                    continuation.resume(token)
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
                .addOnCanceledListener {
                    continuation.resume(null)
                }
        }
    }
}