package com.m.vodovoz.data.maps

import com.m.vodovoz.common.constants.AppKeys
import com.m.vodovoz.data.maps.model.YandexGeoResponseDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YandexMapAPI {

    @GET("/1.x/")
    suspend fun getAddressByGeo(
        @Query("geocode") geocode: String? = null,
        @Query("apikey") apiKey: String,
        @Query("format") format: String? = "json",
    ): Response<YandexGeoResponseDTO>


}