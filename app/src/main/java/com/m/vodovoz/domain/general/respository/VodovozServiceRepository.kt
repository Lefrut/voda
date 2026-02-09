package com.m.vodovoz.domain.general.respository

import androidx.paging.PagingData
import com.m.vodovoz.common.model.AppConfig
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsBSModel
import com.m.vodovoz.domain.general.model.cart.BottomCartModel
import com.m.vodovoz.domain.general.model.cart.CartDetailsModel
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel
import com.m.vodovoz.domain.general.model.location.AddAddressDetailsModel
import com.m.vodovoz.domain.general.model.location.AddressLabelsModel
import com.m.vodovoz.domain.general.model.location.AddressModel
import com.m.vodovoz.domain.general.model.location.MapAddressModel
import com.m.vodovoz.domain.general.model.location.MapZonesModel
import com.m.vodovoz.domain.general.model.order.CancelOrderDetailsModel
import com.m.vodovoz.domain.general.model.order.DeliveryDateDetailsModel
import com.m.vodovoz.domain.general.model.order.FormModel
import com.m.vodovoz.domain.general.model.order.OrderCallYouDetailsModel
import com.m.vodovoz.domain.general.model.order.OrderDetailsModel
import com.m.vodovoz.domain.general.model.order.OrderWithMenuModel
import com.m.vodovoz.domain.general.model.order.OrderingDetailsModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryDetailsModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryItemModel
import com.m.vodovoz.domain.general.model.order.PaymentMethodDetailsModel
import com.m.vodovoz.domain.general.model.order.RecipientDetailsModel
import com.m.vodovoz.domain.general.model.order.RecipientModel
import com.m.vodovoz.domain.general.model.order.WhereOrderDetailsModel
import com.m.vodovoz.domain.general.model.product.AllBottlesDetailsModel
import com.m.vodovoz.domain.general.model.product.BuyCertificateDetailsModel
import com.m.vodovoz.domain.general.model.product.BuyCertificateModel
import com.m.vodovoz.domain.general.model.product.CartProductsModel
import com.m.vodovoz.domain.general.model.product.CatalogDetailsModel
import com.m.vodovoz.domain.general.model.product.CertificateActivationDetailsModel
import com.m.vodovoz.domain.general.model.product.CommentModel
import com.m.vodovoz.domain.general.model.product.FilterValueModel
import com.m.vodovoz.domain.general.model.product.FiltersModel
import com.m.vodovoz.domain.general.model.product.ParentCategoryModel
import com.m.vodovoz.domain.general.model.product.PopularCategoryModel
import com.m.vodovoz.domain.general.model.product.ProductCommentsInfoModel
import com.m.vodovoz.domain.general.model.product.ProductDetailsScreenModel
import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.product.ProductsSectionModel
import com.m.vodovoz.domain.general.model.product.SearchRecommendationsModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.product.SortModel
import com.m.vodovoz.domain.general.model.product.SuperTopModel
import com.m.vodovoz.domain.general.model.product.UnratedProductsSectionModel
import com.m.vodovoz.domain.general.model.product.WaitFeedbackProductModel
import com.m.vodovoz.domain.general.model.promotion.BannerModel
import com.m.vodovoz.domain.general.model.promotion.BrandModel
import com.m.vodovoz.domain.general.model.promotion.BrandSectionModel
import com.m.vodovoz.domain.general.model.promotion.PopupWindowInfoModel
import com.m.vodovoz.domain.general.model.promotion.PresentInfoModel
import com.m.vodovoz.domain.general.model.promotion.ProductsTitle
import com.m.vodovoz.domain.general.model.promotion.PromotionDetailsModel
import com.m.vodovoz.domain.general.model.promotion.PromotionModel
import com.m.vodovoz.domain.general.model.promotion.PromotionsSectionModel
import com.m.vodovoz.domain.general.model.promotion.StoryModel
import com.m.vodovoz.domain.general.model.service.AllServicesDetailsModel
import com.m.vodovoz.domain.general.model.service.ServiceDetailsModel
import com.m.vodovoz.domain.general.model.user.AuthDetailsModel
import com.m.vodovoz.domain.general.model.user.BonusesPopupWindowModel
import com.m.vodovoz.domain.general.model.user.ChangePasswordDetailsModel
import com.m.vodovoz.domain.general.model.user.NotificationSettingsDetailsModel
import com.m.vodovoz.domain.general.model.user.ProfileDetailsModel
import com.m.vodovoz.domain.general.model.user.QuestionnairesDetailsModel
import com.m.vodovoz.domain.general.model.user.QuestionnairesWelcomeDetailsModel
import com.m.vodovoz.domain.general.model.user.RequestCodeModel
import com.m.vodovoz.domain.general.model.user.UserAuthInfoModel
import com.m.vodovoz.domain.general.model.user.UserDataModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate


private typealias FlowResult<T> = Flow<Result<T>>

interface VodovozServiceRepository {


    fun removeAddress(addressId: Int): FlowResult<String>

    fun addAddress(address: MapAddressModel, params: Map<String, String>): FlowResult<Long>

    fun getAddAddressDetails(addressId: Long?): FlowResult<AddAddressDetailsModel>

    fun getAddressLabels(): FlowResult<AddressLabelsModel>

    fun addAddressLabel(label: String): FlowResult<String>

    fun deleteAddressLabel(label: String): FlowResult<String>

    fun deleteAllAddressLabels(): FlowResult<String>


    fun updateAddress(
        addressId: Long,
        address: MapAddressModel?,
        params: Map<String, String>,
    ): FlowResult<String>

    fun getPaymentMethodDetails(
        addressId: Long,
        date: LocalDate,
        queryParams: Map<String, String>,
    ): FlowResult<PaymentMethodDetailsModel>

    fun getDeliveryDateDetails(
        addressId: Long,
        date: LocalDate? = null,
        queryParams: Map<String, String>,
    ): FlowResult<DeliveryDateDetailsModel>

    fun getOrderRecipientDetails(
        addressId: Long,
    ): FlowResult<RecipientDetailsModel>

    fun getRecipient(
        addressId: Long,
    ): FlowResult<RecipientModel>

    fun sendOrderRecipient(
        addressId: Long,
        params: Map<String, String>,
    ): FlowResult<String>

    fun getOrderCallYouDetails(
        addressId: Long,
        queryParams: Map<String, String>,
    ): FlowResult<OrderCallYouDetailsModel>

    fun getOrderingDetails(
        addressId: Long? = null,
        date: String? = null,
        timeInterval: String? = null,
        coupon: String? = null,
        useBonuses: Boolean? = false,
        useBalance: Boolean? = false,
        bonuses: Int? = null,
        queryParams: Map<String, String>
    ): FlowResult<OrderingDetailsModel>

    fun doOrder(
        addressId: Long,
        deliveryDate: String,
        deliveryTimeInterval: String,
        userFIO: String,
        userPhone: String,
        userEmail: String?,
        paymentMethodId: Long,
        paymentChange: String?,
        coupon: String?,
        deviceInfo: String?,
        notifyDriverId: String? = null,
        useBonuses: Boolean?,
        useBalance: Boolean?,
        bonuses: Int?,
        params: Map<String, String>?,
    ): FlowResult<VodovozPlaceholderModel>

    fun orderService(
        serviceType: String,
        queries: Map<String, String>,
    ): FlowResult<VodovozPlaceholderModel>

    fun getServiceOrderDetails(serviceType: String): FlowResult<FormModel>


    fun removeFirebaseToken(token: String): FlowResult<String>

    fun sendFirebaseToken(token: String): FlowResult<String>

    fun getOrdersHistoryDetails(): FlowResult<OrdersHistoryDetailsModel>

    fun getOrdersHistoryItemsPaged(
        statuses: String,
        searchQuery: String,
    ): Flow<PagingData<OrdersHistoryItemModel>>

    fun repeatOrder(orderId: Long): FlowResult<String>

    fun getWaitFeedbackProductsTitle(): FlowResult<String>

    fun getWaitFeedbackProductsPaged(): Flow<PagingData<WaitFeedbackProductModel>>

    fun getNotificationSettingsDetails(): FlowResult<NotificationSettingsDetailsModel>

    fun updateNotificationSettings(params: Map<String, String>): FlowResult<String>

    fun getRecoverPasswordDetails(): FlowResult<AuthDetailsModel>

    fun recoverPassword(fields: List<FieldModel>): FlowResult<VodovozPlaceholderModel>

    fun requestPhoneCode(
        url: String,
        phone: String,
        params: Map<String, String> = emptyMap(),
    ): FlowResult<RequestCodeModel>

    fun loginByPhone(
        url: String,
        code: String,
        phone: String,
    ): FlowResult<UserAuthInfoModel>

    fun getAllServicesDetails(): FlowResult<AllServicesDetailsModel>

    fun getServiceDetails(serviceId: Int): FlowResult<ServiceDetailsModel>

    fun getQuestionnairesWelcomeDetails(): FlowResult<QuestionnairesWelcomeDetailsModel>

    fun getQuestionnairesDetails(who: String): FlowResult<QuestionnairesDetailsModel>

    fun sendQuestionnairesAnswers(
        who: String,
        answers: String,
    ): FlowResult<VodovozPlaceholderModel>

    fun getCancelOrderDetails(orderId: Long): FlowResult<CancelOrderDetailsModel>

    fun cancelOrder(orderId: Long, params: Map<String, String>): FlowResult<String>

    fun sendOrderQuestion(
        orderId: Long,
        fields: List<FieldModel>,
    ): FlowResult<VodovozPlaceholderModel>

    fun getOrderQuestionDetails(orderId: Long): FlowResult<FormModel>

    fun getOrderDetails(orderId: Long): FlowResult<OrderDetailsModel>

    fun getAllBottles(): FlowResult<AllBottlesDetailsModel>

    fun getWhereMyOrderDetails(
        orderId: Long,
        driverId: String,
    ): FlowResult<WhereOrderDetailsModel>

    fun getAddresses(): FlowResult<List<SectionModel<AddressModel>>>

    fun getMapAreas(): FlowResult<MapZonesModel>


    fun getPastPurchasesDetails(
        sort: SortModel,
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getPastPurchasesPaged(
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>


    fun getBrands(
        searchQuery: String = "",
    ): FlowResult<BrandSectionModel>


    fun getBrandsPaged(
        searchQuery: String = "",
    ): Flow<PagingData<BrandModel>>

    fun getBrandProducts(
        brandId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getBrandProductsPaged(
        brandId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>

    fun getBannerPromotions(
        bannerId: Long,
        blockId: Long,
        categoryId: Int = -1,
    ): FlowResult<PromotionsSectionModel>

    fun getBannerPromotionsPaged(
        bannerId: Long,
        blockId: Long,
        categoryId: Int? = -1,
    ): Flow<PagingData<PromotionModel>>


    fun getBannerProducts(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getBannerProductsPaged(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>


    fun getLoginDetails(): FlowResult<AuthDetailsModel>

    fun getLoginByEmailDetails(): FlowResult<AuthDetailsModel>

    fun updatePassword(password: String): FlowResult<VodovozPlaceholderModel>

    fun getChangePasswordDetails(): FlowResult<ChangePasswordDetailsModel>

    fun updateUserAvatar(avatarFile: File): FlowResult<String>

    fun updateUserData(fields: List<FieldModel>): FlowResult<String>

    fun getUserData(): FlowResult<UserDataModel>

    fun getProfileDetails(): FlowResult<ProfileDetailsModel>

    fun getBonusesPopupWindow(): FlowResult<BonusesPopupWindowModel>

    fun updateBonusesSubscribe(subscribe: Boolean): FlowResult<Unit>

    fun getFilters(categoryId: Int): FlowResult<FiltersModel>

    fun getFilterValues(categoryId: Int, filterId: String): FlowResult<List<FilterValueModel>>

    fun buyCertificate(params: Map<String, String>): FlowResult<BuyCertificateModel>

    fun getBuyCertificateDetails(): FlowResult<BuyCertificateDetailsModel>

    fun getCertificateActivationDetails(): FlowResult<CertificateActivationDetailsModel>

    fun activateCertificate(field: FieldUi): FlowResult<String>

    fun getRegisterDetails(): FlowResult<AuthDetailsModel>

    fun logout(): FlowResult<Unit>

    fun deleteAccount(): FlowResult<Unit>

    fun relogin(): FlowResult<Boolean>

    fun register(params: Map<String, String>): FlowResult<UserAuthInfoModel>

    fun loginByEmail(
        params: Map<String, String>,
    ): FlowResult<UserAuthInfoModel>

    fun getCatalogDetails(): FlowResult<CatalogDetailsModel>

    fun getCategoryTree(
        categoryId: Long,
    ): FlowResult<List<ParentCategoryModel>>

    fun getCategoryProducts(
        categoryId: Long,
        filters: FiltersModel,
    ): FlowResult<ProductsSectionModel>

    fun getCategoryProductsPaged(
        categoryId: Long,
        sort: SortModel,
        filters: FiltersModel,
    ): Flow<PagingData<ProductModel>>

    fun getSearchProductsPaged(
        query: String,
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getSearchProducts(query: String, categoryId: Int): FlowResult<ProductsSectionModel>

    fun getBarCodeProducts(barCode: String): FlowResult<List<ProductModel>>

    fun getSearchRecommendations(): FlowResult<SearchRecommendationsModel>

    fun getMiniSearchRecommendations(query: String): FlowResult<SearchRecommendationsModel>

    fun getSiteState(): FlowResult<AppConfig>

    fun getPreorderDetails(productId: Long): FlowResult<FormModel>

    fun sendPreorder(
        productId: Long,
        queries: Map<String, String>,
    ): FlowResult<String>

    fun getUnratedProductsDetails(): FlowResult<UnratedProductsSectionModel>

    fun removeUnratedProduct(productId: Long): FlowResult<String>

    fun getFavoriteProducts(
        productsIds: String = "",
    ): FlowResult<ProductsSectionModel>

    fun getFavoriteProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
        productsIds: String = "",
    ): Flow<PagingData<ProductModel>>

    suspend fun addFavoriteProducts(
        productsIds: String,
    ): FlowResult<ProductsSectionModel>

    suspend fun addProductToFavorites(
        productId: Long,
    ): FlowResult<String>

    suspend fun removeProductFromFavorites(
        productId: Long,
    ): FlowResult<String>

    suspend fun getBottomCart(): FlowResult<BottomCartModel>

    suspend fun getCartDetails(
        coupon: String? = null,
    ): FlowResult<CartDetailsModel>

    fun getAdditionalProductsBS(
        productsId: Long,
        productsArticle: String,
    ): FlowResult<AdditionalProductsBSModel>

    fun getAdditionalProductsPaged(
        productsId: Long,
        productsArticle: String,
    ): Flow<PagingData<ProductModel>>


    suspend fun addProductToCart(
        productId: Long,
        quantity: Int,
    ): FlowResult<String>

    suspend fun addMultipleProductsToCart(
        productIdsWithQuantity: String,
    ): FlowResult<String>

    suspend fun replaceMultipleBottlesToCart(
        cartProducts: CartProductsModel,
    ): FlowResult<String>

    suspend fun removeProductFromCart(
        productId: Long,
    ): FlowResult<String>


    suspend fun updateProductInCart(
        productId: Long,
        quantity: Int,
    ): FlowResult<String>

    suspend fun clearCart(): FlowResult<String>

    fun getProductAnalogs(
        productId: Long,
        sort: SortModel,
    ): FlowResult<ProductsSectionModel>

    fun getWriteMessageDetails(
    ): FlowResult<FormModel>

    fun sendMessage(
        params: Map<String, String>,
    ): FlowResult<VodovozPlaceholderModel>

    fun sendComment(
        productId: Long,
        rating: Int,
        message: String,
        imageBytesArray: List<ByteArray>,
    ): FlowResult<VodovozPlaceholderModel>

    fun getProductCommentsInfo(
        productId: Long,
    ): FlowResult<ProductCommentsInfoModel>

    fun getProductCommentsPaged(
        productId: Long,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<CommentModel>>

    fun getProductDetails(productId: Long): FlowResult<ProductDetailsScreenModel>

    fun getPresentInfo(): FlowResult<PresentInfoModel>

    fun getPopupWindowInfo(): FlowResult<PopupWindowInfoModel>

    fun getStories(): FlowResult<List<StoryModel>>

    fun getBanners(): FlowResult<List<BannerModel>>

    fun getPromotions(): FlowResult<PromotionsSectionModel>

    fun getPromotionDetails(
        promotionId: Int,
    ): FlowResult<Pair<ProductsTitle, PromotionDetailsModel>>

    fun getPromotionDetailsProductsPaged(
        promotionId: Int,
        limit: Int = 5,
    ): Flow<PagingData<ProductModel>>

    fun getAllPromotionsDetails(
        categoryId: Int = -1,
    ): FlowResult<PromotionsSectionModel>

    fun getAllPromotionsPaged(
        limit: Int = 10,
        categoryId: Int?,
    ): Flow<PagingData<PromotionModel>>

    fun getOrderMenu(): FlowResult<OrderWithMenuModel>

    fun getPopularCategories(): FlowResult<SectionModel<PopularCategoryModel>>

    fun getNewProducts(): FlowResult<SectionModel<ProductModel>>

    fun getAllNewProducts(
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getAllNewProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getHurryUpBuyProducts(): FlowResult<SectionModel<ProductModel>>

    suspend fun getAllHurryUpBuyProducts(
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getAllHurryUpBuyProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getSuperTopCategories(): FlowResult<SuperTopModel>

    fun getSuperTopProducts(categoryId: Long): FlowResult<List<ProductModel>>

    fun getAllSuperTop(
        buttonId: Int,
        categoryId: Int = -1,
    ): FlowResult<ProductsSectionModel>

    fun getAllSuperTopPaged(
        buttonId: Int,
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getViewedProducts(): FlowResult<SectionModel<ProductModel>>

    fun getAllViewedProducts(
        categoryId: Int,
    ): FlowResult<ProductsSectionModel>

    fun getAllViewedProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

}