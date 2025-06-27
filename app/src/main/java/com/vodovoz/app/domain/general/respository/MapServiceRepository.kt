package com.vodovoz.app.domain.general.respository

import com.vodovoz.app.design_system.model.MapPointUi
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import com.vodovoz.app.domain.general.model.location.MapPointModel
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

    fun getRoute(start: MapPointModel, end: MapPointModel): Flow<Result<List<MapPointModel>>>

}