package com.m.vodovoz.common.account

import androidx.annotation.Keep
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.common.datastore.DataStorePrefs
import com.yandex.metrica.YandexMetrica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
) {

    private val accountIdListener = MutableStateFlow<Long?>(null)
    fun observeAccountId() = accountIdListener.asStateFlow()

    fun fetchAccountId(): Long? {
        val id = accountIdListener.value ?: fetchUserId()
        accountIdListener.value = id
        return id
    }

    private fun fetchUserId() = dataStorePrefs.getInt(USER_ID)?.toLong()

    fun updateUserId(userId: Long) {
        dataStorePrefs.putInt(USER_ID, userId.toInt())
        accountIdListener.value = userId
    }

    fun removeUserId() {
        dataStorePrefs.remove(USER_ID)
        accountIdListener.value = null
    }

    fun fetchUserToken() =
        dataStorePrefs.getString(USER_TOKEN)

    fun updateUserToken(userToken: String) {
        dataStorePrefs.putString(USER_TOKEN, userToken)
    }

    fun removeUserToken() {
        dataStorePrefs.remove(USER_TOKEN)
    }

    fun fetchUserSettings(): UserSettings {
        val email = dataStorePrefs.getString(EMAIL) ?: ""
        val password = dataStorePrefs.getString(PASSWORD) ?: ""
        return UserSettings(email, password)
    }

    fun updateLastLoginSetting(settings: UserSettings) {
        with(dataStorePrefs) {
            putString(EMAIL, settings.email)
            putString(PASSWORD, settings.password)
        }
    }

    fun saveUseBio(use: Boolean) {
        dataStorePrefs.putBoolean(USE_BIO, use)
    }

    fun fetchUseBio(): Boolean {
        return dataStorePrefs.getBoolean(USE_BIO) ?: false
    }

    fun isAlreadyLogin() = fetchUserId() != null

    @Keep
    fun reportEvent(text: String, eventParam: String? = null) {
        if (!BuildConfig.DEBUG) {
            val eventParameters = "{\"UserID\":\"${accountIdListener.value ?: "0"}\"" +
                    if (eventParam != null) ",$eventParam}" else "}"

            YandexMetrica.reportEvent(text, eventParameters)
        }
    }

    @Keep
    fun reportError(text: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) {
            YandexMetrica.reportError(text, throwable)
        }
    }

    data class UserSettings(
        val email: String,
        val password: String,
    )

    val pendingDeeplinkFlow = dataStorePrefs.getStringFlow(
        PENDING_DEEPLINK_KEY
    ).filter { deeplink ->
        !deeplink.isNullOrBlank()
    }.filterNotNull().onEach { setPendingDeeplink("") }

    fun setPendingDeeplink(deeplink: String){
        dataStorePrefs.putString(PENDING_DEEPLINK_KEY, deeplink)
    }

    companion object {
        private const val USER_ID = "User_ID"
        private const val USER_TOKEN = "User_token"
        private const val EMAIL = "Email"
        private const val PASSWORD = "Password"
        private const val USE_BIO = "USE_BIO"

        private const val PENDING_DEEPLINK_KEY = "pending_deeplink"
        const val ORDERS_DEEPLINK = "orders"
    }

}