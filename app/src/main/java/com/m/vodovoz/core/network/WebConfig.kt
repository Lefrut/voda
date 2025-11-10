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
    //todo - m.vodovoz.ru/
    private const val VODOVOZ_DOMAIN = "m.vodovoz.ru/"

    var VODOVOZ_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_DOMAIN"
    const val VODOVOZ_PATH = "newmobile_new/"
    private val VODOVOZ_URL_PATH = "$VODOVOZ_URL$VODOVOZ_PATH"
    private val VODOVOZ_INFO_URL = "${VODOVOZ_URL_PATH}informatsiya/"

    val ABOUT_SHOP_URL = "${VODOVOZ_INFO_URL}omagazine.php"

    fun toFullUrl(path: String): String {
        return VODOVOZ_URL.removeSuffix("/") + path
    }


}