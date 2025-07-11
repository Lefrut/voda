package com.vodovoz.app.common.account

import androidx.annotation.Keep
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.common.datastore.DataStoreRepository
import com.yandex.metrica.YandexMetrica
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountManager @Inject constructor(
    private val dataStoreRepository: DataStoreRepository,
) {

    private val accountIdListener = MutableStateFlow<Long?>(null)
    fun observeAccountId() = accountIdListener.asStateFlow()

    fun fetchAccountId(): Long? {
        val id = accountIdListener.value ?: fetchUserId()
        accountIdListener.value = id
        return id
    }

    private fun fetchUserId() = dataStoreRepository.getInt(USER_ID)?.toLong()

    fun updateUserId(userId: Long) {
        dataStoreRepository.putInt(USER_ID, userId.toInt())
        accountIdListener.value = userId
    }

    fun removeUserId() {
        dataStoreRepository.remove(USER_ID)
        accountIdListener.value = null
    }

    fun fetchUserToken() =
        dataStoreRepository.getString(USER_TOKEN)

    fun updateUserToken(userToken: String) {
        dataStoreRepository.putString(USER_TOKEN, userToken)
    }

    fun removeUserToken() {
        dataStoreRepository.remove(USER_TOKEN)
    }

    fun fetchUserSettings(): UserSettings {
        val email = dataStoreRepository.getString(EMAIL) ?: ""
        val password = dataStoreRepository.getString(PASSWORD) ?: ""
        return UserSettings(email, password)
    }

    fun updateLastLoginSetting(settings: UserSettings) {
        with(dataStoreRepository) {
            putString(EMAIL, settings.email)
            putString(PASSWORD, settings.password)
        }
    }

    fun saveUseBio(use: Boolean) {
        dataStoreRepository.putBoolean(USE_BIO, use)
    }

    fun fetchUseBio(): Boolean {
        return dataStoreRepository.getBoolean(USE_BIO) ?: false
    }

    fun isAlreadyLogin() = fetchUserId() != null

    @Keep
    fun reportEvent(text: String, eventParam: String? = null) {
        if (!BuildConfig.DEBUG) {
            val eventParameters = "{\"UserID\":\"${accountIdListener.value ?: "0"}\"" +
                    if (eventParam != null) ",$eventParam}" else "}"
            //YandexMetrica.reportEvent(text, eventParameters) //todo release
        }
    }

    @Keep
    fun reportError(text: String, throwable: Throwable? = null) {
        if (!BuildConfig.DEBUG) {
            //YandexMetrica.reportError(text, throwable) //todo release
        }
    }

    data class UserSettings(
        val email: String,
        val password: String,
    )

    companion object {
        private const val USER_ID = "User_ID"
        private const val USER_TOKEN = "User_token"
        private const val EMAIL = "Email"
        private const val PASSWORD = "Password"
        private const val USE_BIO = "USE_BIO"
    }

}