package com.m.vodovoz.core.network

data object WebConfig {

    private const val MAPKIT_PROTOCOL = "https://"
    private const val MAPKIT_IP = "geocode-maps.yandex.ru"

    const val MAPKIT_URL = "$MAPKIT_PROTOCOL$MAPKIT_IP"

    const val RUTUBE_URL = "https://rutube.ru/video/"
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v=/"

}

data object VodovozWebConfig {

    private const val VODOVOZ_PROTOCOL = "https://"
    private const val VODOVOZ_CONFIG_DOMAIN = "m.vodovoz.ru/"

    var isTestMode = false
        private set
    const val VODOVOZ_BASE_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_CONFIG_DOMAIN"
    const val VODOVOZ_PATH = "newmobile_new/"

    @Volatile
    private var currentUrl = VODOVOZ_BASE_URL
    private var testModeUrl = VODOVOZ_BASE_URL

    val VODOVOZ_URL: String
        get() = currentUrl

    val VODOVOZ_API_URL: String
        get() = buildUrl(VODOVOZ_PATH)

    val ABOUT_SHOP_URL: String
        get() = buildUrl("${VODOVOZ_PATH}informatsiya/omagazine.php")

    fun setProdMode(userUrl: String) {
        isTestMode = false
        currentUrl = userUrl.ifBlank { VODOVOZ_BASE_URL }
    }

    fun setTestMode(testUrl: String): Boolean {
        if (testUrl.isBlank()) return false
        isTestMode = true
        testModeUrl = testUrl
        currentUrl = testUrl
        return true
    }

    fun setAuthUrl(userUrl: String): String {
        currentUrl = userUrl.ifBlank {
            if (isTestMode) testModeUrl else VODOVOZ_BASE_URL
        }
        return currentUrl
    }

    fun completeAuth(userUrl: String): String? {
        val actualUrl = setAuthUrl(userUrl)
        return actualUrl.takeUnless { isTestMode }
    }

    fun resetAuthUrl(): String? {
        if (isTestMode) {
            currentUrl = testModeUrl
            return null
        }
        currentUrl = VODOVOZ_BASE_URL
        return currentUrl
    }

    fun buildUrl(path: String): String {
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        return currentUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
