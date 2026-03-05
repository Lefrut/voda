package com.m.vodovoz.core.network

import javax.inject.Singleton


@Singleton
class UrlMananger(){

}

data object WebConfig {

    private const val MAPKIT_PROTOCOL = "https://"
    private const val MAPKIT_IP = "geocode-maps.yandex.ru"

    const val MAPKIT_URL = "$MAPKIT_PROTOCOL$MAPKIT_IP"

    const val RUTUBE_URL = "https://rutube.ru/video/"
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v=/"

}

data object VodovozWebConfig {

    private const val VODOVOZ_PROTOCOL = "https://"

    //todo - vodovoz.ru/
    private const val VODOVOZ_CONFIG_DOMAIN = "m.vodovoz.ru/"
    private const val VODOVOZ_TEST_DOMAIN = "vodovoz.net/"

    const val VODOVOZ_BASE_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_CONFIG_DOMAIN"


    var VODOVOZ_URL = VODOVOZ_BASE_URL
        private set

    fun setUrl(url: String): String{
        VODOVOZ_URL = url
        return VODOVOZ_URL
    }

    const val VODOVOZ_PATH = "newmobile_new/"
    private val VODOVOZ_URL_PATH = "$VODOVOZ_URL$VODOVOZ_PATH"
    private val VODOVOZ_INFO_URL = "${VODOVOZ_URL_PATH}informatsiya/"

    val ABOUT_SHOP_URL = "${VODOVOZ_INFO_URL}omagazine.php"


}