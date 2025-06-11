package com.vodovoz.app.data.maps

import com.yandex.mapkit.search.SuggestItem

interface YandexMapSDK {

    suspend fun getAddressesInMoscow(query: String): List<SuggestItem>

    suspend fun getAddressInfo(address: String): YandexSearchResponse

}