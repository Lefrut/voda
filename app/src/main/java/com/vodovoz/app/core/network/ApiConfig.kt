package com.vodovoz.app.core.network

object ApiConfig {
    private const val VODOVOZ_PROTOCOL = "https://"

    private const val VODOVOZ_IP = "m.vodovoz.ru/"
    //private const val VODOVOZ_IP = "vodovoz.net/"

    var VODOVOZ_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_IP"

    private const val MAPKIT_PROTOCOL = "https://"
    private const val MAPKIT_IP = "geocode-maps.yandex.ru"

    const val MAPKIT_URL = "$MAPKIT_PROTOCOL$MAPKIT_IP"

    const val RUTUBE_URL = "https://rutube.ru/video/"
    const val YOUTUBE_URL = "https://www.youtube.com/watch?v=/"


}

object VodovozWebConfig {

    private const val VODOVOZ_PROTOCOL = "https://"
    //todo - change to prod. url
    private const val VODOVOZ_DOMAIN = "vodovoz.net/"

    const val VODOVOZ_URL = "$VODOVOZ_PROTOCOL$VODOVOZ_DOMAIN"
    const val VODOVOZ_PATH = "newmobile_new/"

    const val ABOUT_PAYMENT_URL = "${VODOVOZ_URL}newmobile/informatsiya/oplata.php"
    const val ABOUT_DELIVERY_URL = "${VODOVOZ_URL}newmobile/informatsiya/dosytavka.php"
    const val ABOUT_SHOP_URL = "${VODOVOZ_URL}newmobile/informatsiya/omagazine.php"

    fun toFullUrl(suffix: String): String {
        return VODOVOZ_URL.removeSuffix("/") + suffix
    }



}