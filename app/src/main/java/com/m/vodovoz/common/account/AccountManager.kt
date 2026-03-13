package com.m.vodovoz.common.account

import androidx.annotation.Keep
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.common.datastore.DataStorePrefs
import com.m.vodovoz.core.network.VodovozUrlManager
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import io.appmetrica.analytics.AppMetrica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManager @Inject constructor(
    private val dataStorePrefs: DataStorePrefs,
    private val urlManager: VodovozUrlManager,
) {

    private val _accountIdListener = MutableStateFlow<Long?>(null)
    fun observeAccountId() = _accountIdListener.asStateFlow()

    fun fetchAccountId(): Long? {
        val id = _accountIdListener.value ?: fetchUserId()
        _accountIdListener.value = id
        return id
    }

    private fun fetchUserId() = dataStorePrefs.getInt(USER_ID)?.toLong()

    fun updateUserId(userId: Long) {
        dataStorePrefs.putInt(USER_ID, userId.toInt())
        _accountIdListener.value = userId
    }

    fun removeUserId() {
        dataStorePrefs.remove(USER_ID)
        _accountIdListener.value = null
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
    fun reportEvent(text: String, eventParam: String? = null) = runCatching {
        if (!BuildConfig.DEBUG) {
            val eventParameters = "{\"UserID\":\"${_accountIdListener.value ?: "0"}\"" +
                    if (eventParam != null) ",$eventParam}" else "}"

            AppMetrica.reportEvent(text, eventParameters)
        }

    }

    @Keep
    fun reportError(text: String, throwable: Throwable? = null) = runCatching {
        if (!BuildConfig.DEBUG) {
            AppMetrica.reportError(text, throwable)
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

    fun setPendingDeeplink(deeplink: String) {
        dataStorePrefs.putString(PENDING_DEEPLINK_KEY, deeplink)
    }

    fun updateUserUrl(url: String) {
        urlManager.setUrl(url)
        dataStorePrefs.putString(USER_URL, url)
    }

    val userUrlFlow
        get() = dataStorePrefs.getStringFlow(USER_URL).map { url ->
            url.orEmpty().ifEmpty { VodovozWebConfig.VODOVOZ_BASE_URL }
        }

    suspend fun getUserUrl(): String {
        return userUrlFlow.firstOrNull().orEmpty().ifEmpty {
            VodovozWebConfig.VODOVOZ_BASE_URL
        }
    }

    companion object {
        private const val USER_ID = "User_ID"
        private const val USER_TOKEN = "User_token"
        private const val EMAIL = "Email"
        private const val PASSWORD = "Password"
        private const val USE_BIO = "USE_BIO"

        private const val PENDING_DEEPLINK_KEY = "pending_deeplink"
        const val ORDERS_DEEPLINK_ID = "orders"

        private const val USER_URL = "user_url"
    }

}