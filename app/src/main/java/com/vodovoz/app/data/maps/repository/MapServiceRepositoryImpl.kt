package com.vodovoz.app.data.maps.repository

import com.vodovoz.app.data.maps.YandexMapAPI
import com.vodovoz.app.data.maps.mappers.toDomain
import com.vodovoz.app.data.vodovoz_service.mappers.executeRequest
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import com.vodovoz.app.domain.general.respository.MapServiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapServiceRepositoryImpl @Inject constructor(
    private val mapApi: YandexMapAPI,
) : MapServiceRepository {

    override fun getAddressByGeo(lat: Double, lon: Double): Flow<Result<MapAddressModel>> {
        return executeRequest(
            request = {
                mapApi.getAddressByGeo(geocode = "$lon, $lat")
            },
            mapper = { yandexGeoResponseDTO ->
                yandexGeoResponseDTO.toDomain() ?: throw IllegalArgumentException("Address can't be null")
            }
        )
    }


}