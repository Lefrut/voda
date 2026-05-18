package com.m.vodovoz.core.network

import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.feature.sitestate.SiteStateManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking


data object WebConfig {

    private const val MAPKIT_PROTOCOL = "https://"
    private const val MAPKIT_IP = "geocode-maps.yandex.ru"

    const val MAPKIT_URL = "$MAPKIT_PROTOCOL$MAPKIT_IP"

    const val RUTUBE_URL = "https://rutube.ru/video/"
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v=/"

}

private class VodovozUrlProvider(
    private val accountManager: AccountManager,
    private val siteStateManager: SiteStateManager,
) {
    private val _isTestModeState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isTestModeState = _isTestModeState.asStateFlow()

    val currentUrlFlow: Flow<String>
        get() = combine(
            siteStateManager.siteStateFlow,
            accountManager.userUrlFlow,
            isTestModeState
        ) { config, userUrl, isTestMode ->


            if (isTestMode) config?.testUrl ?: VodovozWebConfig.VODOVOZ_BASE_URL
            else userUrl.ifEmpty { VodovozWebConfig.VODOVOZ_BASE_URL }
        }.distinctUntilChanged()

    val currentUrl: String
        get() = runBlocking {
            currentUrlFlow.firstOrNull() ?: VodovozWebConfig.VODOVOZ_BASE_URL
        }

    fun setTestMode() {
        _isTestModeState.update { true }
    }

    fun setProdMode() {
        _isTestModeState.update { false }
    }
}

data object VodovozWebConfig {

    private const val VODOVOZ_PROTOCOL = "https://"


    private const val VODOVOZ_CONFIG_DOMAIN = "m.vodovoz.ru/"
    private const val VODOVOZ_TEST_DOMAIN = "vodovoz.net/"

    var isTestMode = false
    const val VODOVOZ_BASE_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_CONFIG_DOMAIN"


    var VODOVOZ_URL = VODOVOZ_BASE_URL
        private set

    fun setUrl(url: String): String {
        VODOVOZ_URL = url
        return VODOVOZ_URL
    }

    const val VODOVOZ_PATH = "newmobile_new/"
    private val VODOVOZ_URL_PATH = "$VODOVOZ_URL$VODOVOZ_PATH"
    private val VODOVOZ_INFO_URL = "${VODOVOZ_URL_PATH}informatsiya/"

    val ABOUT_SHOP_URL = "${VODOVOZ_INFO_URL}omagazine.php"


}