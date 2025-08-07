package com.vodovoz.app.domain.general.respository

import androidx.paging.PagingData
import com.vodovoz.app.common.model.VodovozSiteState
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.domain.general.model.CatalogDetailsModel
import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.FilterValueModel
import com.vodovoz.app.domain.general.model.FiltersModel
import com.vodovoz.app.domain.general.model.OrderWithMenuModel
import com.vodovoz.app.domain.general.model.ParentCategoryModel
import com.vodovoz.app.domain.general.model.PopularCategoryModel
import com.vodovoz.app.domain.general.model.PresentInfoModel
import com.vodovoz.app.domain.general.model.ProductCommentsInfoModel
import com.vodovoz.app.domain.general.model.SearchRecommendationsModel
import com.vodovoz.app.domain.general.model.SortModel
import com.vodovoz.app.domain.general.model.VodovozPlaceholderModel
import com.vodovoz.app.domain.general.model.brand.BrandModel
import com.vodovoz.app.domain.general.model.brand.BrandSectionModel
import com.vodovoz.app.domain.general.model.cart.BottomCartModel
import com.vodovoz.app.domain.general.model.cart.CartDetailsModel
import com.vodovoz.app.domain.general.model.certificate.BuyCertificateDetailsModel
import com.vodovoz.app.domain.general.model.certificate.BuyCertificateModel
import com.vodovoz.app.domain.general.model.certificate.CertificateActivationDetailsModel
import com.vodovoz.app.domain.general.model.location.AddAddressDetailsModel
import com.vodovoz.app.domain.general.model.location.AddressModel
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import com.vodovoz.app.domain.general.model.location.MapAreaModel
import com.vodovoz.app.domain.general.model.order.CancelOrderDetailsModel
import com.vodovoz.app.domain.general.model.order.DeliveryDateDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderCallYouDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderQuestionDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderingDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryItemModel
import com.vodovoz.app.domain.general.model.order.PaymentMethodDetailsModel
import com.vodovoz.app.domain.general.model.order.FormModel
import com.vodovoz.app.domain.general.model.order.RecipientDetailsModel
import com.vodovoz.app.domain.general.model.order.RecipientModel
import com.vodovoz.app.domain.general.model.order.WhereOrderDetailsModel
import com.vodovoz.app.domain.general.model.product.AllBottlesDetailsModel
import com.vodovoz.app.domain.general.model.product.CommentModel
import com.vodovoz.app.domain.general.model.product.ProductDetailsScreenModel
import com.vodovoz.app.domain.general.model.product.ProductModel
import com.vodovoz.app.domain.general.model.product.ProductsSectionModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.product.TopAndBottomSectionsModel
import com.vodovoz.app.domain.general.model.product.UnratedProductsSectionModel
import com.vodovoz.app.domain.general.model.product.WaitFeedbackProductModel
import com.vodovoz.app.domain.general.model.promotion.BannerModel
import com.vodovoz.app.domain.general.model.promotion.PopupWindowInfoModel
import com.vodovoz.app.domain.general.model.promotion.ProductsTitle
import com.vodovoz.app.domain.general.model.promotion.PromotionDetailsModel
import com.vodovoz.app.domain.general.model.promotion.PromotionModel
import com.vodovoz.app.domain.general.model.promotion.PromotionsSectionModel
import com.vodovoz.app.domain.general.model.promotion.StoryModel
import com.vodovoz.app.domain.general.model.service.AllServicesDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceDetailsModel
import com.vodovoz.app.domain.general.model.service.ServiceOrderDetailsModel
import com.vodovoz.app.domain.general.model.user.AuthDetailsModel
import com.vodovoz.app.domain.general.model.user.BonusesPopupWindowModel
import com.vodovoz.app.domain.general.model.user.ChangePasswordDetailsModel
import com.vodovoz.app.domain.general.model.user.NotificationSettingsDetailsModel
import com.vodovoz.app.domain.general.model.user.ProfileDetailsModel
import com.vodovoz.app.domain.general.model.user.QuestionnairesDetailsModel
import com.vodovoz.app.domain.general.model.user.QuestionnairesWelcomeDetailsModel
import com.vodovoz.app.domain.general.model.user.RequestCodeModel
import com.vodovoz.app.domain.general.model.user.UserAuthInfoModel
import com.vodovoz.app.domain.general.model.user.UserDataModel
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate

interface VodovozServiceRepository {


    fun removeAddress(addressId: Int): Flow<Result<String>>

    fun addAddress(address: MapAddressModel, params: Map<String, String>): Flow<Result<Long>>

    fun getAddAddressDetails(addressId: Long?): Flow<Result<AddAddressDetailsModel>>

    fun updateAddress(
        addressId: Long,
        address: MapAddressModel?,
        params: Map<String, String>,
    ): Flow<Result<String>>

    fun getPaymentMethodDetails(
        addressId: Long,
        date: LocalDate,
    ): Flow<Result<PaymentMethodDetailsModel>>

    fun getDeliveryDateDetails(
        addressId: Long,
        date: LocalDate? = null,
    ): Flow<Result<DeliveryDateDetailsModel>>

    fun getOrderRecipientDetails(
        addressId: Long,
    ): Flow<Result<RecipientDetailsModel>>

    fun getRecipient(
        addressId: Long,
    ): Flow<Result<RecipientModel>>

    fun sendOrderRecipient(
        addressId: Long,
        fields: List<FieldModel>,
    ): Flow<Result<String>>

    fun getOrderCallYouDetails(
        addressId: Long,
    ): Flow<Result<OrderCallYouDetailsModel>>

    fun getOrderingDetails(
        addressId: Long? = null,
        date: String? = null,
        timeInterval: String? = null,
    ): Flow<Result<OrderingDetailsModel>>

    fun doOrder(
        addressId: Long,
        deliveryDate: String,
        deliveryTimeInterval: String,
        phone: String,
        paymentMethodId: Long,
        callYouId: Long? = null,
        coupon: String?,
        balance: String?,
        deviceInfo: String?,
        notifyDriverId: String? = null,
        message: String? = null,
        params: Map<String, String>?
    ): Flow<Result<VodovozPlaceholderModel>>

    fun orderService(
        serviceType: String,
        fields: List<FieldModel>,
    ): Flow<Result<VodovozPlaceholderModel>>

    fun getServiceOrderDetails(serviceType: String): Flow<Result<ServiceOrderDetailsModel>>

    fun removeFirebaseToken(token: String): Flow<Result<String>>

    fun sendFirebaseToken(token: String): Flow<Result<String>>

    fun getOrdersHistoryDetails(): Flow<Result<OrdersHistoryDetailsModel>>

    fun getOrdersHistoryItemsPaged(
        statuses: String,
        searchQuery: String,
    ): Flow<PagingData<OrdersHistoryItemModel>>

    fun repeatOrder(orderId: Long): Flow<Result<String>>

    fun getWaitFeedbackProductsTitle(): Flow<Result<String>>

    fun getWaitFeedbackProductsPaged(): Flow<PagingData<WaitFeedbackProductModel>>

    fun getNotificationSettingsDetails(): Flow<Result<NotificationSettingsDetailsModel>>

    fun updateNotificationSettings(params: Map<String, String>): Flow<Result<String>>

    fun getRecoverPasswordDetails(): Flow<Result<AuthDetailsModel>>

    fun recoverPassword(fields: List<FieldModel>): Flow<Result<VodovozPlaceholderModel>>

    fun requestPhoneCode(
        url: String,
        phone: String,
        params: Map<String, String> = emptyMap(),
    ): Flow<Result<RequestCodeModel>>

    fun loginByPhone(
        url: String,
        code: String,
        phone: String,
    ): Flow<Result<UserAuthInfoModel>>

    fun getAllServicesDetails(): Flow<Result<AllServicesDetailsModel>>

    fun getServiceDetails(serviceId: Int): Flow<Result<ServiceDetailsModel>>

    fun getQuestionnairesWelcomeDetails(): Flow<Result<QuestionnairesWelcomeDetailsModel>>

    fun getQuestionnairesDetails(who: String): Flow<Result<QuestionnairesDetailsModel>>

    fun sendQuestionnairesAnswers(
        who: String,
        answers: String,
    ): Flow<Result<VodovozPlaceholderModel>>

    fun getCancelOrderDetails(orderId: Long): Flow<Result<CancelOrderDetailsModel>>

    fun cancelOrder(orderId: Long, params: Map<String, String>): Flow<Result<String>>

    fun sendOrderQuestion(
        orderId: Long,
        fields: List<FieldModel>,
    ): Flow<Result<VodovozPlaceholderModel>>

    fun getOrderQuestionDetails(orderId: Long): Flow<Result<FormModel>>

    fun getOrderDetails(orderId: Long): Flow<Result<OrderDetailsModel>>

    fun getAllBottles(): Flow<Result<AllBottlesDetailsModel>>

    fun getWhereMyOrderDetails(
        orderId: Long,
        driverId: String,
    ): Flow<Result<WhereOrderDetailsModel>>

    fun getAddresses(): Flow<Result<List<SectionModel<AddressModel>>>>

    fun getMapAreas(): Flow<Result<List<MapAreaModel>>>


    fun getPastPurchasesDetails(
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<Result<ProductsSectionModel>>

    fun getPastPurchasesPaged(
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>


    fun getBrands(
        searchQuery: String = "",
    ): Flow<Result<BrandSectionModel>>


    fun getBrandsPaged(
        searchQuery: String = "",
    ): Flow<PagingData<BrandModel>>

    fun getBrandProducts(
        brandId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<Result<ProductsSectionModel>>

    fun getBrandProductsPaged(
        brandId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>

    fun getBannerPromotions(
        bannerId: Long,
        blockId: Long,
        categoryId: Int = -1,
    ): Flow<Result<PromotionsSectionModel>>

    fun getBannerPromotionsPaged(
        bannerId: Long,
        blockId: Long,
        categoryId: Int = -1,
    ): Flow<PagingData<PromotionModel>>



    fun getBannerProducts(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<Result<ProductsSectionModel>>

    fun getBannerProductsPaged(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int = -1,
    ): Flow<PagingData<ProductModel>>


    fun getLoginDetails(): Flow<Result<AuthDetailsModel>>

    fun getLoginByEmailDetails(): Flow<Result<AuthDetailsModel>>

    fun updatePassword(password: String): Flow<Result<VodovozPlaceholderModel>>

    fun getChangePasswordDetails(): Flow<Result<ChangePasswordDetailsModel>>

    fun updateUserAvatar(avatarFile: File): Flow<Result<String>>

    fun updateUserData(fields: List<FieldModel>): Flow<Result<String>>

    fun getUserData(): Flow<Result<UserDataModel>>

    fun getProfileDetails(): Flow<Result<ProfileDetailsModel>>

    fun getBonusesPopupWindow(): Flow<Result<BonusesPopupWindowModel>>

    fun updateBonusesSubscribe(subscribe: Boolean): Flow<Result<Unit>>

    fun getFilters(categoryId: Int): Flow<Result<FiltersModel>>

    fun getFilterValues(categoryId: Int, filterId: String): Flow<Result<List<FilterValueModel>>>

    fun buyCertificate(params: Map<String, String>): Flow<Result<BuyCertificateModel>>

    fun getBuyCertificateDetails(): Flow<Result<BuyCertificateDetailsModel>>

    fun getCertificateActivationDetails(): Flow<Result<CertificateActivationDetailsModel>>

    fun activateCertificate(field: FieldUi): Flow<Result<String>>

    fun getRegisterDetails(): Flow<Result<AuthDetailsModel>>

    fun logout(): Flow<Result<Unit>>

    fun deleteAccount(): Flow<Result<Unit>>

    fun relogin(): Flow<Result<Boolean>>

    fun register(params: Map<String, String>): Flow<Result<UserAuthInfoModel>>

    fun loginByEmail(
        params: Map<String, String>
    ): Flow<Result<UserAuthInfoModel>>

    fun getCatalogDetails(): Flow<Result<CatalogDetailsModel>>

    fun getCategoryTree(
        categoryId: Long,
    ): Flow<Result<List<ParentCategoryModel>>>

    fun getCategoryProducts(
        categoryId: Long,
        filters: FiltersModel,
    ): Flow<Result<ProductsSectionModel>>

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

    fun getSearchProducts(query: String, categoryId: Int): Flow<Result<ProductsSectionModel>>

    fun getBarCodeProducts(barCode: String): Flow<Result<List<ProductModel>>>

    fun getSearchRecommendations(): Flow<Result<SearchRecommendationsModel>>

    fun getMiniSearchRecommendations(query: String): Flow<Result<SearchRecommendationsModel>>

    fun getSiteState(): Flow<Result<VodovozSiteState>>

    fun getPreorderFields(productId: Long): Flow<Result<FormModel>>

    fun sendPreorder(productId: Long, fields: List<FieldModel>): Flow<Result<String>>

    fun getUnratedProductsDetails(): Flow<Result<UnratedProductsSectionModel>>

    fun removeUnratedProduct(productId: Long): Flow<Result<String>>

    fun getFavoriteProducts(
        productsIds: String = "",
    ): Flow<Result<ProductsSectionModel>>

    fun getFavoriteProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
        productsIds: String = "",
    ): Flow<PagingData<ProductModel>>

    suspend fun addFavoriteProducts(
        productsIds: String,
    ): Flow<Result<ProductsSectionModel>>

    suspend fun addProductToFavorites(
        productId: Long,
    ): Flow<Result<String>>

    suspend fun removeProductFromFavorites(
        productId: Long,
    ): Flow<Result<String>>

    suspend fun getBottomCart(): Flow<Result<BottomCartModel>>

    suspend fun getCartDetails(
        coupon: String? = null,
    ): Flow<Result<CartDetailsModel>>

    suspend fun addProductToCart(
        productId: Long,
        quantity: Int,
    ): Flow<Result<String>>

    suspend fun addMultipleProductsToCart(
        productIdsWithQuantity: String,
    ): Flow<Result<String>>

    suspend fun removeProductFromCart(
        productId: Long,
    ): Flow<Result<String>>


    suspend fun updateProductInCart(
        productId: Long,
        quantity: Int,
    ): Flow<Result<String>>

    suspend fun clearCart(): Flow<Result<String>>

    fun getProductAnalogs(
        productId: Long,
        sort: SortModel,
    ): Flow<Result<ProductsSectionModel>>

    fun getWriteMessageDetails(
    ): Flow<Result<FormModel>>

    fun sendMessage(
        params: Map<String, String>
    ): Flow<Result<VodovozPlaceholderModel>>

    fun sendComment(
        productId: Long,
        rating: Int,
        message: String,
        imageBytesArray: List<ByteArray>
    ): Flow<Result<VodovozPlaceholderModel>>

    fun getProductCommentsInfo(
        productId: Long,
    ): Flow<Result<ProductCommentsInfoModel>>

    fun getProductCommentsPaged(
        productId: Long,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<CommentModel>>

    fun getProductDetails(productId: Long): Flow<Result<ProductDetailsScreenModel>>

    fun getPresentInfo(): Flow<Result<PresentInfoModel>>

    fun getPopupWindowInfo(): Flow<Result<PopupWindowInfoModel>>

    fun getStories(): Flow<Result<List<StoryModel>>>

    fun getBanners(): Flow<Result<List<BannerModel>>>

    fun getPromotions(): Flow<Result<PromotionsSectionModel>>

    fun getPromotionDetails(
        promotionId: Int,
    ): Flow<Result<Pair<ProductsTitle, PromotionDetailsModel>>>

    fun getPromotionDetailsProductsPaged(
        promotionId: Int,
        limit: Int = 5,
    ): Flow<PagingData<ProductModel>>

    fun getAllPromotionsDetails(
        categoryId: Int = -1,
    ): Flow<Result<PromotionsSectionModel>>

    fun getAllPromotionsPaged(
        limit: Int = 10,
        categoryId: Int = -1,
    ): Flow<PagingData<PromotionModel>>

    fun getOrderMenu(
        userId: Long? = null,
    ): Flow<Result<OrderWithMenuModel>>

    fun getPopularCategories(): Flow<Result<SectionModel<PopularCategoryModel>>>

    fun getNewProducts(): Flow<Result<SectionModel<ProductModel>>>

    fun getAllNewProducts(
        categoryId: Int = -1
    ): Flow<Result<ProductsSectionModel>>

    fun getAllNewProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getHurryUpBuyProducts(): Flow<Result<SectionModel<ProductModel>>>

    suspend fun getAllHurryUpBuyProducts(
        categoryId: Int = -1,
    ): Flow<Result<ProductsSectionModel>>

    fun getAllHurryUpBuyProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getSuperTop(): Flow<Result<TopAndBottomSectionsModel>>

    fun getAllSuperTop(
        buttonId: Int,
        categoryId: Int = -1,
    ): Flow<Result<ProductsSectionModel>>

    fun getAllSuperTopPaged(
        buttonId: Int,
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

    fun getViewedProducts(): Flow<Result<SectionModel<ProductModel>>>

    fun getAllViewedProducts(
        categoryId: Int
    ): Flow<Result<ProductsSectionModel>>

    fun getAllViewedProductsPaged(
        categoryId: Int = -1,
        sort: SortModel = SortModel.Empty,
    ): Flow<PagingData<ProductModel>>

}