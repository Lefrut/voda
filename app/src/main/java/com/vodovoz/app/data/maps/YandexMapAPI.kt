package com.vodovoz.app.data.maps

import com.vodovoz.app.data.maps.model.YandexGeoResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YandexMapAPI {

    @GET("/1.x/")
    suspend fun getAddressByGeo(
        @Query("geocode") geocode: String? = null,
        @Query("apikey") apiKey: String? = "6216dbf2-a506-48be-b53a-b54bfdb6803f", //346ef353-b4b2-44b3-b597-210d62eeb66b
        @Query("format") format: String? = "json",
    ): Response<YandexGeoResponseDTO>


}