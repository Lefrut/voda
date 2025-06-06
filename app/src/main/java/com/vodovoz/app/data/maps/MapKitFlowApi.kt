package com.vodovoz.app.data.maps

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface MapKitFlowApi {

    //Получить Cookie Session Id
    @GET("/1.x/")
    suspend fun getAddressByGeo(
        @Query("apikey") apiKey: String? = null,
        @Query("geocode") geocode: String? = null,
        @Query("format") format: String? = "json"
    ): ResponseBody

}