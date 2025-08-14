package com.vodovoz.app.core.network

data object WebConfig {

    private const val MAPKIT_PROTOCOL = "https://"
    private const val MAPKIT_IP = "geocode-maps.yandex.ru"

    const val MAPKIT_URL = "$MAPKIT_PROTOCOL$MAPKIT_IP"

    const val RUTUBE_URL = "https://rutube.ru/video/"
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v=/"

}

data object VodovozWebConfig {

    private const val VODOVOZ_PROTOCOL = "https://"

    //todo - change to prod. url "m.vodovoz.ru/"
    private const val VODOVOZ_DOMAIN = "m.vodovoz.ru/"

    var VODOVOZ_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_DOMAIN"
    const val VODOVOZ_PATH = "newmobile_new/"

    val ABOUT_PAYMENT_URL = "${VODOVOZ_URL}newmobile/informatsiya/oplata.php"
    val ABOUT_DELIVERY_URL = "${VODOVOZ_URL}newmobile/informatsiya/dosytavka.php"
    val ABOUT_SHOP_URL = "${VODOVOZ_URL}newmobile/informatsiya/omagazine.php"

    fun toFullUrl(path: String): String {
        return VODOVOZ_URL.removeSuffix("/") + path
    }


}