package com.m.vodovoz.data.vodovoz_service.repository

import androidx.core.text.HtmlCompat
import androidx.paging.PagingData
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.model.AppConfig
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.core.network.retrofit.messageWithCode
import com.m.vodovoz.core.network.retrofit.stringBody
import com.m.vodovoz.core.network.retrofit.stringErrorBody
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.data.vodovoz_service.RequestExecutor
import com.m.vodovoz.data.vodovoz_service.VodovozService
import com.m.vodovoz.data.vodovoz_service.executeRequest
import com.m.vodovoz.data.vodovoz_service.mappers.RequestFailureStrategy
import com.m.vodovoz.data.vodovoz_service.mappers.executeRequest
import com.m.vodovoz.data.vodovoz_service.mappers.mapToDomain
import com.m.vodovoz.data.vodovoz_service.mappers.toDomain
import com.m.vodovoz.data.vodovoz_service.model.BrandSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductCommentsDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionsDTO
import com.m.vodovoz.data.vodovoz_service.model.SiteStateResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozErrorResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.RecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrdersHistoryDetailsDTO
import com.m.vodovoz.data.vodovoz_service.paging.VodovozPagerFactory
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsBSModel
import com.m.vodovoz.domain.general.model.cart.BottomCartModel
import com.m.vodovoz.domain.general.model.cart.CartDetailsModel
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.model.exceptions.RequestException
import com.m.vodovoz.domain.general.model.exceptions.TooManyRequestsException
import com.m.vodovoz.domain.general.model.exceptions.UserBlockedException
import com.m.vodovoz.domain.general.model.exceptions.UserNotLoginException
import com.m.vodovoz.domain.general.model.exceptions.ValidationException
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
import com.m.vodovoz.domain.general.model.product.format
import com.m.vodovoz.domain.general.model.product.toSliderQueries
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
import com.m.vodovoz.domain.general.model.widgets.toQueries
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.formatters.VodovozDateFormatters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class VodovozServiceRepositoryImpl @Inject constructor(
    private val vodovozService: VodovozService,
    private val accountManager: AccountManager,
    private val cookieManager: CookieManager,
    private val moshi: Moshi,
) : VodovozServiceRepository {

   private val defaultExecutor = object : RequestExecutor(moshi) {
       override fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
           return Result.failure(RequestException(response.messageWithCode()))
       }
    }

    private data object EmptyFailureStrategy : RequestFailureStrategy {

        override fun <R : Any> handleFail(response: Response<ResponseBody>): Result<R> {
            TODO("Not yet implemented")
        }

    }

    private data object AllFailureStrategy : RequestFailureStrategy {

        override fun <R : Any> handleFail(response: Response<ResponseBody>): Result<R> {
            TODO("Not yet implemented")
        }

    }

    private data object AuthFailureStrategy : RequestFailureStrategy {

        override fun <R : Any> handleFail(response: Response<ResponseBody>): Result<R> {
            TODO("Not yet implemented")
        }

    }

    private data object DefaultFailureStrategy : RequestFailureStrategy {

        override fun <R : Any> handleFail(response: Response<ResponseBody>): Result<R> {
            val exception = RequestException(response.messageWithCode())
            return Result.failure(exception)
        }
    }

    override fun removeAddress(addressId: Int): Flow<Result<String>> {
        return DefaultFailureStrategy.executeRequest(
            request = { vodovozService.deleteAddress(addressId) }
        )
    }

    override fun addAddress(
        address: MapAddressModel,
        params: Map<String, String>,
    ): Flow<Result<Long>> {
        return defaultExecutor.executeRequest(
            request = {
                val point = address.point
                vodovozService.addAddress(
                    geo = "${point.lat},${point.lon}",
                    city = address.city,
                    street = address.street,
                    fromMoscowToAddressKm = address.fromMoscowToPoint,
                    queries = params
                )
            },
            mapper = {
                it.data!!
            },
        )
    }

    override fun getAddAddressDetails(addressId: Long?): Flow<Result<AddAddressDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAddAddressDetails(addressId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getAddressLabels(): Flow<Result<AddressLabelsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAddressLabels(

                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun addAddressLabel(label: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.addAddressLabel(

                    label = label
                )
            },
            mapper = { it.data!! }
        )
    }

    override fun deleteAddressLabel(label: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.deleteAddressLabel(

                    label = label
                )
            },
            mapper = { it.data!! }
        )

    }

    override fun deleteAllAddressLabels(): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.deleteAllAddressLabels(

                )
            },
            mapper = { it.data!! }
        )
    }

    override fun updateAddress(
        addressId: Long,
        address: MapAddressModel?,
        params: Map<String, String>,
    ): Flow<Result<String>> {


        return executeRequest(
            request = {
                val point = address?.point
                val geo = point?.let {
                    "${point.lat},${point.lon}"
                }

                vodovozService.updateAddress(

                    addressId = addressId,
                    geo = geo,
                    city = address?.city,
                    street = address?.street,
                    fromMoscowToAddressKm = address?.fromMoscowToPoint,
                    params = params
                )
            },
            mapper = {
                it.message ?: ""
            },
            fail = { response ->

                val message = moshi.fromJson<VodovozResponseDTO<String?>>(
                    response.stringErrorBody()
                ).message ?: ""

                throw RequestException(message)
            }
        )
    }

    override fun getPaymentMethodDetails(
        addressId: Long,
        date: LocalDate,
    ): Flow<Result<PaymentMethodDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getPaymentMethodDetails(

                    addressId = addressId,
                    date = VodovozDateFormatters.DMY.format(date)
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getDeliveryDateDetails(
        addressId: Long,
        date: LocalDate?,
    ): Flow<Result<DeliveryDateDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getDeliveryDateDetails(

                    addressId = addressId,
                    date = date?.format(VodovozDateFormatters.DMY)
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderRecipientDetails(
        addressId: Long,
    ): Flow<Result<RecipientDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getRecipientDetails(
                    addressId = addressId,

                    )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getRecipient(addressId: Long): Flow<Result<RecipientModel>> {
        return executeRequest(
            request = {
                vodovozService.getRecipient(
                    addressId = addressId,

                    )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun sendOrderRecipient(
        addressId: Long,
        params: Map<String, String>,
    ): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.sendOrderRecipient(
                    addressId = addressId,

                    params = params
                )
            },
            mapper = {
                it.data ?: ""
            }
        )
    }

    override fun getOrderCallYouDetails(addressId: Long): Flow<Result<OrderCallYouDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderCallYouDetails(addressId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderingDetails(
        addressId: Long?,
        date: String?,
        timeInterval: String?,
        coupon: String?,
        useBonuses: Boolean?,
        useBalance: Boolean?,
        bonuses: Int?,
    ): Flow<Result<OrderingDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderingDetails(
                    addressId = addressId,
                    date = date,
                    timeInterval = timeInterval,
                    coupon = coupon,
                    useBalance = VodovozBoolean.from(useBonuses).value,
                    useBonuses = VodovozBoolean.from(useBalance).value,
                    bonuses = bonuses
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun doOrder(
        addressId: Long,
        deliveryDate: String,
        deliveryTimeInterval: String,
        userFIO: String,
        userPhone: String,
        userEmail: String?,
        paymentMethodId: Long,
        paymentChange: String?,
        callYouId: Long?,
        coupon: String?,
        deviceInfo: String?,
        notifyDriverId: String?,
        useBonuses: Boolean?,
        useBalance: Boolean?,
        bonuses: Int?,
        message: String?,
        params: Map<String, String>?,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.doOrder(
                    addressId = addressId,
                    deliveryDate = deliveryDate,
                    deliveryTimeInterval = deliveryTimeInterval,
                    userPhone = userPhone,
                    userEmail = userEmail,
                    userFIO = userFIO,
                    paymentMethodId = paymentMethodId,
                    paymentChange = paymentChange,
                    notifyDriverId = notifyDriverId,
                    callYouId = callYouId,
                    coupon = coupon,
                    deviceInfo = deviceInfo,
                    queries = params,
                    message = message,
                    useBalance = VodovozBoolean.from(useBalance).value,
                    useBonuses = VodovozBoolean.from(useBonuses).value,
                    bonuses = bonuses
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }


    override fun orderService(
        serviceType: String,
        queries: Map<String, String>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.orderService(

                    serviceType = serviceType,
                    queries = queries
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getServiceOrderDetails(serviceType: String): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                vodovozService.getServiceOrderDetails(serviceType)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun removeFirebaseToken(token: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.removeFirebaseToken(token)
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override fun sendFirebaseToken(token: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.sendFirebaseToken(token)
            },
            mapper = {
                it.message ?: ""
            }
        )

    }

    override fun getOrdersHistoryDetails(): Flow<Result<OrdersHistoryDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrdersHistoryDetails(

                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getOrdersHistoryItemsPaged(
        statuses: String,
        searchQuery: String,
    ): Flow<PagingData<OrdersHistoryItemModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = OrdersHistoryDetailsDTO::class,
            request = { page, _ ->
                vodovozService.getOrdersHistoryDetails(

                    page = page,
                    statuses = if (searchQuery.isNotBlank()) null else statuses,
                    search = searchQuery.takeIf { it.isNotBlank() }
                )
            },
            mapper = { dto ->
                dto.toDomain().items
            }
        )
    }

    override fun repeatOrder(orderId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.repeatOrder(orderId)
            },
            mapper = { response ->
                response.data ?: ""
            }
        )
    }

    override fun getWaitFeedbackProductsTitle(): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.getWaitFeedbackProducts()
            },
            mapper = {
                it.data?.products!!.isEmpty()

                it.data.title ?: ""
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getWaitFeedbackProductsPaged(): Flow<PagingData<WaitFeedbackProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = WaitFeedbackProductsDTO::class,
            request = { page, _ ->
                vodovozService.getWaitFeedbackProducts(

                    page
                )
            },
            mapper = { dto ->
                dto.products!!.mapToDomain()
            }
        )
    }

    override fun getNotificationSettingsDetails(): Flow<Result<NotificationSettingsDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getNotificationSettingsDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun updateNotificationSettings(params: Map<String, String>): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.updateNotificationSettings(

                    params
                )
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override fun getRecoverPasswordDetails(): Flow<Result<AuthDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getRecoverPasswordDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun recoverPassword(fields: List<FieldModel>): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.recoverPassword(fields.toQueries())
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { it ->
                val errorMessage =
                    moshi.fromJson<VodovozResponseDTO<String>>(it.stringErrorBody()).message ?: ""

                throw RequestException(errorMessage)
            }
        )
    }

    override fun requestPhoneCode(
        url: String,
        phone: String,
        params: Map<String, String>,
    ): Flow<Result<RequestCodeModel>> {
        return executeRequest(
            request = {
                vodovozService.requestPhoneCode(
                    url = url,
                    phone = phone,
                    params = params
                )
            },
            mapper = {
                val mapResult = it.data!!.toDomain()
                if (it.status == "429") {
                    throw TooManyRequestsException(remainingSeconds = mapResult.remainingSeconds)
                }
                mapResult
            }
        )
    }

    override fun loginByPhone(
        url: String,
        code: String,
        phone: String,
    ): Flow<Result<UserAuthInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.loginByPhone(url, phone, code)
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val json = response.stringErrorBody()
                val errorMessage = moshi.fromJson<VodovozResponseDTO<String>>(json).message ?: ""
                throw RequestException(errorMessage)
            }
        )
    }

    override fun getAllServicesDetails(): Flow<Result<AllServicesDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAllServicesDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getServiceDetails(serviceId: Int): Flow<Result<ServiceDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getServiceDetails(serviceId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getQuestionnairesWelcomeDetails(): Flow<Result<QuestionnairesWelcomeDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getQuestionnairesWelcomeDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getQuestionnairesDetails(who: String): Flow<Result<QuestionnairesDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getQuestionnairesDetails(who)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun sendQuestionnairesAnswers(
        who: String,
        answers: String,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.sendQuestionnaires(
                    who = who,

                    answers = answers
                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val message =
                    moshi.fromJson<VodovozResponseDTO<String>>(response.stringErrorBody()).message
                        ?: ""

                throw RequestException(message = message)
            }
        )
    }


    override fun getCancelOrderDetails(
        orderId: Long,
    ): Flow<Result<CancelOrderDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getCancelOrderDetails(orderId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun cancelOrder(
        orderId: Long,
        params: Map<String, String>,
    ): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.cancelOrder(

                    orderId = orderId,
                    queries = params
                )
            },
            mapper = {
                it.data ?: ""
            }
        )
    }

    override fun sendOrderQuestion(
        orderId: Long,
        fields: List<FieldModel>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.sendOrderQuestion(orderId, fields.toQueries())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderQuestionDetails(orderId: Long): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderQuestionDetails(orderId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderDetails(orderId: Long): Flow<Result<OrderDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderDetails(orderId)
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                throw UserNotLoginException(placeholder = placeholder)
            }
        )
    }

    override fun getAllBottles(): Flow<Result<AllBottlesDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAllBottles()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getWhereMyOrderDetails(
        orderId: Long,
        driverId: String,
    ): Flow<Result<WhereOrderDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getWhereMyOrderDetails(

                    orderId = orderId,
                    driverId = driverId
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getAddresses(): Flow<Result<List<SectionModel<AddressModel>>>> {
        return executeRequest(
            request = {
                vodovozService.getAddresses()
            },
            mapper = {
                it.data!!.toDomain().ifEmpty {
                    throw IllegalArgumentException("Addresses can't be empty")
                }
            },
            fail = { response ->
                val body = response.stringErrorBody()
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(body).data!!
                throw EmptyResultException(placeholder = placeholder.toDomain())
            }
        )
    }

    override fun getMapAreas(): Flow<Result<MapZonesModel>> {
        return executeRequest(
            request = {
                vodovozService.getMapAreas()
            },
            mapper = { it.data!!.toDomain() }
        )
    }

    override fun getPastPurchasesDetails(
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getPastPurchasesDetails(

                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { id -> id > 0 }
                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getPastPurchasesPaged(
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getPastPurchasesDetails(
                    page = page,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { id -> id > -1 }
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Past purchases can't be null")
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBrands(
        searchQuery: String,
    ): Flow<Result<BrandSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getBrands(
                    page = if (searchQuery.isBlank()) 1 else null,
                    search = searchQuery.takeIf { s -> s.isNotEmpty() }
                )
            },
            mapper = { vodovozResponse ->
                vodovozResponse.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBrandsPaged(
        searchQuery: String,
    ): Flow<PagingData<BrandModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = BrandSectionDTO::class,
            request = { page, _ ->
                if (page > 1 && searchQuery.isNotBlank()) {
                    throw EmptyResultException()
                }
                vodovozService.getBrands(
                    page = if (searchQuery.isBlank()) page else null,
                    search = searchQuery.takeIf { s -> s.isNotEmpty() }
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Brands can't be null")
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBrandProducts(
        brandId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getBrandProducts(
                    brandId = brandId,
                    page = 1,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { response ->
                response.data?.toDomain()!!
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBrandProductsPaged(
        brandId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getBrandProducts(
                    brandId = brandId,
                    page = page,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Brand products can't be null")
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBannerPromotions(
        bannerId: Long,
        blockId: Long,
        categoryId: Int,
    ): Flow<Result<PromotionsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getBannerPromotions(
                    bannerId = bannerId,
                    blockId = blockId,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getBannerPromotionsPaged(
        bannerId: Long,
        blockId: Long,
        categoryId: Int?,
    ): Flow<PagingData<PromotionModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = PromotionsDTO::class,
            request = { page, _ ->
                vodovozService.getBannerPromotions(
                    bannerId = bannerId,
                    blockId = blockId,
                    page = page,
                    categoryId = categoryId
                )
            },
            mapper = { dto ->
                dto.DATA!!.mapToDomain()
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBannerProducts(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getBannerProducts(
                    bannerId = bannerId,
                    blockId = blockId,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBannerProductsPaged(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getBannerProducts(
                    bannerId = bannerId,
                    blockId = blockId,
                    order = sort.order,
                    sort = sort.value,
                    page = page,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Banner products can't be null")
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getLoginDetails(): Flow<Result<AuthDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getLoginDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getLoginByEmailDetails(): Flow<Result<AuthDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getLoginByEmailDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun updatePassword(password: String): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.updatePassword(password)
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                throw try {
                    val message =
                        moshi.fromJson<VodovozErrorResponseDTO>(response.stringErrorBody()).message
                    ValidationException(message = message ?: "")
                } catch (_: Throwable) {
                    val placeholder =
                        moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data
                    UserNotLoginException(placeholder = placeholder?.toDomain())
                }
            }
        )
    }

    override fun getChangePasswordDetails(): Flow<Result<ChangePasswordDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getChangePasswordDetails()
            },
            mapper = { vodovozResponse ->
                vodovozResponse.data!!.toDomain()
            }
        )
    }

    override fun updateUserAvatar(avatarFile: File): Flow<Result<String>> {
        return executeRequest(
            request = {
                val requestBody = avatarFile.asRequestBody(avatarFile.extension.toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData(
                    name = "userpic",
                    filename = avatarFile.name,
                    body = requestBody
                )
                vodovozService.updateUserAvatar(filePart)
            },
            mapper = {
                it.message ?: ""
            },
            fail = {
                val info = it.stringErrorBody()
                throw Exception(info)
            }
        )
    }

    override fun updateUserData(fields: List<FieldModel>): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.updateUserData(
                    fields.toQueries()
                )
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override fun getUserData(): Flow<Result<UserDataModel>> {
        return executeRequest(
            request = {
                vodovozService.getUserData()
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val errorData = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data

                Result.failure(UserNotLoginException(placeholder = errorData!!.toDomain()))
            }
        )

    }

    override fun getProfileDetails(): Flow<Result<ProfileDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getProfileDetails()
            },
            mapper = {
                it.data!!.toDomain()
            },
            fail = { response ->
                val errorData = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data

                Result.failure(UserNotLoginException(placeholder = errorData!!.toDomain()))
            }
        )
    }

    override fun getBonusesPopupWindow(): Flow<Result<BonusesPopupWindowModel>> {
        return executeRequest(
            request = {
                vodovozService.getBonusesPopupWindow()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun updateBonusesSubscribe(subscribe: Boolean): Flow<Result<Unit>> {
        return executeRequest(
            request = {
                vodovozService.updateBonusesSubscribe(

                    VodovozBoolean.from(subscribe).value
                )
            },
            mapper = {}
        )
    }

    override fun getFilters(categoryId: Int): Flow<Result<FiltersModel>> {
        return executeRequest(
            request = {
                vodovozService.getFilters(categoryId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getFilterValues(
        categoryId: Int,
        filterId: String,
    ): Flow<Result<List<FilterValueModel>>> {
        return executeRequest(
            request = {
                vodovozService.getFilterValues(categoryId, filterId)
            },
            mapper = {
                it.data!!.map { filterValue ->
                    FilterValueModel(
                        filterValue,
                        HtmlCompat.fromHtml(
                            filterValue, HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toString()
                    )
                }
            }
        )
    }

    override fun buyCertificate(params: Map<String, String>): Flow<Result<BuyCertificateModel>> {
        return executeRequest(
            request = {
                vodovozService.buyCertificate(params)
            },
            mapper = { it.data!!.toDomain() }
        )
    }

    override fun getBuyCertificateDetails(): Flow<Result<BuyCertificateDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getBuyCertificateDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getCertificateActivationDetails(): Flow<Result<CertificateActivationDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getCertificateActivationDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun activateCertificate(field: FieldUi): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.activateCertificate(
                    mapOf(field.id to field.value.trim())
                )
            },
            mapper = {
                it.message ?: ""
            },
            fail = { response ->
                val errorDTO = moshi.fromJson<VodovozResponseDTO<String>>(
                    response.stringErrorBody(),
                    Types.newParameterizedType(VodovozResponseDTO::class.java, String::class.java)
                )
                throw RequestException(errorDTO.message ?: "")
            }
        )
    }

    override fun getRegisterDetails(): Flow<Result<AuthDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getRegisterFields()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun logout(): Flow<Result<Unit>> {
        return executeRequest(
            request = {
                vodovozService.logout()
            },
            mapper = {}
        )
    }

    override fun deleteAccount(): Flow<Result<Unit>> {
        return executeRequest(
            request = {
                vodovozService.deleteAccount()
            },
            mapper = {}
        )
    }

    override fun relogin(): Flow<Result<Boolean>> {
        return executeRequest(
            request = {
                val id = accountManager.fetchAccountId()
                val token = accountManager.fetchUserToken()
                if (id == null || token == null) {
                    throw UserNotLoginException()
                }
                vodovozService.relogin(token)
            },
            mapper = { response ->
                if (response.data == false || response.data == null) {
                    throw UserBlockedException(message = response.message ?: "")
                }
                response.data
            },
            response = onResponse@{ response ->
                if (response.code() != 200) return@onResponse

                val cookies = response.headers().values("Set-Cookie")
                val sessionId = cookies.firstOrNull { s -> s.startsWith("PHPSESSID=") }
                cookieManager.updateCookieSessionId(sessionId)
            },
            fail = { response ->
                val code = response.code()

                when {
                    code == 402 || code == 404 -> {
                        throw UserBlockedException(message = response.messageWithCode())
                    }

                    else -> {
                        throw RequestException()
                    }
                }
            }
        )
    }

    override fun register(params: Map<String, String>): Flow<Result<UserAuthInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.register(params)
            },
            mapper = { registerDTO ->
                registerDTO.data!!.toDomain()
            },
            fail = { response ->
                val jsonBody = response.stringErrorBody().ifEmpty { response.stringBody() }

                throw when (response.code()) {
                    404 -> {
                        val message = moshi.fromJson<VodovozErrorResponseDTO>(
                            json = jsonBody
                        ).message ?: ""

                        ValidationException(message = message)
                    }

                    else -> {
                        RequestException(response.messageWithCode())
                    }
                }
            }
        )
    }

    override fun loginByEmail(params: Map<String, String>): Flow<Result<UserAuthInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.loginByEmail(params)
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            fail = { response ->
                val jsonBody = response.stringErrorBody()
                val errorResponse = moshi.fromJson<VodovozResponseDTO<String>>(jsonBody)

                Result.failure(ValidationException(errorResponse.message ?: ""))
            }
        )
    }

    override fun getCatalogDetails(): Flow<Result<CatalogDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getCatalogDetails()
            },
            mapper = { response ->
                response.data?.toDomain()!!
            }
        )
    }

    override fun getCategoryTree(categoryId: Long): Flow<Result<List<ParentCategoryModel>>> {
        return executeRequest(
            request = {
                vodovozService.getCategoryTree(categoryId)
            },
            mapper = {
                it.data!!.mapToDomain()
            }
        )
    }

    override fun getCategoryProducts(
        categoryId: Long,
        filters: FiltersModel,
    ): Flow<Result<ProductsSectionModel>> {
        val filtersQuery = filters.filters.joinToString(",") { it.name }
        val filtersAndValuesQuery = filters.filters.filter { it.currentBounds == null }.format()
        val boundsMap = filters.filters.toSliderQueries()

        return executeRequest(
            request = {
                vodovozService.getCategoryProducts(
                    categoryId = categoryId,
                    filters = filtersQuery.takeIf { s -> s.isNotBlank() },
                    filtersAndValues = filtersAndValuesQuery.takeIf { s -> s.isNotBlank() },
                    priceTo = filters.priceRange.last.toFloat(),
                    priceFrom = filters.priceRange.first.toFloat(),
                    queries = boundsMap
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data

                throw EmptyResultException(placeholder = placeholder!!.toDomain())
            }
        )
    }

    override fun getCategoryProductsPaged(
        categoryId: Long,
        sort: SortModel,
        filters: FiltersModel,
    ): Flow<PagingData<ProductModel>> {

        val filtersQuery = filters.filters.joinToString(",") { it.name }
        val filtersAndValuesQuery = filters.filters.format()
        val boundsMap = filters.filters.toSliderQueries()

        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getCategoryProducts(
                    page = page,
                    categoryId = categoryId,
                    sort = sort.value,
                    order = sort.order,
                    filters = filtersQuery.takeIf { s -> s.isNotBlank() },
                    filtersAndValues = filtersAndValuesQuery.takeIf { s -> s.isNotBlank() },
                    priceTo = filters.priceRange.last.toFloat(),
                    priceFrom = filters.priceRange.first.toFloat(),
                    queries = boundsMap
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Paged search products can't be null")
            }
        )
    }


    override fun getSearchProductsPaged(
        query: String,
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getSearchProducts(
                    query = query,
                    page = page,
                    categoryId = if (categoryId < 0) null else categoryId,
                    sort = sort.value,
                    order = sort.order
                )
            },
            mapper = { dto ->
                dto.TOVAR?.mapToDomain()
                    ?: throw IllegalArgumentException("Paged search products can't be null")
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data
                throw EmptyResultException(placeholder = placeholder?.toDomain())
            }
        )
    }

    override fun getSearchProducts(
        query: String,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getSearchProducts(
                    query = query,
                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data
                throw EmptyResultException(placeholder = placeholder?.toDomain())
            }
        )
    }

    override fun getBarCodeProducts(barCode: String): Flow<Result<List<ProductModel>>> {
        return executeRequest(
            request = {
                vodovozService.getSearchProducts(
                    query = barCode,
                    isCamera = VodovozBoolean.True.value
                )
            },
            mapper = {
                it.data!!.TOVAR!!.mapToDomain()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data
                throw EmptyResultException(placeholder = placeholder?.toDomain())
            }
        )
    }

    override fun getSearchRecommendations(): Flow<Result<SearchRecommendationsModel>> {
        return executeRequest(
            request = {
                vodovozService.getSearchRecommendations()
            },
            mapper = { responseDTO ->
                responseDTO.data?.toDomain()!!
            }
        )
    }

    override fun getMiniSearchRecommendations(query: String): Flow<Result<SearchRecommendationsModel>> {
        return executeRequest(
            request = {
                vodovozService.getMiniSearchRecommendations(query)
            },
            mapper = { responseDTO ->
                responseDTO.data?.toDomain()!!
            },
            fail = { response ->
                val value =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody())
                throw EmptyResultException(placeholder = value.data!!.toDomain())
            }
        )
    }

    override fun getSiteState(): Flow<Result<AppConfig>> {
        return executeRequest(
            request = {
                vodovozService.getSiteState()
            },
            mapper = { response ->
                val siteState = response.toDomain()
                siteState
            },
            fail = onFail@{ response ->
                runCatching {
                    moshi.fromJson<SiteStateResponseDTO>(
                        response.stringErrorBody()
                    ).toDomain()
                }.recoverCatching {
                    throw RequestException(response.messageWithCode())
                }
            }
        )
    }

    override fun getPreorderDetails(productId: Long): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                vodovozService.getPreOrderDetails(productId)
            },
            mapper = {
                it.data?.toDomain() ?: throw IllegalArgumentException("PreorderDTO can't be null")
            }
        )
    }

    override fun sendPreorder(productId: Long, queries: Map<String, String>): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.sendPreorder(productId, queries)
            },
            mapper = { response -> response.message ?: "" },
            fail = { response ->
                val jsonBody = response.stringErrorBody()
                val responseBody = moshi.fromJson<VodovozErrorResponseDTO>(jsonBody)
                Result.failure(ValidationException(responseBody.message ?: ""))
            }
        )
    }

    override fun getUnratedProductsDetails(): Flow<Result<UnratedProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getUnratedProductsDetails()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun removeUnratedProduct(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.removeUnratedProduct(productId)
            },
            mapper = {
                it.data ?: ""
            }
        )
    }

    override fun getFavoriteProducts(productsIds: String): Flow<Result<ProductsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getFavoriteProducts(
                    productsIds = if (accountManager.fetchAccountId() == null
                    ) productsIds else null
                )
            },
            mapper = { responseDTO ->
                responseDTO.data!!.toDomain()
            },
            fail = { response ->
                val value =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody())
                throw EmptyResultException(placeholder = value.data!!.toDomain())
            }
        )

    override fun getFavoriteProductsPaged(
        categoryId: Int,
        sort: SortModel,
        productsIds: String,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getFavoriteProducts(
                    page = page,
                    categoryId = categoryId.takeIf { value -> value != -1 },
                    sort = sort.value,
                    order = sort.order,
                    productsIds = if (accountManager.fetchAccountId() == null) productsIds else null
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain()
                    ?: throw IllegalArgumentException("Paged favorite products can't be null")
            }
        )
    }

    override suspend fun addFavoriteProducts(productsIds: String): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getFavoriteProducts(productsIds = productsIds)
            },
            mapper = { response ->
                response.data?.toDomain()!!
            }
        )
    }

    override suspend fun addProductToFavorites(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.addToFavorites(productId)
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override suspend fun removeProductFromFavorites(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.removeFromFavorites(productId)
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override suspend fun getBottomCart(): Flow<Result<BottomCartModel>> {
        return executeRequest(
            request = {
                vodovozService.getBottomCart()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override suspend fun getCartDetails(coupon: String?): Flow<Result<CartDetailsModel>> {
        return executeRequest(
            request = {

                vodovozService.getCartDetails(coupon)
            },
            mapper = { vodovozResponse ->
                vodovozResponse.data!!.toDomain()
            },
            fail = { response ->

                val value = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                )
                throw EmptyResultException(placeholder = value.data!!.toDomain())
            }
        )
    }

    override fun getAdditionalProductsBS(
        productsId: Long,
        productsArticle: String,
    ): Flow<Result<AdditionalProductsBSModel>> {
        return executeRequest(
            request = {
                vodovozService.getAdditionalProducts(
                    productsId = productsId,
                    productsArticle = productsArticle,

                    )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getAdditionalProductsPaged(
        productsId: Long,
        productsArticle: String,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = RecommendationsDTO::class,
            request = { page, _ ->
                vodovozService.getAdditionalProducts(
                    page = page,
                    productsId = productsId,
                    productsArticle = productsArticle
                )
            },
            mapper = { dto ->
                dto.products!!.mapToDomain()
            }
        )
    }


    override suspend fun addProductToCart(productId: Long, quantity: Int): Flow<Result<String>> =
        executeRequest(
            request = {
                vodovozService.addProductToCart(productId, quantity)
            },
            mapper = { response ->
                response.data ?: ""
            }
        )

    override suspend fun addMultipleProductsToCart(productIdsWithQuantity: String): Flow<Result<String>> =
        executeRequest(
            request = { vodovozService.addMultipleProductsToCart(productIdsWithQuantity) },
            mapper = { response -> response.data ?: "" }
        )

    override suspend fun replaceMultipleBottlesToCart(cartProducts: CartProductsModel): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.replaceMultipleBottlesToCart(cartProducts.productsIdsWithQuantity)
            },
            mapper = { it.data ?: "" }
        )
    }

    override suspend fun removeProductFromCart(productId: Long): Flow<Result<String>> =
        executeRequest(
            request = { vodovozService.removeProductFromCart(productId) },
            mapper = { response -> response.data ?: "" }
        )

    override suspend fun updateProductInCart(productId: Long, quantity: Int): Flow<Result<String>> =
        executeRequest(
            request = {
                vodovozService.updateProductInCart(productId, quantity)
            },
            mapper = { response ->
                response.data ?: ""
            }
        )

    override suspend fun clearCart(): Flow<Result<String>> = executeRequest(
        request = { vodovozService.clearCart() },
        mapper = { it.data ?: "" },
    )

    override fun getProductAnalogs(
        productId: Long,
        sort: SortModel,
    ): Flow<Result<ProductsSectionModel>> = executeRequest(
        request = {
            vodovozService.getProductAnalogs(productId, sort.value, sort.order)
        },
        mapper = { response ->
            response.data?.toDomain()
                ?: throw IllegalArgumentException("ProductAnalogsDTO can't be null")
        }
    )

    override fun getWriteMessageDetails(): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                vodovozService.getWriteMessageDetails()
            },
            mapper = {
                it.data!!.toDomain()
            },
        )
    }

    override fun sendMessage(params: Map<String, String>): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.sendMessage(params)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun sendComment(
        productId: Long,
        rating: Int,
        message: String,
        imageBytesArray: List<ByteArray>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {

                val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)

                mapOf(
                    "message" to message,
                    "rating_value" to rating.toString(),
                    "id" to productId.toString(),
                    "action" to "add"
                ).forEach { (name, value) ->
                    multipartBuilder.addFormDataPart(name, value)
                }

                imageBytesArray.forEachIndexed { index, bytes ->
                    val fileName = "userpic_$index.jpeg"
                    multipartBuilder.addFormDataPart(
                        name = "commentimages[]",
                        filename = fileName,
                        body = bytes.toRequestBody("image/jpeg".toMediaType())
                    )
                }


                vodovozService.sendComment(body = multipartBuilder.build())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getProductCommentsInfo(productId: Long): Flow<Result<ProductCommentsInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.getComments(productId, 1)
            },
            mapper = { response ->
                response.data?.toDomain()
                    ?: throw IllegalArgumentException("ProductCommentsDTO can't be null")
            }
        )
    }


    override fun getProductCommentsPaged(
        productId: Long,
        sort: SortModel,
    ): Flow<PagingData<CommentModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductCommentsDTO::class,
            request = { page, _ ->
                vodovozService.getComments(
                    productId = productId,
                    page = page,
                    sort = sort.value,
                    order = sort.order
                )
            },
            mapper = { dto ->
                dto.COMMENTS?.mapNotNull { it?.toDomain() } ?: emptyList()
            }
        )
    }

    override fun getProductDetails(productId: Long): Flow<Result<ProductDetailsScreenModel>> =
        executeRequest(
            request = {
                vodovozService.getProductDetails(
                    productId = productId,

                    )
            },
            mapper = { responseDto ->
                responseDto.data?.toDomain()
                    ?: throw IllegalArgumentException("ProductDetails cannot be null")
            }
        )

    override fun getPresentInfo(): Flow<Result<PresentInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.getPresentInfo()
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }


    override fun getPopupWindowInfo(): Flow<Result<PopupWindowInfoModel>> = executeRequest(
        request = {
            vodovozService.getPopupWindowInfo()
        },
        mapper = { responseDTO ->
            responseDTO.data?.toDomain()!!
        }
    )

    override fun getStories(): Flow<Result<List<StoryModel>>> = executeRequest(
        request = {
            vodovozService.getStories()
        },
        mapper = { responseDTO ->
            responseDTO.data?.toDomain()!!
        }
    )

    override fun getBanners(): Flow<Result<List<BannerModel>>> = executeRequest(
        request = {
            vodovozService.getBanners()
        },
        mapper = { promotionsDTOVodovozResponseDTO ->
            promotionsDTOVodovozResponseDTO.data?.mapToDomain()!!
        }
    )

    override fun getPromotions(): Flow<Result<PromotionsSectionModel>> = executeRequest(
        request = {
            vodovozService.getPromotions()
        },
        mapper = { promotionsDTOVodovozResponseDTO ->
            promotionsDTOVodovozResponseDTO.data?.toDomain()!!
        }
    )

    override fun getPromotionDetails(promotionId: Int): Flow<Result<Pair<ProductsTitle, PromotionDetailsModel>>> =
        executeRequest(
            request = {
                vodovozService.getPromotionDetails(promotionId)
            },
            mapper = { response ->
                ProductsTitle(
                    response.data?.TOVAR?.NAMETOVAR ?: ""
                ) to response.data?.AKCIYA?.toDomain()!!
            }
        )


    override fun getPromotionDetailsProductsPaged(
        promotionId: Int,
        limit: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = PromotionDetailsDTO::class,
            request = { page, limit ->
                vodovozService.getPromotionDetails(promotionId, page, limit)
            },
            mapper = { dto ->
                dto.TOVAR?.DATA?.mapToDomain() ?: emptyList()
            }
        )
    }


    override fun getAllPromotionsDetails(categoryId: Int): Flow<Result<PromotionsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getAllPromotions(
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
        )

    override fun getAllPromotionsPaged(
        limit: Int,
        categoryId: Int?,
    ): Flow<PagingData<PromotionModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = PromotionsDTO::class,
            request = { page, limit ->
                vodovozService.getAllPromotions(
                    page = page,
                    limit = limit,
                    categoryId = categoryId
                )
            },
            mapper = { dto ->
                dto.toDomain().promotions
            },
        )
    }


    override fun getOrderMenu(): Flow<Result<OrderWithMenuModel>> = executeRequest(
        request = {
            vodovozService.getOrderMenu()
        },
        mapper = {
            it.data?.toDomain()!!
        }
    )


    override fun getPopularCategories(): Flow<Result<SectionModel<PopularCategoryModel>>> =
        executeRequest(
            request = {
                vodovozService.getPopularCategories()
            },
            mapper = { popularCategoriesDTOVodovozResponseDTO ->
                popularCategoriesDTOVodovozResponseDTO.data?.toDomain()!!
            }
        )

    override fun getNewProducts(): Flow<Result<SectionModel<ProductModel>>> = executeRequest(
        request = {
            vodovozService.getNewProducts()
        },
        mapper = { razdelDTO ->
            razdelDTO.data?.toDomain()!!
        }
    )

    override fun getAllNewProducts(categoryId: Int): Flow<Result<ProductsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getAllNewProducts(categoryId = categoryId)
            },
            mapper = { response ->
                response.data?.toDomain()
                    ?: throw IllegalArgumentException("NewProductsDTO can't be null")
            }
        )

    override fun getAllNewProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getAllNewProducts(
                    page = page,
                    categoryId = categoryId,
                    sort = sort.value,
                    order = sort.order
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain() ?: emptyList()
            }
        )
    }


    override fun getHurryUpBuyProducts(): Flow<Result<SectionModel<ProductModel>>> = executeRequest(
        request = {
            vodovozService.getHurryUpBuyProducts()
        },
        mapper = { dto ->
            dto.data?.toDomain()!!
        }
    )

    override suspend fun getAllHurryUpBuyProducts(
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getAllHurryUpBuyProducts(
                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            mapper = { responseDto ->
                responseDto.data?.toDomain()
                    ?: throw IllegalArgumentException("ProductSectionDTO can't be null")
            }
        )

    override fun getAllHurryUpBuyProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getAllHurryUpBuyProducts(
                    page = page,
                    categoryId = categoryId,
                    sort = sort.value,
                    order = sort.order
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain() ?: emptyList()
            }
        )
    }

    override fun getSuperTopCategories(): Flow<Result<SuperTopModel>> = executeRequest(
        request = {
            vodovozService.getSuperTopCategories()
        },
        mapper = { topAndBottomDTO ->
            topAndBottomDTO.data?.toDomain()
                ?: throw IllegalArgumentException("SuperTop can't be null")
        }
    )

    override fun getSuperTopProducts(categoryId: Long): Flow<Result<List<ProductModel>>> =
        executeRequest(
            request = { vodovozService.getSuperTopByCategory(categoryId) },
            mapper = { it.data!!.mapToDomain() }
        )


    override fun getAllSuperTop(
        buttonId: Int,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> = executeRequest(
        request = {
            vodovozService.getAllSuperTop(
                id = buttonId.toLong(),
                categoryId = categoryId.takeIf { it > -1 }
            )
        },
        mapper = { superTopResponse ->
            superTopResponse.data!!.toDomain()
        },
        fail = { response ->
            val placeholder =
                moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
            throw EmptyResultException(placeholder = placeholder)
        }
    )

    override fun getAllSuperTopPaged(
        buttonId: Int,
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getAllSuperTop(
                    id = buttonId.toLong(),
                    page = page,
                    categoryId = categoryId.takeIf { it > 0 },
                    sort = sort.value,
                    order = sort.order
                )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain() ?: emptyList()
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getViewedProducts(): Flow<Result<SectionModel<ProductModel>>> {
        return executeRequest(
            request = {
                vodovozService.getViewedProducts()
            },
            mapper = {
                it.data?.toDomain()
                    ?: throw IllegalArgumentException("Viewed products can't be null")
            }
        )
    }

    override fun getAllViewedProducts(
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getAllViewedProducts(

                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            mapper = {
                it.data?.toDomain()
                    ?: throw IllegalArgumentException("All Viewed products can't be null")
            },
            fail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getAllViewedProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            clazz = ProductsSectionDTO::class,
            request = { page, _ ->
                vodovozService.getAllViewedProducts(
                    page = page,
                    categoryId = categoryId.takeIf { it > 0 },
                    sort = sort.value,
                    order = sort.order,

                    )
            },
            mapper = { dto ->
                dto.DATA?.mapToDomain() ?: emptyList()
            },
            fail = { response ->
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                ).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }


}