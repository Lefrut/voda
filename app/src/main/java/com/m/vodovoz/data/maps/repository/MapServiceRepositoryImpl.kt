package com.m.vodovoz.data.maps.repository

import com.m.vodovoz.data.maps.YandexMapAPI
import com.m.vodovoz.data.maps.YandexMapSDK
import com.m.vodovoz.data.maps.mappers.mapToDomain
import com.m.vodovoz.data.maps.mappers.toData
import com.m.vodovoz.data.maps.mappers.toDomain
import com.m.vodovoz.data.vodovoz_service.mappers.executeRequest
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.domain.general.model.location.MapPointModel
import com.m.vodovoz.domain.general.respository.MapServiceRepository
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.util.extensions.catchResult
import com.yandex.mapkit.search.Address
import com.yandex.mapkit.search.ToponymObjectMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapServiceRepositoryImpl @Inject constructor(
    private val mapApi: YandexMapAPI,
    private val mapSDK: YandexMapSDK,
    private val siteStateManager: SiteStateManager,
) : MapServiceRepository {

    override fun getAddressByGeo(lat: Double, lon: Double): Flow<Result<MapAddressModel>> {
        return executeRequest(
            request = {
                mapApi.getAddressByGeo(
                    geocode = "$lon, $lat",
                    apiKey = siteStateManager.siteStateSnapshot.geocoderKey
                )
            },
            mapper = { yandexGeoResponseDTO ->
                yandexGeoResponseDTO.toDomain()
                    ?: throw IllegalArgumentException("Address can't be null")
            }
        )
    }

    override fun getAddressesInMoscowByQuery(query: String): Flow<Result<List<String>>> =
        flow {
            emit(Result.success(mapSDK.getSuggestsInMoscow(query).mapToDomain()))
        }.catchResult()


    private fun findFirstNonEmptyName(
        components: List<Address.Component>,
        kinds: List<Address.Component.Kind>,
    ): String? {
        return components.firstOrNull { component ->
            component.name.isNotBlank() && component.kinds.any { it in kinds }
        }?.name
    }

    override fun searchAddressInMoscow(address: String): Flow<Result<MapAddressModel>> =
        flow {
            val yandexSearchResponse = mapSDK.searchAddressInMoscow(address)
            val geoObject = yandexSearchResponse.collection.children[0].obj


            val toponymObjectMetadata =
                geoObject?.metadataContainer?.getItem(ToponymObjectMetadata::class.java)
            val yandexAddress = toponymObjectMetadata?.address
            val yandexAddressComponents = yandexAddress?.components ?: emptyList()

            val point = geoObject?.geometry?.getOrNull(0)?.point

            val city = findFirstNonEmptyName(
                components = yandexAddressComponents,
                kinds = listOf(
                    Address.Component.Kind.LOCALITY,
                    Address.Component.Kind.DISTRICT,
                    Address.Component.Kind.AREA,
                    Address.Component.Kind.PROVINCE
                )
            ) ?: ""

            val street = findFirstNonEmptyName(
                components = yandexAddressComponents,
                kinds = listOf(
                    Address.Component.Kind.STREET,
                    Address.Component.Kind.ROUTE
                )
            ) ?: ""
            val house = findFirstNonEmptyName(
                components = yandexAddressComponents,
                kinds = listOf(
                    Address.Component.Kind.HOUSE,
                    Address.Component.Kind.ENTRANCE
                )
            ) ?: ""

            emit(
                Result.success(
                    MapAddressModel(
                        point = point?.toDomain()
                            ?: throw IllegalArgumentException("Search point can't be null"),
                        name = toponymObjectMetadata?.address?.formattedAddress ?: geoObject.name
                        ?: address,
                        city = city,
                        street = street,
                        house = house
                    )
                )
            )
        }.catchResult()

    override fun getRoutes(
        start: MapPointModel,
        end: MapPointModel,
    ): Flow<Result<List<MapPointModel>>> = flow {
        val points = mapSDK.getRoutes(start.toData(), end.toData())
        emit(Result.success(points.mapToDomain()))
    }.catchResult()


}