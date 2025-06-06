package com.vodovoz.app.domain.general.respository

import com.vodovoz.app.domain.general.model.map.MapAddressModel
import kotlinx.coroutines.flow.Flow

interface MapServiceRepository {

    fun getAddressByGeo(
        lat: Double,
        lon: Double
    ): Flow<Result<MapAddressModel>>

}