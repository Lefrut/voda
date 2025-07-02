package com.vodovoz.app.data

import com.vodovoz.app.data.parser.response.map.AddAddressResponseJsonParser.parseAddAddressResponse
import com.vodovoz.app.data.parser.response.map.DeliveryZonesBundleResponseJsonParser.parseDeliveryZonesBundleResponse
import com.vodovoz.app.data.parser.response.map.UpdateAddressResponseJsonParser.parseUpdateAddressResponse
import com.vodovoz.app.data.parser.response.ordering.RegOrderResponseJsonParser.parseRegOrderResponse
import javax.inject.Inject

class MainRepository(
    private val api: MainApi = mainApi,
) {

    @Inject constructor() : this(mainApi)
    /**
     * map
     */

    //Информация о зонах доставки
    suspend fun fetchDeliveryZonesResponse() = api.fetchMapResponse(
        action = "tochkakarta"
    ).parseDeliveryZonesBundleResponse()

    /**
     * Addresses
     */

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
}






