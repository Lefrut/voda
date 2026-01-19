package com.m.vodovoz.data.vodovoz_service.repository

import androidx.core.text.HtmlCompat
import androidx.paging.PagingData
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.model.AppConfig
import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.core.network.retrofit.messageWithCode
import com.m.vodovoz.core.network.retrofit.stringErrorBody
import com.m.vodovoz.core.network.serialization.fromJson
import com.m.vodovoz.data.vodovoz_service.VodovozRequestExecutor
import com.m.vodovoz.data.vodovoz_service.VodovozService
import com.m.vodovoz.data.vodovoz_service.executeRequest
import com.m.vodovoz.data.vodovoz_service.mappers.mapToDomain
import com.m.vodovoz.data.vodovoz_service.mappers.toDomain
import com.m.vodovoz.data.vodovoz_service.model.BrandSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductCommentsDTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.PromotionsDTO
import com.m.vodovoz.data.vodovoz_service.model.SiteStateResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.m.vodovoz.data.vodovoz_service.model.VodovozResponseDTO
import com.m.vodovoz.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.RecommendationsDTO
import com.m.vodovoz.data.vodovoz_service.model.messageOrEmpty
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
import kotlin.reflect.jvm.javaType
import kotlin.reflect.typeOf


@Singleton
class VodovozServiceRepositoryImpl @Inject constructor(
    private val vodovozService: VodovozService,
    private val accountManager: AccountManager,
    private val cookieManager: CookieManager,
    private val moshi: Moshi,
) : VodovozServiceRepository {

    private val defaultExecutor = object : VodovozRequestExecutor(moshi) {
        override fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
            val json = response.stringErrorBody()
            val errorMessage = runCatching {
                moshi.fromJson<VodovozResponseDTO<String>>(json).messageOrEmpty
            }.getOrDefault(response.messageWithCode())
            throw RequestException(errorMessage)
        }
    }

    private val canBeEmptyExecutor = object : VodovozRequestExecutor(moshi) {
        override fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
            val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                response.stringErrorBody()
            ).data!!.toDomain()

            return throw EmptyResultException(placeholder = placeholder)
        }
    }

    private val userExecutor = object : VodovozRequestExecutor(moshi) {
        override fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
            val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                response.stringErrorBody()
            ).data!!.toDomain()
            throw UserNotLoginException(placeholder = placeholder)
        }
    }

    private val validationAwareExecutor = object : VodovozRequestExecutor(moshi) {
        override fun <R : Any> onFail(response: Response<ResponseBody>): Result<R> {
            val json = response.stringErrorBody()

            throw runCatching {
                val message = moshi.fromJson<VodovozResponseDTO<Unit?>>(
                    json
                ).messageOrEmpty
                ValidationException(message = message)
            }.recoverCatching {
                val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    json
                ).data!!.toDomain()
                UserNotLoginException(placeholder = placeholder)
            }.getOrThrow()
        }
    }

    private inline fun <reified T : Any?, reified R : Any> executeCanEmptyRequest(
        noinline request: suspend () -> Response<VodovozResponseDTO<T>>,
        noinline toDomain: T.() -> R = { this as R },
        noinline mapper: (VodovozResponseDTO<T>) -> R = {
            it.data!!.toDomain()
        }
    ): Flow<Result<R>> {
        return canBeEmptyExecutor.executeRequest(
            request = request,
            toDomain = toDomain,
            mapper = mapper
        )
    }

    private inline fun <reified T : Any?, reified R : Any> executeUserRequest(
        noinline request: suspend () -> Response<VodovozResponseDTO<T>>,
        noinline toDomain: T.() -> R = { this as R },
        noinline mapper: (VodovozResponseDTO<T>) -> R = {
            it.data!!.toDomain()
        }
    ): Flow<Result<R>> {
        return userExecutor.executeRequest(
            request = request,
            toDomain = toDomain,
            mapper = mapper
        )
    }

    private inline fun <reified T : Any?, reified R : Any> executeDefaultRequest(
        noinline request: suspend () -> Response<VodovozResponseDTO<T>>,
        noinline toDomain: T.() -> R = { this as R },
        noinline mapper: (VodovozResponseDTO<T>) -> R = {
            it.data!!.toDomain()
        }
    ): Flow<Result<R>> {
        return defaultExecutor.executeRequest(
            request = request,
            toDomain = toDomain,
            mapper = mapper
        )
    }

    override fun removeAddress(addressId: Int): Flow<Result<String>> {
        return executeDefaultRequest(
            request = { vodovozService.deleteAddress(addressId) },
        )
    }

    override fun addAddress(
        address: MapAddressModel,
        params: Map<String, String>,
    ): Flow<Result<Long>> {
        return executeDefaultRequest(
            request = {
                val point = address.point
                vodovozService.addAddress(
                    geo = "${point.lat},${point.lon}",
                    city = address.city,
                    street = address.street,
                    fromMoscowToAddressKm = address.fromMoscowToPoint,
                    queries = params
                )
            }
        )
    }

    override fun getAddAddressDetails(addressId: Long?): Flow<Result<AddAddressDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getAddAddressDetails(addressId)
            }
        )
    }

    override fun getAddressLabels(): Flow<Result<AddressLabelsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getAddressLabels()
            }
        )
    }

    override fun addAddressLabel(label: String): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.addAddressLabel(
                    label = label
                )
            },
        )
    }

    override fun deleteAddressLabel(label: String): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.deleteAddressLabel(

                    label = label
                )
            }
        )

    }

    override fun deleteAllAddressLabels(): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.deleteAllAddressLabels()
            },
        )
    }

    override fun updateAddress(
        addressId: Long,
        address: MapAddressModel?,
        params: Map<String, String>,
    ): Flow<Result<String>> {


        return executeDefaultRequest(
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
            mapper = { it.messageOrEmpty }
        )
    }

    override fun getPaymentMethodDetails(
        addressId: Long,
        date: LocalDate,
    ): Flow<Result<PaymentMethodDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getPaymentMethodDetails(
                    addressId = addressId,
                    date = VodovozDateFormatters.DMY.format(date)
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getDeliveryDateDetails(
        addressId: Long,
        date: LocalDate?,
    ): Flow<Result<DeliveryDateDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getDeliveryDateDetails(
                    addressId = addressId,
                    date = date?.format(VodovozDateFormatters.DMY)
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getOrderRecipientDetails(
        addressId: Long,
    ): Flow<Result<RecipientDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getRecipientDetails(
                    addressId = addressId
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getRecipient(addressId: Long): Flow<Result<RecipientModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getRecipient(addressId = addressId)
            },
            toDomain = { toDomain() }
        )
    }

    override fun sendOrderRecipient(
        addressId: Long,
        params: Map<String, String>,
    ): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.sendOrderRecipient(
                    addressId = addressId,
                    params = params
                )
            }
        )
    }

    override fun getOrderCallYouDetails(addressId: Long): Flow<Result<OrderCallYouDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getOrderCallYouDetails(addressId)
            },
            toDomain = { toDomain() }
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
        return executeDefaultRequest(
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
            toDomain = { toDomain() }
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
        return executeDefaultRequest(
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
            toDomain = { toDomain() }
        )
    }


    override fun orderService(
        serviceType: String,
        queries: Map<String, String>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.orderService(
                    serviceType = serviceType,
                    queries = queries
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getServiceOrderDetails(serviceType: String): Flow<Result<FormModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getServiceOrderDetails(serviceType)
            },
            toDomain = { toDomain() }
        )
    }

    override fun removeFirebaseToken(token: String): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.removeFirebaseToken(token)
            },
            mapper = { it.messageOrEmpty }
        )
    }

    override fun sendFirebaseToken(token: String): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.sendFirebaseToken(token)
            },
            mapper = { it.messageOrEmpty }
        )

    }

    override fun getOrdersHistoryDetails(): Flow<Result<OrdersHistoryDetailsModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getOrdersHistoryDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getOrdersHistoryItemsPaged(
        statuses: String,
        searchQuery: String,
    ): Flow<PagingData<OrdersHistoryItemModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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
        return executeDefaultRequest(
            request = {
                vodovozService.repeatOrder(orderId)
            }
        )
    }

    override fun getWaitFeedbackProductsTitle(): Flow<Result<String>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getWaitFeedbackProducts()
            },
            toDomain = {
                products!!
                title ?: ""
            },
        )
    }

    override fun getWaitFeedbackProductsPaged(): Flow<PagingData<WaitFeedbackProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,
            request = { page, _ ->
                vodovozService.getWaitFeedbackProducts(page)
            },
            mapper = { dto ->
                dto.products!!.mapToDomain()
            }
        )
    }

    override fun getNotificationSettingsDetails(): Flow<Result<NotificationSettingsDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getNotificationSettingsDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun updateNotificationSettings(params: Map<String, String>): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.updateNotificationSettings(
                    params
                )
            },
            mapper = {
                it.messageOrEmpty
            }
        )
    }

    override fun getRecoverPasswordDetails(): Flow<Result<AuthDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getRecoverPasswordDetails()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun recoverPassword(fields: List<FieldModel>): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.recoverPassword(fields.toQueries())
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun requestPhoneCode(
        url: String,
        phone: String,
        params: Map<String, String>,
    ): Flow<Result<RequestCodeModel>> {
        return executeDefaultRequest(
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
        return executeDefaultRequest(
            request = {
                vodovozService.loginByPhone(url, phone, code)
            },
            toDomain = { toDomain() },
        )
    }

    override fun getAllServicesDetails(): Flow<Result<AllServicesDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getAllServicesDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getServiceDetails(serviceId: Int): Flow<Result<ServiceDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getServiceDetails(serviceId)
            },
            toDomain = { toDomain() }
        )
    }

    override fun getQuestionnairesWelcomeDetails(): Flow<Result<QuestionnairesWelcomeDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getQuestionnairesWelcomeDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getQuestionnairesDetails(who: String): Flow<Result<QuestionnairesDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getQuestionnairesDetails(who)
            },
            toDomain = { toDomain() }
        )
    }

    override fun sendQuestionnairesAnswers(
        who: String,
        answers: String,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.sendQuestionnaires(
                    who = who,

                    answers = answers
                )
            },
            toDomain = { toDomain() },
        )
    }


    override fun getCancelOrderDetails(
        orderId: Long,
    ): Flow<Result<CancelOrderDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getCancelOrderDetails(orderId)
            },
            toDomain = { toDomain() }
        )
    }

    override fun cancelOrder(
        orderId: Long,
        params: Map<String, String>,
    ): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.cancelOrder(

                    orderId = orderId,
                    queries = params
                )
            },
        )
    }

    override fun sendOrderQuestion(
        orderId: Long,
        fields: List<FieldModel>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.sendOrderQuestion(orderId, fields.toQueries())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderQuestionDetails(orderId: Long): Flow<Result<FormModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getOrderQuestionDetails(orderId)
            },
            toDomain = { toDomain() }
        )
    }

    override fun getOrderDetails(orderId: Long): Flow<Result<OrderDetailsModel>> {
        return executeUserRequest(
            request = {
                vodovozService.getOrderDetails(orderId)
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getAllBottles(): Flow<Result<AllBottlesDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getAllBottles()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getWhereMyOrderDetails(
        orderId: Long,
        driverId: String,
    ): Flow<Result<WhereOrderDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getWhereMyOrderDetails(
                    orderId = orderId,
                    driverId = driverId
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getAddresses(): Flow<Result<List<SectionModel<AddressModel>>>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getAddresses()
            },
            toDomain = {
                toDomain().ifEmpty {
                    throw IllegalArgumentException("Addresses can't be empty")
                }
            },
        )
    }

    override fun getMapAreas(): Flow<Result<MapZonesModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getMapAreas()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getPastPurchasesDetails(
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getPastPurchasesDetails(
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { id -> id > 0 }
                )
            },
            toDomain = { toDomain() },
        )
    }

    override fun getPastPurchasesPaged(
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
        )
    }

    override fun getBrands(
        searchQuery: String,
    ): Flow<Result<BrandSectionModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getBrands(
                    page = if (searchQuery.isBlank()) 1 else null,
                    search = searchQuery.takeIf { s -> s.isNotEmpty() }
                )
            },
            toDomain = { toDomain() }
        )
    }

    override fun getBrandsPaged(
        searchQuery: String,
    ): Flow<PagingData<BrandModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
            }
        )
    }

    override fun getBrandProducts(
        brandId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getBrandProducts(
                    brandId = brandId,
                    page = 1,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getBrandProductsPaged(
        brandId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
            }
        )
    }

    override fun getBannerPromotions(
        bannerId: Long,
        blockId: Long,
        categoryId: Int,
    ): Flow<Result<PromotionsSectionModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getBannerPromotions(
                    bannerId = bannerId,
                    blockId = blockId,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getBannerPromotionsPaged(
        bannerId: Long,
        blockId: Long,
        categoryId: Int?,
    ): Flow<PagingData<PromotionModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
            }
        )
    }

    override fun getBannerProducts(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getBannerProducts(
                    bannerId = bannerId,
                    blockId = blockId,
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            toDomain = {
                toDomain()
            },
        )
    }

    override fun getBannerProductsPaged(
        bannerId: Long,
        blockId: Long,
        sort: SortModel,
        categoryId: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
                    ?: error("Banner products can't be null")
            },
        )
    }

    override fun getLoginDetails(): Flow<Result<AuthDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getLoginDetails()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getLoginByEmailDetails(): Flow<Result<AuthDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getLoginByEmailDetails()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun updatePassword(password: String): Flow<Result<VodovozPlaceholderModel>> {
        return validationAwareExecutor.executeRequest(
            request = {
                vodovozService.updatePassword(password)
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getChangePasswordDetails(): Flow<Result<ChangePasswordDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getChangePasswordDetails()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun updateUserAvatar(avatarFile: File): Flow<Result<String>> {
        return executeDefaultRequest(
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
                it.messageOrEmpty
            }
        )
    }

    override fun updateUserData(fields: List<FieldModel>): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.updateUserData(
                    fields.toQueries()
                )
            },
            mapper = {
                it.messageOrEmpty
            }
        )
    }

    override fun getUserData(): Flow<Result<UserDataModel>> {
        return executeUserRequest(
            request = {
                vodovozService.getUserData()
            },
            toDomain = {
                toDomain()
            },
        )

    }

    override fun getProfileDetails(): Flow<Result<ProfileDetailsModel>> {
        return executeUserRequest(
            request = {
                vodovozService.getProfileDetails()
            },
            toDomain = { toDomain() },
        )
    }

    override fun getBonusesPopupWindow(): Flow<Result<BonusesPopupWindowModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getBonusesPopupWindow()
            },
            toDomain = { toDomain() }
        )
    }

    override fun updateBonusesSubscribe(subscribe: Boolean): Flow<Result<Unit>> {
        return executeDefaultRequest(
            request = {
                vodovozService.updateBonusesSubscribe(
                    VodovozBoolean.from(subscribe).value
                )
            },
            mapper = {}
        )
    }

    override fun getFilters(categoryId: Int): Flow<Result<FiltersModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getFilters(categoryId)
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getFilterValues(
        categoryId: Int,
        filterId: String,
    ): Flow<Result<List<FilterValueModel>>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getFilterValues(categoryId, filterId)
            },
            toDomain = {
                map { filterValue ->
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
        return executeDefaultRequest(
            request = {
                vodovozService.buyCertificate(params)
            },
            toDomain = { toDomain() }
        )
    }

    override fun getBuyCertificateDetails(): Flow<Result<BuyCertificateDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getBuyCertificateDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getCertificateActivationDetails(): Flow<Result<CertificateActivationDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getCertificateActivationDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun activateCertificate(field: FieldUi): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.activateCertificate(
                    mapOf(field.id to field.value.trim())
                )
            },
            mapper = { it.messageOrEmpty },
        )
    }

    override fun getRegisterDetails(): Flow<Result<AuthDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getRegisterFields()
            },
            toDomain = { toDomain() }
        )
    }

    override fun logout(): Flow<Result<Unit>> {
        return executeDefaultRequest(
            request = {
                vodovozService.logout()
            },
            mapper = {}
        )
    }

    override fun deleteAccount(): Flow<Result<Unit>> {
        return executeDefaultRequest(
            request = {
                vodovozService.deleteAccount()
            },
            mapper = {}
        )
    }

    override fun relogin(): Flow<Result<Boolean>> {
        return defaultExecutor.executeRequestImpl(
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

                throw when {
                    code == 402 || code == 404 -> {
                        UserBlockedException(message = response.messageWithCode())
                    }

                    else -> {
                        RequestException()
                    }
                }
            },
            type = typeOf<VodovozResponseDTO<Boolean>>().javaType
        )
    }

    override fun register(params: Map<String, String>): Flow<Result<UserAuthInfoModel>> {
        return validationAwareExecutor.executeRequest(
            request = {
                vodovozService.register(params)
            },
            toDomain = { toDomain() },
        )
    }

    override fun loginByEmail(params: Map<String, String>): Flow<Result<UserAuthInfoModel>> {
        return validationAwareExecutor.executeRequest(
            request = {
                vodovozService.loginByEmail(params)
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getCatalogDetails(): Flow<Result<CatalogDetailsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getCatalogDetails()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getCategoryTree(categoryId: Long): Flow<Result<List<ParentCategoryModel>>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getCategoryTree(categoryId)
            },
            toDomain = { mapToDomain() }
        )
    }

    override fun getCategoryProducts(
        categoryId: Long,
        filters: FiltersModel,
    ): Flow<Result<ProductsSectionModel>> {
        val filtersQuery = filters.filters.joinToString(",") { it.name }
        val filtersAndValuesQuery = filters.filters.filter { it.currentBounds == null }.format()
        val boundsMap = filters.filters.toSliderQueries()

        return executeCanEmptyRequest(
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
            toDomain = { toDomain() },
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
            executor = defaultExecutor,

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
            executor = canBeEmptyExecutor,

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
        )
    }

    override fun getSearchProducts(
        query: String,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getSearchProducts(
                    query = query,
                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            toDomain = {
                toDomain()
            },
        )
    }

    override fun getBarCodeProducts(barCode: String): Flow<Result<List<ProductModel>>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getSearchProducts(
                    query = barCode,
                    isCamera = VodovozBoolean.True.value
                )
            },
            toDomain = {
                TOVAR!!.mapToDomain()
            },
        )
    }

    override fun getSearchRecommendations(): Flow<Result<SearchRecommendationsModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getSearchRecommendations()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getMiniSearchRecommendations(query: String): Flow<Result<SearchRecommendationsModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getMiniSearchRecommendations(query)
            },
            mapper = { responseDTO ->
                responseDTO.data?.toDomain()!!
            }
        )
    }

    override fun getSiteState(): Flow<Result<AppConfig>> {
        return defaultExecutor.executeRequestImpl(
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
            },
            type = typeOf<SiteStateResponseDTO>().javaType
        )
    }

    override fun getPreorderDetails(productId: Long): Flow<Result<FormModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getPreOrderDetails(productId)
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun sendPreorder(productId: Long, queries: Map<String, String>): Flow<Result<String>> {
        return validationAwareExecutor.executeRequest(
            request = { vodovozService.sendPreorder(productId, queries) },
            mapper = { response -> response.messageOrEmpty },
        )
    }

    override fun getUnratedProductsDetails(): Flow<Result<UnratedProductsSectionModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getUnratedProductsDetails()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun removeUnratedProduct(productId: Long): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.removeUnratedProduct(productId)
            }
        )
    }

    override fun getFavoriteProducts(
        productsIds: String
    ): Flow<Result<ProductsSectionModel>> = executeCanEmptyRequest(
        request = {
            vodovozService.getFavoriteProducts(
                productsIds = productsIds.takeIf { accountManager.fetchAccountId() == null }
            )
        },
        mapper = { responseDTO ->
            responseDTO.data!!.toDomain()
        }
    )

    override fun getFavoriteProductsPaged(
        categoryId: Int,
        sort: SortModel,
        productsIds: String,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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
        return executeDefaultRequest(
            request = {
                vodovozService.getFavoriteProducts(productsIds = productsIds)
            },
            toDomain = { toDomain() }
        )
    }

    override suspend fun addProductToFavorites(productId: Long): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.addToFavorites(productId)
            },
            mapper = {
                it.messageOrEmpty
            }
        )
    }

    override suspend fun removeProductFromFavorites(productId: Long): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.removeFromFavorites(productId)
            },
            mapper = {
                it.messageOrEmpty
            }
        )
    }

    override suspend fun getBottomCart(): Flow<Result<BottomCartModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getBottomCart()
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override suspend fun getCartDetails(coupon: String?): Flow<Result<CartDetailsModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getCartDetails(coupon)
            },
            toDomain = { toDomain() },
        )
    }

    override fun getAdditionalProductsBS(
        productsId: Long,
        productsArticle: String,
    ): Flow<Result<AdditionalProductsBSModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getAdditionalProducts(
                    productsId = productsId,
                    productsArticle = productsArticle,

                    )
            },
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getAdditionalProductsPaged(
        productsId: Long,
        productsArticle: String,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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


    override suspend fun addProductToCart(
        productId: Long,
        quantity: Int
    ): Flow<Result<String>> = executeDefaultRequest(
        request = {
            vodovozService.addProductToCart(productId, quantity)
        }
    )

    override suspend fun addMultipleProductsToCart(
        productIdsWithQuantity: String
    ): Flow<Result<String>> = executeDefaultRequest(
        request = { vodovozService.addMultipleProductsToCart(productIdsWithQuantity) },
    )

    override suspend fun replaceMultipleBottlesToCart(cartProducts: CartProductsModel): Flow<Result<String>> {
        return executeDefaultRequest(
            request = {
                vodovozService.replaceMultipleBottlesToCart(
                    cartProducts.productsIdsWithQuantity
                )
            }
        )
    }

    override suspend fun removeProductFromCart(productId: Long): Flow<Result<String>> =
        executeDefaultRequest(
            request = { vodovozService.removeProductFromCart(productId) },
        )

    override suspend fun updateProductInCart(productId: Long, quantity: Int): Flow<Result<String>> =
        executeDefaultRequest(
            request = {
                vodovozService.updateProductInCart(productId, quantity)
            }
        )

    override suspend fun clearCart(): Flow<Result<String>> = executeDefaultRequest(
        request = { vodovozService.clearCart() }
    )

    override fun getProductAnalogs(
        productId: Long,
        sort: SortModel,
    ): Flow<Result<ProductsSectionModel>> = executeDefaultRequest(
        request = {
            vodovozService.getProductAnalogs(productId, sort.value, sort.order)
        },
        toDomain = {
            toDomain()
        }
    )

    override fun getWriteMessageDetails(): Flow<Result<FormModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getWriteMessageDetails()
            },
            toDomain = {
                toDomain()
            },
        )
    }

    override fun sendMessage(params: Map<String, String>): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.sendMessage(params)
            },
            toDomain = { toDomain() }
        )
    }

    override fun sendComment(
        productId: Long,
        rating: Int,
        message: String,
        imageBytesArray: List<ByteArray>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeDefaultRequest(
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
            toDomain = {
                toDomain()
            }
        )
    }

    override fun getProductCommentsInfo(productId: Long): Flow<Result<ProductCommentsInfoModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getComments(productId, 1)
            },
            toDomain = {
                toDomain()
            }
        )
    }


    override fun getProductCommentsPaged(
        productId: Long,
        sort: SortModel,
    ): Flow<PagingData<CommentModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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

    override fun getProductDetails(
        productId: Long
    ): Flow<Result<ProductDetailsScreenModel>> = executeDefaultRequest(
        request = {
            vodovozService.getProductDetails(
                productId = productId,

                )
        },
        toDomain = {
            toDomain()
        }
    )

    override fun getPresentInfo(): Flow<Result<PresentInfoModel>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getPresentInfo()
            },
            toDomain = { toDomain() }
        )
    }


    override fun getPopupWindowInfo(): Flow<Result<PopupWindowInfoModel>> = executeDefaultRequest(
        request = {
            vodovozService.getPopupWindowInfo()
        },
        toDomain = { toDomain() }
    )

    override fun getStories(): Flow<Result<List<StoryModel>>> = executeDefaultRequest(
        request = {
            vodovozService.getStories()
        },
        toDomain = { toDomain() }
    )

    override fun getBanners(): Flow<Result<List<BannerModel>>> = executeDefaultRequest(
        request = {
            vodovozService.getBanners()
        },
        toDomain = { mapToDomain() }
    )

    override fun getPromotions(): Flow<Result<PromotionsSectionModel>> = executeDefaultRequest(
        request = {
            vodovozService.getPromotions()
        },
        toDomain = {
            toDomain()
        }
    )

    override fun getPromotionDetails(promotionId: Int): Flow<Result<Pair<ProductsTitle, PromotionDetailsModel>>> =
        executeDefaultRequest(
            request = {
                vodovozService.getPromotionDetails(promotionId)
            },
            toDomain = {
                ProductsTitle(TOVAR?.NAMETOVAR ?: "") to AKCIYA?.toDomain()!!
            },
        )


    override fun getPromotionDetailsProductsPaged(
        promotionId: Int,
        limit: Int,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

            request = { page, limit ->
                vodovozService.getPromotionDetails(promotionId, page, limit)
            },
            mapper = { dto ->
                dto.TOVAR?.DATA?.mapToDomain() ?: emptyList()
            }
        )
    }


    override fun getAllPromotionsDetails(categoryId: Int): Flow<Result<PromotionsSectionModel>> =
        executeDefaultRequest(
            request = {
                vodovozService.getAllPromotions(
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            toDomain = {
                toDomain()
            },
        )

    override fun getAllPromotionsPaged(
        limit: Int,
        categoryId: Int?,
    ): Flow<PagingData<PromotionModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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


    override fun getOrderMenu(): Flow<Result<OrderWithMenuModel>> = executeDefaultRequest(
        request = {
            vodovozService.getOrderMenu()
        },
        toDomain = {
            toDomain()
        }
    )


    override fun getPopularCategories(): Flow<Result<SectionModel<PopularCategoryModel>>> =
        executeDefaultRequest(
            request = {
                vodovozService.getPopularCategories()
            },
            toDomain = {
                toDomain()
            }
        )

    override fun getNewProducts(): Flow<Result<SectionModel<ProductModel>>> = executeDefaultRequest(
        request = {
            vodovozService.getNewProducts()
        },
        mapper = { razdelDTO ->
            razdelDTO.data?.toDomain()!!
        }
    )

    override fun getAllNewProducts(categoryId: Int): Flow<Result<ProductsSectionModel>> =
        executeDefaultRequest(
            request = {
                vodovozService.getAllNewProducts(categoryId = categoryId)
            },
            toDomain = { toDomain() }
        )

    override fun getAllNewProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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


    override fun getHurryUpBuyProducts(
    ): Flow<Result<SectionModel<ProductModel>>> = executeDefaultRequest(
        request = {
            vodovozService.getHurryUpBuyProducts()
        },
        toDomain = {
            toDomain()
        }
    )

    override suspend fun getAllHurryUpBuyProducts(
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> =
        executeDefaultRequest(
            request = {
                vodovozService.getAllHurryUpBuyProducts(
                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            toDomain = {
                toDomain()
            }
        )

    override fun getAllHurryUpBuyProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = defaultExecutor,

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

    override fun getSuperTopCategories(): Flow<Result<SuperTopModel>> = executeDefaultRequest(
        request = {
            vodovozService.getSuperTopCategories()
        },
        toDomain = {
            toDomain()
        }
    )

    override fun getSuperTopProducts(categoryId: Long): Flow<Result<List<ProductModel>>> =
        executeDefaultRequest(
            request = { vodovozService.getSuperTopByCategory(categoryId) },
            toDomain = { mapToDomain() }
        )


    override fun getAllSuperTop(
        buttonId: Int,
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> = executeCanEmptyRequest(
        request = {
            vodovozService.getAllSuperTop(
                id = buttonId.toLong(),
                categoryId = categoryId.takeIf { it > -1 }
            )
        },
        toDomain = {
            toDomain()
        },
    )

    override fun getAllSuperTopPaged(
        buttonId: Int,
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
            }
        )
    }

    override fun getViewedProducts(): Flow<Result<SectionModel<ProductModel>>> {
        return executeDefaultRequest(
            request = {
                vodovozService.getViewedProducts()
            },
            toDomain = { toDomain() }
        )
    }

    override fun getAllViewedProducts(
        categoryId: Int,
    ): Flow<Result<ProductsSectionModel>> {
        return executeCanEmptyRequest(
            request = {
                vodovozService.getAllViewedProducts(

                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            toDomain = {
                toDomain()
            },
        )
    }

    override fun getAllViewedProductsPaged(
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return VodovozPagerFactory.getFlow(
            executor = canBeEmptyExecutor,

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
        )
    }
}