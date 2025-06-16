package com.vodovoz.app.data

import com.vodovoz.app.data.parser.response.map.AddAddressResponseJsonParser.parseAddAddressResponse
import com.vodovoz.app.data.parser.response.map.AddressByGeocodeResponseJsonParser.parseAddressByGeocodeResponse
import com.vodovoz.app.data.parser.response.map.DeleteAddressResponseJsonParser.parseDeleteAddressResponse
import com.vodovoz.app.data.parser.response.map.DeliveryZonesBundleResponseJsonParser.parseDeliveryZonesBundleResponse
import com.vodovoz.app.data.parser.response.map.FetchAddressesSavedResponseJsonParser.parseFetchAddressesSavedResponse
import com.vodovoz.app.data.parser.response.map.UpdateAddressResponseJsonParser.parseUpdateAddressResponse
import com.vodovoz.app.data.parser.response.order.RepeatOrderResponseJsonParser.parseRepeatOrderResponse
import com.vodovoz.app.data.parser.response.ordering.RegOrderResponseJsonParser.parseRegOrderResponse
import com.vodovoz.app.data.parser.response.shipping.FreeShippingDaysResponseJsonParser.parseFreeShippingDaysResponse
import com.vodovoz.app.data.parser.response.shipping.ShippingInfoResponseJsonParser.parseShippingInfoResponse
import com.vodovoz.app.data.maps.MapKitFlowApi
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val api: MainApi,
    private val mapKitApi: MapKitFlowApi,
) {


    suspend fun repeatOrder(
        orderId: Long?,
        userId: Long?,
    ) = api.repeatOrder(
        orderId = orderId,
        userId = userId
    ).parseRepeatOrderResponse()

    /**
     * map
     */

    //Информация о зонах доставки
    suspend fun fetchDeliveryZonesResponse() = api.fetchMapResponse(
        action = "tochkakarta"
    ).parseDeliveryZonesBundleResponse()

    //Адрес по координатам
    suspend fun fetchAddressByGeocodeResponse(
        latitude: Double,
        longitude: Double,
    ) = mapKitApi.getAddressByGeo(
        apiKey = "346ef353-b4b2-44b3-b597-210d62eeb66b",
        geocode = "$longitude,$latitude",
        format = "json"
    ).parseAddressByGeocodeResponse()


    /**
     * Addresses
     */

    //Удалить адресс
    suspend fun deleteAddress(
        addressId: Long?,
        userId: Long?,
    ) = api.fetchAddressResponse(
        addressId = addressId,
        userid = userId,
        action = "del",
        blockId = 102
    ).parseDeleteAddressResponse()

    //Добавить адрес в сохраненные
    suspend fun addAddress(
        locality: String?,
        street: String?,
        house: String?,
        entrance: String?,
        floor: String?,
        office: String?,
        intercom: String?,
        type: Int?,
        userId: Long?,
        lat: String,
        longitude: String,
        length: String,
        fullAddress: String,
    ) = api.fetchAddressResponse(
        locality = locality,
        street = street,
        house = house,
        entrance = entrance,
        floor = floor,
        office = office,
        intercom = intercom,
        type = type,
        userid = userId,
        blockId = 102,
        action = "add",
        fullAddress = fullAddress,
        length = length,
        longAndLat = "$lat,$longitude"
    ).parseAddAddressResponse()

    //Обновить адрес
    suspend fun updateAddress(
        locality: String?,
        street: String?,
        house: String?,
        entrance: String?,
        floor: String?,
        office: String?,
        intercom: String?,
        type: Int?,
        addressId: Long?,
        userId: Long?,
        lat: String,
        longitude: String,
        length: String,
        fullAddress: String,
    ) = api.fetchAddressResponse(
        locality = locality,
        street = street,
        house = house,
        entrance = entrance,
        floor = floor,
        office = office,
        intercom = intercom,
        type = type,
        addressId = addressId,
        userid = userId,
        blockId = 102,
        action = "update",
        fullAddress = fullAddress,
        length = length,
        longAndLat = "$lat,$longitude"
    ).parseUpdateAddressResponse()

    /**
     * Ordering
     */

    suspend fun fetchShippingInfo(
        userId: Long?,
        addressId: Long?,
        date: String?,
        appVersion: String?,
    ) = api.fetchInfoAboutOrderingResponse(
        userId = userId,
        addressId = addressId,
        date = date,
        appVersion = appVersion
    ).parseShippingInfoResponse()

    suspend fun fetchFreeShippingDaysInfoResponse(
        appVersion: String?,
    ) = api.fetchInfoAboutOrderingResponse(appVersion = appVersion).parseFreeShippingDaysResponse()

    suspend fun regOrder(
        orderType: Int?, //Тип заказа (1/2)
        device: String?, //Телефон Android:12 Версия: 1.4.83
        addressId: Long?, //адрес id - (150543)
        date: String?, //23.08.2022
        paymentId: Long?, //Pay method Id
        needOperatorCall: String?, // Y/N
        needShippingAlert: String?, //За 90 минут
        shippingAlertPhone: String?,
        comment: String?,
        totalPrice: Int?, //Итоговая сумма заказа
        shippingId: Long?, //
        shippingPrice: Int?, // цена доставки
        name: String?,
        phone: String?,
        email: String?,
        inn: String?,
        companyName: String?,
        userId: Long?,
        deposit: Int?, //?
        fastShippingPrice: Int?, // 500 р
        extraShippingPrice: Int?, // из delivery
        commonShippingPrice: Int?, //?
        coupon: String?, // передавать из корзины
        shippingIntervalId: Long?, //id Интервал доставки
        overMoney: Int?, //?
        parking: Int?, // числовое значение
        appVersion: String?,
        checkDeliveryValue: Int?,
        useScore: String = "N",
    ) = api.fetchRegOrderResponse(
        orderType = orderType,
        device = device,
        addressId = addressId,
        date = date,
        paymentId = paymentId,
        needOperatorCall = "Y",
        needShippingAlert = needShippingAlert,
        shippingAlertPhone = shippingAlertPhone,
        comment = comment,
        totalPrice = totalPrice,
        shippingId = shippingId,
        shippingPrice = shippingPrice,
        name = name,
        phone = phone,
        email = email,
        inn = inn,
        companyName = companyName,
        userId = userId,
        deposit = deposit,
        fastShippingPrice = fastShippingPrice,
        extraShippingPrice = extraShippingPrice,
        commonShippingPrice = commonShippingPrice,
        coupon = coupon,
        shippingIntervalId = shippingIntervalId,
        overMoney = overMoney,
        parking = parking,
        appVersion = appVersion,
        checkDeliveryValue = checkDeliveryValue,
        useScore = useScore
    ).parseRegOrderResponse()


    suspend fun logout(userId: Long) = api.logout(userId = userId)

}






