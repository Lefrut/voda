package com.m.vodovoz.data.vodovoz_service

import com.m.vodovoz.BuildConfig
import com.m.vodovoz.data.vodovoz_service.model.AllBottlesDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.AnalogsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.BannerDTO
import com.m.vodovoz.data.vodovoz_service.model.BrandSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.CATEGORY_NODE_DTO
import com.m.vodovoz.data.vodovoz_service.model.CancelOrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.CertificateActivationDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.FieldsDTO
import com.m.vodovoz.data.vodovoz_service.model.FormDTO
import com.m.vodovoz.data.vodovoz_service.model.MiniSearchRecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.OrderMenuDTO
import com.m.vodovoz.data.vodovoz_service.model.OrderPlaceholderDTO
import com.m.vodovoz.data.vodovoz_service.model.OrderQuestionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.PopularCategoriesDTO
import com.m.vodovoz.data.vodovoz_service.model.PopupWindowDTO
import com.m.vodovoz.data.vodovoz_service.model.PresentDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductCommentsDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionsDTO
import com.m.vodovoz.data.vodovoz_service.model.QuestionnairesDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.QuestionnairesWelcomeDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.RAZDEL_DTO
import com.m.vodovoz.data.vodovoz_service.model.SearchRecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.SiteStateResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.StoriesDTO
import com.m.vodovoz.data.vodovoz_service.model.SuperTopAndBottomSectionsDTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_DATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozErrorResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddAddressDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddressLabelsDTO
import com.m.vodovoz.data.vodovoz_service.model.address.AddressesDTO
import com.m.vodovoz.data.vodovoz_service.model.address.MapZonesDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.AuthDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.LoginByPhoneDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.RequestCodeDTO
import com.m.vodovoz.data.vodovoz_service.model.auth.UserAuthInfoDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.BottomCartDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.CartDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.RecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.catalog.CatalogDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.certificate.BuyCertificateDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.delivery_date.DeliveryDateDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.filters.FiltersDTO
import com.m.vodovoz.data.vodovoz_service.model.notification_settings.NotificationSettingsDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderCallYouDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderingDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrdersHistoryDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.RecipientDTO
import com.m.vodovoz.data.vodovoz_service.model.order.RecipientDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.WhereMyOrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.payment_method.PaymentMethodDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.ProductDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.profile.BonusesPopupWindowDTO
import com.m.vodovoz.data.vodovoz_service.model.profile.ProfileDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.AllServicesDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.ServiceDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.services.ServiceOrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.unrated_products.UnratedProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.user_data.UserDataDTO
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.feature.cart.ordering.model.OrderingMenuItemUi
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface VodovozService {


    /**
     * Past purchases requests
     * */
    @GET("profile/historyorder/proshlpokipki.php?action=getLastFifty")
    suspend fun getPastPurchasesDetails(

        @Query("nav") page: Int = 1,
        @Query("sort") sort: String? = null,
        @Query("ascdesc") order: String? = null,
        @Query("sect") categoryId: Int? = null,
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    /**
     * Push requests
     * */
    @GET("osnova/userpushapi.php?action=token")
    suspend fun sendFirebaseToken(

        @Query("token") token: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("osnova/userpushapi.php?action=del")
    suspend fun removeFirebaseToken(

        @Query("token") token: String,
    ): Response<VodovozResponseDTO<String>>

    /**
     * Service requests
     * */
    @GET("glavnaya/uslygi/index.php?action=spisok")
    suspend fun getAllServicesDetails(
    ): Response<VodovozResponseDTO<AllServicesDetailsDTO>>

    @GET("glavnaya/uslygi/index.php?action=details")
    suspend fun getServiceDetails(
        @Query("id") serviceId: Int?,
    ): Response<VodovozResponseDTO<ServiceDetailsDTO>>

    @GET("osnova/form/yslygiform.php?action=detail")
    suspend fun getServiceOrderDetails(

        @Query("tip") serviceType: String,
    ): Response<VodovozResponseDTO<ServiceOrderDetailsDTO>>

    @GET("osnova/form/yslygiform.php?action=otpravka")
    suspend fun orderService(

        @Query("tip") serviceType: String,
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>


    /**
     * Order requests
     * */
    @GET("oformlenie/confirm.php?action=glav&versiya=${BuildConfig.VERSION_NAME}")
    suspend fun doOrder(
        @Query("adresid") addressId: Long,
        @Query("date") deliveryDate: String,
        @Query("indos") deliveryTimeInterval: String,
        @Query("fio_f") userFIO: String,
        @Query("phone_f") userPhone: String,
        @Query("email_f") userEmail: String?,
        @Query("payment") paymentMethodId: Long,
        @Query("sdacha") paymentChange: String?,
        @Query("kupon") coupon: String?,
        @Query("device", encoded = true) deviceInfo: String?,
        @Query("driver") notifyDriverId: String?,
        @Query(PaymentMethodItemModel.BALANCE_ID) useBalance: String?,
        @Query(PaymentMethodItemModel.BONUSES_ID) useBonuses: String?,
        @Query(FieldModel.BONUS_ID) bonuses: Int?,
        @QueryMap queries: Map<String, String>? = null,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    @GET("oformlenie/zvonok.php?action=vampozvonit")
    suspend fun getOrderCallYouDetails(
        @Query("adresid") addressId: Long,
        @QueryMap queryParams: Map<String, String>,

        ): Response<VodovozResponseDTO<OrderCallYouDetailsDTO>>

    @GET("oformlenie/profil.php?action=glav")
    suspend fun getRecipientDetails(
        @Query("adresid") addressId: Long,

        ): Response<VodovozResponseDTO<RecipientDetailsDTO>>

    @GET("oformlenie/profil.php?action=glav&proverka=Y")
    suspend fun getRecipient(
        @Query("adresid") addressId: Long,

        ): Response<VodovozResponseDTO<RecipientDTO>>

    @GET("oformlenie/profil.php?action=update")
    suspend fun sendOrderRecipient(
        @Query("adresid") addressId: Long,

        @QueryMap params: Map<String, String>,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/function/povtor/index.php")
    suspend fun repeatOrder(
        @Query("id") orderId: Long,

        ): Response<VodovozResponseDTO<String>>


    @GET("oformlenie/oplata.php?action=glav")
    suspend fun getPaymentMethodDetails(

        @Query("adresid") addressId: Long,
        @Query("date") date: String,
        @QueryMap queryParams: Map<String, String>,
    ): Response<VodovozResponseDTO<PaymentMethodDetailsDTO>>

    @GET("oformlenie/date.php?action=glav")
    suspend fun getDeliveryDateDetails(

        @Query("adresid") addressId: Long,
        @Query("date") date: String? = null,
        @QueryMap queryParams: Map<String, String>,
    ): Response<VodovozResponseDTO<DeliveryDateDetailsDTO>>

    @GET("oformlenie/oformlenie.php?action=glav")
    suspend fun getOrderingDetails(
        @Query("adresid") addressId: Long?,
        @Query("date") date: String?,
        @Query("indos") timeInterval: String?,
        @Query("coupon") coupon: String?,
        @Query(PaymentMethodItemModel.BALANCE_ID) useBalance: String?,
        @Query(PaymentMethodItemModel.BONUSES_ID) useBonuses: String?,
        @Query(FieldModel.BONUS_ID) bonuses: Int?,
        @QueryMap queryParams: Map<String, String>,
    ): Response<VodovozResponseDTO<OrderingDetailsDTO>>

    @GET("profile/historyorder/voditel.php")
    suspend fun getWhereMyOrderDetails(

        @Query("id") orderId: Long,
        @Query("vodila") driverId: String,
    ): Response<VodovozResponseDTO<WhereMyOrderDetailsDTO>>

    @GET("osnova/form/otmenazakaz.php?action=detail")
    suspend fun getCancelOrderDetails(

        @Query("idzakaz") orderId: Long,
    ): Response<VodovozResponseDTO<CancelOrderDetailsDTO>>

    @GET("osnova/form/otmenazakaz.php?action=otpravka")
    suspend fun cancelOrder(

        @Query("idzakaz") orderId: Long,
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<String>>

    @GET("osnova/form/voprosozakaze.php?action=otpravka")
    suspend fun sendOrderQuestion(

        @Query("idzakaz") orderId: Long,
        @QueryMap queryMap: Map<String, String>,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    @GET("osnova/form/voprosozakaze.php?action=detail")
    suspend fun getOrderQuestionDetails(

        @Query("idzakaz") orderId: Long,
    ): Response<VodovozResponseDTO<OrderQuestionDetailsDTO>>


    @GET("profile/historyorder/detailzakaz.php?action=detail")
    suspend fun getOrderDetails(

        @Query("id") orderId: Long,
    ): Response<VodovozResponseDTO<OrderDetailsDTO>>

    /**
     * OrdersHistory requests
     * */
    @GET("profile/historyorder/spisokzakazov.php?action=spisok")
    suspend fun getOrdersHistoryDetails(

        @Query("nav") page: Int = 1,
        @Query("status") statuses: String? = null,
        @Query("search") search: String? = null,
    ): Response<VodovozResponseDTO<OrdersHistoryDetailsDTO>>

    /**
     * Addresses requests
     * */
    @GET("oformlenie/address.php?action=get")
    suspend fun getAddresses(

    ): Response<VodovozResponseDTO<AddressesDTO>>

    @GET("oformlenie/address.php?action=add&iblock_id=102")
    suspend fun addAddress(

        @Query("ktochka") geo: String,
        @Query("city") city: String? = null,
        @Query("street") street: String? = null,
        @Query("leghtkm") fromMoscowToAddressKm: String,
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<Long>>

    @GET("oformlenie/address.php?action=update&iblock_id=102")
    suspend fun updateAddress(
        @Query("addressid") addressId: Long,

        @Query("ktochka") geo: String?,
        @Query("city") city: String? = null,
        @Query("street") street: String? = null,
        @Query("leghtkm") fromMoscowToAddressKm: String? = null,
        @QueryMap params: Map<String, String>,
    ): Response<VodovozResponseDTO<String?>>

    @GET("oformlenie/address.php?action=del")
    suspend fun deleteAddress(
        @Query("addressid") addressId: Int,

        ): Response<VodovozResponseDTO<String>>


    @GET("oformlenie/address.php?action=edit")
    suspend fun getAddAddressDetails(

        @Query("addressid") addressId: Long?,
    ): Response<VodovozResponseDTO<AddAddressDetailsDTO>>

    @GET("oformlenie/metki.php?action=getlist")
    suspend fun getAddressLabels(

    ): Response<VodovozResponseDTO<AddressLabelsDTO>>

    @GET("oformlenie/metki.php?action=add")
    suspend fun addAddressLabel(

        @Query("slovo") label: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("oformlenie/metki.php?action=del")
    suspend fun deleteAddressLabel(

        @Query("slovo") label: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("oformlenie/metki.php?action=delfull")
    suspend fun deleteAllAddressLabels(

    ): Response<VodovozResponseDTO<String>>


    @GET("profile/karta/index.php?action=tochkakarta")
    suspend fun getMapAreas(): Response<VodovozResponseDTO<MapZonesDTO>>

    /**
     * Brand requests
     * */
    @GET("brand.php?action=detail")
    suspend fun getBrandProducts(
        @Query("id") brandId: Long,
        @Query("nav") page: Int = 1,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
        @Query("sect") categoryId: Int? = null,
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("brand.php?action=brand")
    suspend fun getBrands(
        @Query("nav") page: Int? = null,
        @Query("search") search: String? = null,
    ): Response<VodovozResponseDTO<BrandSectionDTO>>


    /**
     * Category requests
     * */
    @GET("razdel/category.php?iblock_id=12")
    suspend fun getCategoryTree(
        @Query("id") categoryId: Long,
    ): Response<VodovozResponseDTO<List<CATEGORY_NODE_DTO>>>


    /**
     * Profile requests
     * */

    @GET("profile/bonus.php?action=glav")
    suspend fun getBonusesPopupWindow(

    ): Response<VodovozResponseDTO<BonusesPopupWindowDTO>>

    @GET("profile/bonus.php?action=glav")
    suspend fun updateBonusesSubscribe(

        @Query("lgb_subscribe") subscribe: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("profile/index.php?action=glav")
    suspend fun getProfileDetails(

    ): Response<VodovozResponseDTO<ProfileDetailsDTO>>

    @GET("profile/index.php?action=details")
    suspend fun getUserData(

    ): Response<VodovozResponseDTO<UserDataDTO>>

    @GET("profile/index.php?action=logout")
    suspend fun logout(

    ): Response<VodovozResponseDTO<String?>>

    @GET("config/userclose.php?action=zakrituser")
    suspend fun deleteAccount(

    ): Response<VodovozResponseDTO<String?>>

    @GET("profile/index.php?action=edit")
    suspend fun updateUserData(

        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<String>>

    @Multipart
    @POST("profile/index.php?action=uploadPhoto")
    suspend fun updateUserAvatar(
        @Part file: MultipartBody.Part,
    ): Response<VodovozResponseDTO<String>>

    @GET("profile/index.php?action=parol")
    suspend fun getChangePasswordDetails(): Response<VodovozResponseDTO<FieldsDTO>>

    @GET("profile/index.php?action=edit")
    suspend fun updatePassword(

        @Query("password") password: String,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    /**
     * Notification settings requests
     * */
    @GET("osnova/form/uvedomlenie.php?action=detail")
    suspend fun getNotificationSettingsDetails(

    ): Response<VodovozResponseDTO<NotificationSettingsDetailsDTO>>

    @GET("osnova/form/uvedomlenie.php?action=otpiska")
    suspend fun updateNotificationSettings(

        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<String>>

    /**
     * Filter requests
     * */
    @GET("razdel/filtercatalog.php?action=getAllProps")
    suspend fun getFilters(@Query("section") categoryId: Int): Response<VodovozResponseDTO<FiltersDTO>>

    @GET("razdel/filtercatalog.php?action=getAllValueOfProps")
    suspend fun getFilterValues(
        @Query("section") categoryId: Int,
        @Query("propCode") filterId: String,
    ): Response<VodovozResponseDTO<List<String>>>

    /**
     *  Certificate requests
     * */
    @GET("osnova/sertificat/activaciya.php?action=glav")
    suspend fun getCertificateActivationDetails(): Response<VodovozResponseDTO<CertificateActivationDetailsDTO>>

    @GET("osnova/sertificat/index.php?action=oformlenie")
    suspend fun buyCertificate(

        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<OrderPlaceholderDTO>>

    @GET("osnova/sertificat/activaciya.php?action=detail")
    suspend fun activateCertificate(

        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<String>>

    @GET("osnova/sertificat/index.php?action=glav")
    suspend fun getBuyCertificateDetails(

    ): Response<VodovozResponseDTO<BuyCertificateDetailsDTO>>

    /**
     * Catalog screen
     * */
    @GET("razdel/category.php?iblock_id=12")
    suspend fun getCatalogDetails(): Response<VodovozResponseDTO<CatalogDetailsDTO>>

    @GET("razdel/index.php?iblock_id=12")
    suspend fun getCategoryProducts(
        @Query("sectionid") categoryId: Long,
        @Query("nav") page: Int = 1,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
        @Query("filter") filters: String? = null,
        @Query("filtervalue") filtersAndValues: String? = null,
        @Query("price_to") priceTo: Float? = null,
        @Query("price_from") priceFrom: Float? = null,
        @QueryMap queries: Map<String, String?> = emptyMap(),
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    /**
     * Search requests
     * */

    @GET("searching/index.php?action=glav")
    suspend fun getSearchRecommendations(): Response<VodovozResponseDTO<SearchRecommendationsDTO>>

    @GET("searching/minipoisk.php?action=glav")
    suspend fun getMiniSearchRecommendations(
        @Query("search") query: String,
    ): Response<VodovozResponseDTO<MiniSearchRecommendationsDTO>>

    @GET("searching/index.php?action=search")
    suspend fun getSearchProducts(
        @Query("search") query: String,
        @Query("nav") page: Int = 1,
        @Query("sect") categoryId: Int? = null,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
        @Query("kamera") isCamera: String? = null,
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    /**
     * Login requests
     * */
    @GET("auth.php?action=glav")
    suspend fun getLoginDetails(): Response<VodovozResponseDTO<AuthDetailsDTO>>

    @GET("auth.php?action=glav&email=Y")
    suspend fun getLoginByEmailDetails(): Response<VodovozResponseDTO<AuthDetailsDTO>>

    @GET("reg.php?action=glav")
    suspend fun getRegisterFields(): Response<VodovozResponseDTO<AuthDetailsDTO>>

    @GET("auth.php?action=otpravka")
    suspend fun loginByEmail(
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<UserAuthInfoDTO>>

    @GET("reg.php?action=otpravka")
    suspend fun register(
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<UserAuthInfoDTO>>


    @GET("{path}?action=tochkakarta")
    suspend fun requestPhoneCode(
        @Path("path", encoded = true) url: String,
        @Query("telefon") phone: String,
        @QueryMap params: Map<String, String>,
    ): Response<VodovozResponseDTO<RequestCodeDTO>>

    @GET("{path}?action=tochkakarta")
    suspend fun loginByPhone(
        @Path("path", encoded = true) path: String,
        @Query("telefon") phone: String,
        @Query("code") code: String,
    ): Response<VodovozResponseDTO<LoginByPhoneDTO>>

    @Headers("Cookie: ")
    @GET("config/openuserid.php?sandroid=${BuildConfig.VERSION_NAME}")
    suspend fun relogin(

        @Query("token") token: String,
    ): Response<VodovozResponseDTO<Boolean>>

    @GET("recoverPass.php?action=glav")
    suspend fun getRecoverPasswordDetails(): Response<VodovozResponseDTO<AuthDetailsDTO>>


    @GET("recoverPass.php?action=otpravka")
    suspend fun recoverPassword(
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    /**
     * Main requests
     * */
    @GET("config/closesait.php?action=saitosnova&android=${BuildConfig.VERSION_NAME}")
    suspend fun getSiteState(): Response<SiteStateResponseDTO>

    /**
     * PreOrder screen
     * */
    @GET("osnova/predzakaz.php?action=predzakaz")
    suspend fun getPreOrderDetails(

        @Query("tovar") productId: Long?,
    ): Response<VodovozResponseDTO<FormDTO>>

    @GET("osnova/predzakaz.php?action=otpravka")
    suspend fun sendPreorder(

        @Query("tovar") productId: Long,
        @QueryMap queries: Map<String, String>,
    ): Response<VodovozErrorResponseDTO>

    /**
     * Cart requests
     * */

    @GET("korzina/minikorzina.php?action=getbasketuser")
    suspend fun getBottomCart(): Response<VodovozResponseDTO<BottomCartDTO>>

    @GET("korzina/doptovary.php?action=doptovar")
    suspend fun getAdditionalProducts(
        @Query("id") productsId: Long,
        @Query("article") productsArticle: String,

        @Query("nav") page: Int = 1,
    ): Response<VodovozResponseDTO<RecommendationsDTO>>

    @GET("korzina/index.php?action=getbasket")
    suspend fun getCartDetails(
        @Query("coupon") coupon: String? = null,
    ): Response<VodovozResponseDTO<CartDetailsDTO>>

    @GET("korzina/function/add/index.php?action=add")
    suspend fun addProductToCart(
        @Query("id") productId: Long,
        @Query("quantity") quantity: Int,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/function/add/index.php?action=addtoqua")
    suspend fun addMultipleProductsToCart(
        @Query("idquanit") productIdsWithQuantity: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/function/add/index.php?action=addtoqua&ydaltara=Y")
    suspend fun replaceMultipleBottlesToCart(
        @Query("idquanit") productIdsWithQuantity: String,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/function/deletto/index.php?action=deletto")
    suspend fun removeProductFromCart(
        @Query("id") productId: Long,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/function/guaty/index.php?action=guaty")
    suspend fun updateProductInCart(
        @Query("id") productId: Long,
        @Query("quantity") quantity: Int,
    ): Response<VodovozResponseDTO<String>>

    @GET("korzina/index.php?action=delbasket")
    suspend fun clearCart(): Response<VodovozResponseDTO<String>>

    @GET("korzina/brand.php?iblock_id=90")
    suspend fun getAllBottles(): Response<VodovozResponseDTO<AllBottlesDetailsDTO>>


    /**
     * Comment requests
     */
    @GET("comments.php?action=detail")
    suspend fun getComments(
        @Query("id") productId: Long,
        @Query("nav") page: Int,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<ProductCommentsDTO>>

    @GET("profile/otzyvy.php?action=glav")
    suspend fun getWaitFeedbackProducts(

        @Query("nav") page: Int = 1,
    ): Response<VodovozResponseDTO<WaitFeedbackProductsDTO>>

    @POST("comments.php")
    suspend fun sendComment(
        @Body body: RequestBody,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    @GET("osnova/form/obratnayasvyaz.php?action=glav")
    suspend fun getWriteMessageDetails(

    ): Response<VodovozResponseDTO<FormDTO>>

    @GET("osnova/form/obratnayasvyaz.php?action=otpravka")
    suspend fun sendMessage(

        @QueryMap queries: Map<String, String>,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>

    /**
     * ProductDetails screen
     */
    @GET("details/index.php?iblock_id=12")
    suspend fun getProductDetails(
        @Query("id") productId: Long,

        ): Response<VodovozResponseDTO<ProductDetailsDTO>>

    @GET("details/podarki.php?action=podarki")
    suspend fun getPresentInfo(

    ): Response<VodovozResponseDTO<PresentDTO>>

    /**
     * ProductsCollection screen
     */
    @GET("details/analog.php")
    suspend fun getProductAnalogs(
        @Query("id") productId: Long,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<AnalogsSectionDTO>>


    /**
     * Promotion screens
     */
    @GET("glavnaya/akcii.php?action=akcii")
    suspend fun getAllPromotions(
        @Query("nav") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("sect") categoryId: Int? = null,
    ): Response<VodovozResponseDTO<PromotionsDTO>>

    @GET("glavnaya/akcii.php?action=detail")
    suspend fun getPromotionDetails(
        @Query("id") promotionId: Int,
        @Query("nav") page: Int = 1,
        @Query("limit") limit: Int = 5,
    ): Response<VodovozResponseDTO<PromotionDetailsDTO>>

    /**
     * Home screen
     */
    @GET("glavnaya/slayders/index.php?action=slayder")
    suspend fun getBanners(
    ): Response<VodovozResponseDTO<List<BannerDTO>>>

    @GET("osnova/banners.php")
    suspend fun getBannerProducts(
        @Query("id") bannerId: Long,
        @Query("iblock") blockId: Long,
        @Query("nav") page: Int = 1,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
        @Query("sect") categoryId: Int? = null,
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("osnova/banners.php")
    suspend fun getBannerPromotions(
        @Query("id") bannerId: Long,
        @Query("iblock") blockId: Long,
        @Query("nav") page: Int = 1,
        @Query("sect") categoryId: Int? = null,
    ): Response<VodovozResponseDTO<PromotionsDTO>>

    @GET("glavnaya/otzivtovari.php?action=tovarglav")
    suspend fun getUnratedProductsDetails(

    ): Response<VodovozResponseDTO<UnratedProductsSectionDTO>>

    @GET("glavnaya/otzivtovari.php?action=addblock")
    suspend fun removeUnratedProduct(
        @Query("id") productId: Long,

        ): Response<VodovozResponseDTO<String>>


    @GET("glavnaya/stories/index.php?iblock_id=12&action=stories&platforma=android")
    suspend fun getStories(): Response<VodovozResponseDTO<StoriesDTO>>

    @GET("glavnaya/menushka.php?action=glavnaya")
    suspend fun getOrderMenu(
        @Query("userid") userId: Long? = null,
    ): Response<VodovozResponseDTO<OrderMenuDTO>>

    @GET("glavnaya/razdel.php?action=popylrazdel")
    suspend fun getPopularCategories(): Response<VodovozResponseDTO<PopularCategoriesDTO>>

    @GET("glavnaya/akcii.php?action=akcii&limit=10&nav=1")
    suspend fun getPromotions(): Response<VodovozResponseDTO<PromotionsDTO>>

    @GET("glavnaya/novinki.php?new=novinki")
    suspend fun getNewProducts(): Response<VodovozResponseDTO<RAZDEL_DTO>>

    @GET("glavnaya/novinki.php?new=novinki&detail=Y")
    suspend fun getAllNewProducts(
        @Query("nav") page: Int = 1,
        @Query("sect") categoryId: Int = -1,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("glavnaya/viewedproduct/index.php?action=viewed")
    suspend fun getViewedProducts(

    ): Response<VodovozResponseDTO<RAZDEL_DTO>>

    @GET("glavnaya/viewedproduct/index.php?action=details")
    suspend fun getAllViewedProducts(

        @Query("nav") page: Int = 1,
        @Query("sect") categoryId: Int? = null,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("glavnaya/novinki.php?new=specpredlosh&android=${BuildConfig.VERSION_NAME}")
    suspend fun getHurryUpBuyProducts(): Response<VodovozResponseDTO<RAZDEL_DTO>>

    @GET("glavnaya/novinki.php?new=specpredlosh&detail=Y&android=${BuildConfig.VERSION_NAME}")
    suspend fun getAllHurryUpBuyProducts(
        @Query("nav") page: Int = 1,
        @Query("sect") categoryId: Int?,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("glavnaya/super_top_new.php?action=topglav")
    suspend fun getSuperTopCategories(): Response<VodovozResponseDTO<SuperTopAndBottomSectionsDTO>>

    @GET("glavnaya/super_top_new.php?action=tovar")
    suspend fun getSuperTopByCategory(
        @Query("idknokpa") categoryId: Long,
    ): Response<VodovozResponseDTO<List<TOVAR_DATA_DTO>>>


    @GET("glavnaya/super_top_new.php?action=details")
    suspend fun getAllSuperTop(
        @Query("id") id: Long,
        @Query("nav") page: Int? = 1,
        @Query("sect") categoryId: Int? = null,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("glavnaya/okno.php?action=okno&android=${BuildConfig.VERSION_NAME}")
    suspend fun getPopupWindowInfo(

    ): Response<VodovozResponseDTO<PopupWindowDTO>>

    /**
     * Favorite requests
     * */
    @GET("osnova/izbrannoe/izbrannoe.php?action=izbrannoe")
    suspend fun getFavoriteProducts(
        @Query("nav") page: Int = 1,
        @Query("userid") userId: Long? = null,
        @Query("sect") categoryId: Int? = null,
        @Query("sort") sort: String = "",
        @Query("ascdesc") order: String = "",
        @Query("id") productsIds: String? = null,
    ): Response<VodovozResponseDTO<ProductsSectionDTO>>

    @GET("osnova/izbrannoe/adddel.php?action=add")
    suspend fun addToFavorites(
        @Query("id") productId: Long,

        ): Response<VodovozResponseDTO<String>>

    @GET("osnova/izbrannoe/adddel.php?action=del")
    suspend fun removeFromFavorites(
        @Query("id") productId: Long,

        ): Response<VodovozResponseDTO<String>>

    /**
     * Questionnaires screen
     * */
    @GET("profile/anketa/index.php")
    suspend fun getQuestionnairesWelcomeDetails(

    ): Response<VodovozResponseDTO<QuestionnairesWelcomeDetailsDTO>>

    @GET("profile/anketa/index.php")
    suspend fun getQuestionnairesDetails(

        @Query("action") who: String,
    ): Response<VodovozResponseDTO<QuestionnairesDetailsDTO>>

    @GET("profile/anketa/index.php")
    suspend fun sendQuestionnaires(
        @Query("action") who: String,

        @Query("filtervalue") answers: String,
    ): Response<VodovozResponseDTO<VodovozPlaceholderDTO>>
}