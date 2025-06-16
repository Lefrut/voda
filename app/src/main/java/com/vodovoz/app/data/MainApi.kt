package com.vodovoz.app.data

import com.vodovoz.app.BuildConfig
import com.vodovoz.app.feature.search.qrcode.model.QrCodeModel
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

interface MainApi {


    /**
     * repeat order && order details
     */

    @GET("/newmobile/korzina/function/povtor/index.php")
    suspend fun repeatOrder(
        @Query("orderID") orderId: Long? = null,
        @Query("userid") userId: Long? = null,
    ): ResponseBody

    /**
     * map
     */

    @GET("/newmobile/profile/karta/index.php")
    suspend fun fetchMapResponse(
        @Query("action") action: String? = null,
    ): ResponseBody

    @GET("newmobile/user.php?action=logout")
    suspend fun logout(@Query("userid") userId: Long? = null): Response<ResponseBody>

    /**
     * Addresses
     */

    @GET("/newmobile/address.php")
    suspend fun fetchAddressResponse(
        @Query("city") locality: String? = null,
        @Query("domofon") intercom: String? = null,
        @Query("entrance") entrance: String? = null,
        @Query("flat") office: String? = null,
        @Query("floor") floor: String? = null,
        @Query("house") house: String? = null,
        @Query("street") street: String? = null,
        @Query("userid") userid: Long? = null,
        @Query("iblock_id") blockId: Int? = null,
        @Query("action") action: String? = null,
        @Query("tip") type: Int? = null,
        @Query("addressid") addressId: Long? = null,
        @Query("leghtkm") length: String? = null,
        @Query("polnadres") fullAddress: String? = null,
        @Query("ktochka") longAndLat: String? = null,
    ): ResponseBody

    /**
     * Ordering
     */

    @GET("newmobile/getActualDelivery.php")
    suspend fun fetchInfoAboutOrderingResponse(
        @Query("address_id") addressId: Long? = null,
        @Query("userid") userId: Long? = null,
        @Query("curDate") date: String? = null,
        @Query("versiyaan") appVersion: String?,
    ): ResponseBody

    @GET("newmobile/doorder.php")
    suspend fun fetchRegOrderResponse(
        @Query("type") orderType: Int?, //Тип заказа (1/2)
        @Query("device") device: String?, //Телефон Android:12 Версия: 1.4.83
        @Query("profile_buyer") addressId: Long?, //адрес id - (150543)
        @Query("date") date: String?, //23.08.2022
        @Query("payment") paymentId: Long?, //Pay method Id
        @Query("operator") needOperatorCall: String?, // Y/N
        @Query("driver") needShippingAlert: String?,//За 90 минут
        @Query("dopphone") shippingAlertPhone: String?,//телефон для водителя
        @Query("comment") comment: String?,
        @Query("summ") totalPrice: Int?, //Итоговая сумма заказа
        @Query("delivery_id") shippingId: Long?, //
        @Query("summdelivery") shippingPrice: Int?, // цена доставки
        @Query("fio_f") name: String?,
        @Query("phone_f") phone: String?,
        @Query("email_f") email: String?,
        @Query("inn") inn: String?,
        @Query("companyname") companyName: String?,
        @Query("userid") userId: Long?,
        @Query("zalogcena") deposit: Int?, //?
        @Query("sroch") fastShippingPrice: Int?, // 500 р
        @Query("nacenka") extraShippingPrice: Int?, // из delivery
        @Query("obichd") commonShippingPrice: Int?, //?
        @Query("kupon") coupon: String?, // передавать из корзины
        @Query("indos") shippingIntervalId: Long?, //id Интервал доставки
        @Query("sdacha") overMoney: Int?, //?
        @Query("parkovka") parking: Int?, // числовое значение,
        @Query("versiyaan") appVersion: String?,
        @Query("nettovar") checkDeliveryValue: Int?,
        @Query("schet") useScore: String = "N",
    ): ResponseBody
}