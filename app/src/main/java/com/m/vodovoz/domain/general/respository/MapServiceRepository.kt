package com.m.vodovoz.domain.general.respository

import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import kotlinx.coroutines.flow.Flow

interface MapServiceRepository {

    fun getAddressByGeo(
        lat: Double,
        lon: Double
    ): Flow<Result<MapAddressModel>>

    fun getAddressesInMoscowByQuery(
        query: String
    ): Flow<Result<List<String>>>

    fun searchAddressInMoscow(
        address: String
    ): Flow<Result<MapAddressModel>>

    fun getRoutes(
        start: MapPointModel,
        end: MapPointModel
    ): Flow<Result<List<MapPointModel>>>

}