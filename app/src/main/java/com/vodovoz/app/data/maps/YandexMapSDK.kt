package com.vodovoz.app.data.maps

import com.yandex.mapkit.search.SuggestItem

interface YandexMapSDK {

    suspend fun getSuggestsInMoscow(query: String): List<SuggestItem>

    suspend fun searchAddressInMoscow(address: String): YandexSearchResponse

}