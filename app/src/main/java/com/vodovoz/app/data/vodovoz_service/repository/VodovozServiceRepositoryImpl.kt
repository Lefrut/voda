package com.vodovoz.app.data.vodovoz_service.repository

import androidx.core.text.HtmlCompat
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.AppConfig
import com.vodovoz.app.common.model.from
import com.vodovoz.app.core.network.retrofit.messageWithCode
import com.vodovoz.app.core.network.retrofit.prepareImageParts
import com.vodovoz.app.core.network.retrofit.stringBody
import com.vodovoz.app.core.network.retrofit.stringErrorBody
import com.vodovoz.app.core.network.serialization.fromJson
import com.vodovoz.app.data.vodovoz_service.VodovozService
import com.vodovoz.app.data.vodovoz_service.mappers.executeRequest
import com.vodovoz.app.data.vodovoz_service.mappers.mapToDomain
import com.vodovoz.app.data.vodovoz_service.mappers.toDomain
import com.vodovoz.app.data.vodovoz_service.model.BrandSectionDTO
import com.vodovoz.app.data.vodovoz_service.model.ProductCommentsDTO
import com.vodovoz.app.data.vodovoz_service.model.ProductsSectionDTO
import com.vodovoz.app.data.vodovoz_service.model.PromotionDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.PromotionsDTO
import com.vodovoz.app.data.vodovoz_service.model.VodovozErrorResponseDTO
import com.vodovoz.app.data.vodovoz_service.model.VodovozPlaceholderDTO
import com.vodovoz.app.data.vodovoz_service.model.VodovozResponseDTO
import com.vodovoz.app.data.vodovoz_service.model.WaitFeedbackProductsDTO
import com.vodovoz.app.data.vodovoz_service.model.order.OrderDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.order.OrdersHistoryDetailsDTO
import com.vodovoz.app.data.vodovoz_service.paging.VodovozPagingSource
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.domain.general.model.product.CatalogDetailsModel
import com.vodovoz.app.domain.general.model.exceptions.EmptyResultException
import com.vodovoz.app.domain.general.model.widgets.FieldModel
import com.vodovoz.app.domain.general.model.product.FilterValueModel
import com.vodovoz.app.domain.general.model.product.FiltersModel
import com.vodovoz.app.domain.general.model.location.MapZonesModel
import com.vodovoz.app.domain.general.model.order.OrderWithMenuModel
import com.vodovoz.app.domain.general.model.product.ParentCategoryModel
import com.vodovoz.app.domain.general.model.product.PopularCategoryModel
import com.vodovoz.app.domain.general.model.promotion.PresentInfoModel
import com.vodovoz.app.domain.general.model.product.ProductCommentsInfoModel
import com.vodovoz.app.domain.general.model.exceptions.RequestException
import com.vodovoz.app.domain.general.model.product.SearchRecommendationsModel
import com.vodovoz.app.domain.general.model.product.SortModel
import com.vodovoz.app.domain.general.model.exceptions.TooManyRequestsException
import com.vodovoz.app.domain.general.model.exceptions.UserBlockedException
import com.vodovoz.app.domain.general.model.exceptions.UserNotLoginException
import com.vodovoz.app.domain.general.model.exceptions.ValidationException
import com.vodovoz.app.domain.general.model.exceptions.VodovozPlaceholderModel
import com.vodovoz.app.domain.general.model.promotion.BrandModel
import com.vodovoz.app.domain.general.model.promotion.BrandSectionModel
import com.vodovoz.app.domain.general.model.cart.BottomCartModel
import com.vodovoz.app.domain.general.model.cart.CartDetailsModel
import com.vodovoz.app.domain.general.model.product.BuyCertificateDetailsModel
import com.vodovoz.app.domain.general.model.product.BuyCertificateModel
import com.vodovoz.app.domain.general.model.product.CertificateActivationDetailsModel
import com.vodovoz.app.domain.general.model.product.format
import com.vodovoz.app.domain.general.model.location.AddAddressDetailsModel
import com.vodovoz.app.domain.general.model.location.AddressModel
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import com.vodovoz.app.domain.general.model.order.CancelOrderDetailsModel
import com.vodovoz.app.domain.general.model.order.DeliveryDateDetailsModel
import com.vodovoz.app.domain.general.model.order.FormModel
import com.vodovoz.app.domain.general.model.order.OrderCallYouDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderingDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryItemModel
import com.vodovoz.app.domain.general.model.order.PaymentMethodDetailsModel
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
import com.vodovoz.app.domain.general.model.widgets.toQueries
import com.vodovoz.app.domain.general.model.product.toSliderQueries
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
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.net.URLEncoder
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

    override fun removeAddress(addressId: Int): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.deleteAddress(addressId, accountManager.fetchAccountId())
            },
            mapper = {
                it.data!!
            }
        )
    }

    override fun addAddress(
        address: MapAddressModel,
        params: Map<String, String>,
    ): Flow<Result<Long>> {
        return executeRequest(
            request = {
                val point = address.point
                vodovozService.addAddress(
                    userId = accountManager.fetchAccountId(),
                    geo = "${point.lat},${point.lon}",
                    city = address.city,
                    street = address.street,
                    fromMoscowToAddressKm = address.fromMoscowToPoint,
                    queries = params
                )
            },
            mapper = {
                it.data!!
            }
        )
    }

    override fun getAddAddressDetails(addressId: Long?): Flow<Result<AddAddressDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAddAddressDetails(
                    accountManager.fetchAccountId(),
                    addressId
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
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
                    "${point.lat} ${point.lon}"
                }

                vodovozService.updateAddress(
                    userId = accountManager.fetchAccountId(),
                    addressId = addressId,
                    geo = geo,
                    city = address?.city,
                    street = address?.street,
                    params = params
                )
            },
            mapper = {
                it.message ?: ""
            },
            onFail = { response ->

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
                    userId = accountManager.fetchAccountId(),
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
                    userId = accountManager.fetchAccountId(),
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
                    userId = accountManager.fetchAccountId()
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
                    userId = accountManager.fetchAccountId()
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun sendOrderRecipient(
        addressId: Long,
        fields: List<FieldModel>,
    ): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.sendOrderRecipient(
                    addressId = addressId,
                    userId = accountManager.fetchAccountId(),
                    params = fields.toQueries()
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
                vodovozService.getOrderCallYouDetails(addressId, accountManager.fetchAccountId())
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
    ): Flow<Result<OrderingDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderingDetails(
                    userId = accountManager.fetchAccountId(),
                    addressId = addressId,
                    date = date,
                    timeInterval = timeInterval
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
        phone: String,
        paymentMethodId: Long,
        callYouId: Long?,
        coupon: String?,
        balance: String?,
        deviceInfo: String?,
        notifyDriverId: String?,
        message: String?,
        params: Map<String, String>?,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.doOrder(
                    addressId = addressId,
                    userId = accountManager.fetchAccountId(),
                    deliveryDate = deliveryDate,
                    deliveryTimeInterval = deliveryTimeInterval,
                    phone = phone,
                    paymentMethodId = paymentMethodId,
                    callYouId = callYouId,
                    coupon = coupon,
                    balance = balance,
                    deviceInfo = URLEncoder.encode(deviceInfo, "UTF-8"),
                    queries = params
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }


    override fun orderService(
        serviceType: String,
        fields: List<FieldModel>,
    ): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.orderService(
                    accountManager.fetchAccountId(),
                    serviceType,
                    fields.toQueries()
                )
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getServiceOrderDetails(serviceType: String): Flow<Result<ServiceOrderDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getServiceOrderDetails(accountManager.fetchAccountId(), serviceType)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun removeFirebaseToken(token: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.removeFirebaseToken(accountManager.fetchAccountId(), token)
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override fun sendFirebaseToken(token: String): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.sendFirebaseToken(accountManager.fetchAccountId(), token)
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
                    userId = accountManager.fetchAccountId(),
                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = OrdersHistoryDetailsDTO::class,
                    request = { page, _ ->
                        vodovozService.getOrdersHistoryDetails(
                            userId = accountManager.fetchAccountId(),
                            page = page,
                            statuses = if (searchQuery.isNotBlank()) null else statuses,
                            search = searchQuery.takeIf { it.isNotBlank() }
                        )
                    },
                    mapper = { response ->
                        response.data?.toDomain()!!.items
                    }
                )
            }
        ).flow
    }

    override fun repeatOrder(orderId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.repeatOrder(orderId, accountManager.fetchAccountId())
            },
            mapper = { response ->
                response.data ?: ""
            }
        )
    }

    override fun getWaitFeedbackProductsTitle(): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.getWaitFeedbackProducts(accountManager.fetchAccountId())
            },
            mapper = {
                it.data?.products!!.isEmpty()

                it.data.title ?: ""
            },
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringErrorBody()
                    ).data!!.toDomain()

                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getWaitFeedbackProductsPaged(): Flow<PagingData<WaitFeedbackProductModel>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = WaitFeedbackProductsDTO::class,
                    request = { page, _ ->
                        vodovozService.getWaitFeedbackProducts(
                            accountManager.fetchAccountId(),
                            page
                        )
                    },
                    mapper = { response ->
                        response.data!!.products!!.mapToDomain()
                    }
                )
            }
        ).flow
    }

    override fun getNotificationSettingsDetails(): Flow<Result<NotificationSettingsDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getNotificationSettingsDetails(accountManager.fetchAccountId())
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
                    accountManager.fetchAccountId(),
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
            onFail = { it ->
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
            onFail = { it ->
                val json = it.stringErrorBody()
                val errorMessage = moshi.fromJson<VodovozResponseDTO<String>>(json).message ?: ""
                throw RequestException(errorMessage)
            }
        )
    }

    override fun getAllServicesDetails(): Flow<Result<AllServicesDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getAllServicesDetails(accountManager.fetchAccountId())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getServiceDetails(serviceId: Int): Flow<Result<ServiceDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getServiceDetails(accountManager.fetchAccountId(), serviceId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getQuestionnairesWelcomeDetails(): Flow<Result<QuestionnairesWelcomeDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getQuestionnairesWelcomeDetails(accountManager.fetchAccountId())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getQuestionnairesDetails(who: String): Flow<Result<QuestionnairesDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getQuestionnairesDetails(accountManager.fetchAccountId(), who)
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
                    userId = accountManager.fetchAccountId(),
                    answers = answers
                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
                val userId = accountManager.fetchAccountId()
                vodovozService.getCancelOrderDetails(userId, orderId)
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
                    userId = accountManager.fetchAccountId(),
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
                val userId = accountManager.fetchAccountId()
                vodovozService.sendOrderQuestion(userId, orderId, fields.toQueries())
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderQuestionDetails(orderId: Long): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.getOrderQuestionDetails(userId, orderId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun getOrderDetails(orderId: Long): Flow<Result<OrderDetailsModel>> {
        return executeRequest<VodovozResponseDTO<OrderDetailsDTO>, OrderDetailsModel>(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.getOrderDetails(userId, orderId)
            },
            mapper = { it ->
                it.data!!.toDomain()
            },
            onFail = { response ->
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
                    accountManager.fetchAccountId(),
                    orderId,
                    driverId
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
                vodovozService.getAddresses(accountManager.fetchAccountId())
            },
            mapper = {
                it.data!!.toDomain().ifEmpty {
                    throw IllegalArgumentException("Addresses can't be empty")
                }
            },
            onFail = { response ->
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
                    userId = accountManager.fetchAccountId(),
                    sort = sort.value,
                    order = sort.order,
                    categoryId = categoryId.takeIf { id -> id > 0 }
                )
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
        val accountId = accountManager.fetchAccountId()
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductsSectionDTO::class,
                    request = { page, _ ->
                        vodovozService.getPastPurchasesDetails(
                            userId = accountId,
                            page = page,
                            sort = sort.value,
                            order = sort.order,
                            categoryId = categoryId.takeIf { id -> id > -1 }
                        )
                    },
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain()
                            ?: throw IllegalArgumentException("Past purchases can't be null")
                    },
                    onFail = { response ->
                        val placeholder =
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                                response.stringErrorBody()
                            ).data!!.toDomain()
                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
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
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                throw EmptyResultException(placeholder = placeholder)
            }
        )
    }

    override fun getBrandsPaged(
        searchQuery: String,
    ): Flow<PagingData<BrandModel>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain()
                            ?: throw IllegalArgumentException("Brands can't be null")
                    },
                    onFail = { response ->
                        val placeholder =
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                                response.stringErrorBody()
                            ).data!!.toDomain()
                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
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
            onFail = { response ->
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
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain()
                            ?: throw IllegalArgumentException("Brand products can't be null")
                    },
                    onFail = { response ->
                        val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                            response.stringErrorBody()
                        ).data!!.toDomain()

                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
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
        categoryId: Int,
    ): Flow<PagingData<PromotionModel>> {
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = PromotionsDTO::class,
                    request = { page, _ ->
                        vodovozService.getBannerPromotions(
                            bannerId = bannerId,
                            blockId = blockId,
                            page = page,
                            categoryId = categoryId.takeIf { categoryId >= 0 }
                        )
                    },
                    mapper = { response ->
                        response.data!!.DATA!!.mapNotNull { it?.toDomain() }
                    },
                    onFail = { response ->
                        val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                            response.stringErrorBody()
                        ).data!!.toDomain()

                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow

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
            onFail = { response ->
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
        return Pager(
            config = PagingConfig(pageSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.DATA?.mapNotNull { product -> product.toDomain() }
                            ?: throw IllegalArgumentException("Banner products can't be null")
                    },
                    onFail = { response ->
                        val placeholder = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                            response.stringErrorBody()
                        ).data!!.toDomain()

                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
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
                vodovozService.updatePassword(accountManager.fetchAccountId() ?: -1, password)
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
                vodovozService.getChangePasswordDetails(accountManager.fetchAccountId() ?: -1)
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
                vodovozService.updateUserAvatar(accountManager.fetchAccountId() ?: -1, filePart)
            },
            mapper = {
                it.message ?: ""
            },
            onFail = {
                val info = it.stringErrorBody() ?: ""
                throw Exception(info)
            }
        )
    }

    override fun updateUserData(fields: List<FieldModel>): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.updateUserData(
                    accountManager.fetchAccountId() ?: -1,
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
                vodovozService.getUserData(accountManager.fetchAccountId() ?: -1)
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
                vodovozService.getProfileDetails(accountManager.fetchAccountId() ?: -1)
            },
            mapper = {
                it.data!!.toDomain()
            },
            onFail = { response ->
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
                vodovozService.getBonusesPopupWindow(accountManager.fetchAccountId())
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
                    accountManager.fetchAccountId(),
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
                vodovozService.buyCertificate(accountManager.fetchAccountId(), params)
            },
            mapper = { it.data!!.toDomain() }
        )
    }

    override fun getBuyCertificateDetails(): Flow<Result<BuyCertificateDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getBuyCertificateDetails(accountManager.fetchAccountId())
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
                    accountManager.fetchAccountId() ?: -1,
                    mapOf(field.id to field.value.trim())
                )
            },
            mapper = {
                it.message ?: ""
            },
            onFail = { response ->
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
                vodovozService.logout(accountManager.fetchAccountId())
            },
            mapper = {}
        )
    }

    override fun deleteAccount(): Flow<Result<Unit>> {
        return executeRequest(
            request = {
                vodovozService.deleteAccount(accountManager.fetchAccountId())
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
                vodovozService.relogin(id, token)
            },
            mapper = { response ->
                if (response.data == false || response.data == null) {
                    throw UserBlockedException(message = response.message ?: "")
                }
                response.data
            },
            onResponse = onResponse@{ response ->
                if (response.code() != 200) return@onResponse

                val cookies = response.headers().values("Set-Cookie")
                val sessionId = cookies.firstOrNull { s -> s.startsWith("PHPSESSID=") }
                cookieManager.updateCookieSessionId(sessionId)
            },
            onFail = { response ->
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
            onFail = { response ->
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
            onFail = { response ->
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
        val filtersAndValuesQuery = filters.filters.format()
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
            onFail = { response ->
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

        return Pager(
            config = PagingConfig(pageSize = 5, initialLoadSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain()
                            ?: throw IllegalArgumentException("Paged search products can't be null")
                    }
                )
            }
        ).flow
    }


    override fun getSearchProductsPaged(
        query: String,
        categoryId: Int,
        sort: SortModel,
    ): Flow<PagingData<ProductModel>> {
        return Pager(
            config = PagingConfig(pageSize = 5, initialLoadSize = 5),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.TOVAR?.mapToDomain()
                            ?: throw IllegalArgumentException("Paged search products can't be null")
                    },
                    onFail = { response ->
                        val placeholder =
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data
                        throw EmptyResultException(placeholder = placeholder?.toDomain())
                    }
                )
            }
        ).flow
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
            onFail = { response ->
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
            onFail = { response ->
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
            onFail = { response ->
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
                val siteState = response!!.toDomain()
                siteState
            },
            onFail = onFail@{ response ->
                val code = response.code()
                return@onFail when (code) {
                    402, 403 -> Result.success(AppConfig.Blocked)
                    else -> Result.failure(RequestException(response.messageWithCode()))
                }
            }
        )
    }

    override fun getPreorderDetails(productId: Long): Flow<Result<FormModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: -1L
                vodovozService.getPreOrderDetails(userId, productId)
            },
            mapper = {
                it.data?.toDomain() ?: throw IllegalArgumentException("PreorderDTO can't be null")
            }
        )
    }

    override fun sendPreorder(productId: Long, queries: Map<String, String>): Flow<Result<String>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.sendPreorder(userId, productId, queries)
            },
            mapper = { response -> response.message ?: "" },
            onFail = { response ->
                val jsonBody = response.stringErrorBody()
                val responseBody = moshi.fromJson<VodovozErrorResponseDTO>(jsonBody)
                Result.failure(ValidationException(responseBody.message ?: ""))
            }
        )
    }

    override fun getUnratedProductsDetails(): Flow<Result<UnratedProductsSectionModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.getUnratedProductsDetails(userId = userId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }

    override fun removeUnratedProduct(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                vodovozService.removeUnratedProduct(productId, accountManager.fetchAccountId())
            },
            mapper = {
                it.data ?: ""
            }
        )
    }

    override fun getFavoriteProducts(productsIds: String): Flow<Result<ProductsSectionModel>> =
        executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()

                vodovozService.getFavoriteProducts(
                    userId = userId,
                    productsIds = if (userId == null) productsIds else null
                )
            },
            mapper = { responseDTO ->
                responseDTO.data!!.toDomain()
            },
            onFail = { response ->
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
        return Pager(
            config = PagingConfig(pageSize = 4, initialLoadSize = 4, enablePlaceholders = false),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductsSectionDTO::class,
                    request = { page, _ ->
                        val userId = accountManager.fetchAccountId()
                        vodovozService.getFavoriteProducts(
                            userId = userId,
                            page = page,
                            categoryId = categoryId.takeIf { value -> value != -1 },
                            sort = sort.value,
                            order = sort.order,
                            productsIds = if (userId == null) productsIds else null
                        )
                    },
                    mapper = { response ->
                        response.data?.DATA?.mapNotNull { it.toDomain() }
                            ?: throw IllegalArgumentException("Paged favorite products can't be null")
                    }
                )
            }
        ).flow
    }

    override suspend fun addFavoriteProducts(productsIds: String): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: throw UserNotLoginException()
                vodovozService.getFavoriteProducts(userId = userId, productsIds = productsIds)
            },
            mapper = { response ->
                response.data?.toDomain()!!
            }
        )
    }

    override suspend fun addProductToFavorites(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: throw UserNotLoginException()
                vodovozService.addToFavorites(productId, userId)
            },
            mapper = {
                it.message ?: ""
            }
        )
    }

    override suspend fun removeProductFromFavorites(productId: Long): Flow<Result<String>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: throw UserNotLoginException()
                vodovozService.removeFromFavorites(productId, userId)
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
                val userId = accountManager.fetchAccountId()
                vodovozService.getCartDetails(userId, coupon)
            },
            mapper = { vodovozResponse ->
                vodovozResponse.data!!.toDomain()
            },
            onFail = { response ->

                val value = moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                    response.stringErrorBody()
                )
                throw EmptyResultException(placeholder = value.data!!.toDomain())
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
                vodovozService.getWriteMessageDetails(accountManager.fetchAccountId())
            },
            mapper = {
                it.data!!.toDomain()
            },
        )
    }

    override fun sendMessage(params: Map<String, String>): Flow<Result<VodovozPlaceholderModel>> {
        return executeRequest(
            request = {
                vodovozService.sendMessage(accountManager.fetchAccountId(), params)
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
                vodovozService.sendComment(
                    userId = accountManager.fetchAccountId(),
                    productId = productId,
                    rating = rating,
                    message = message,
                    images = imageBytesArray.prepareImageParts(
                        "comment_images"
                    )
                )
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
        return Pager(
            config = PagingConfig(pageSize = 10, initialLoadSize = 10),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductCommentsDTO::class,
                    request = { page, _ ->
                        vodovozService.getComments(
                            productId = productId,
                            page = page,
                            sort = sort.value,
                            order = sort.order
                        )
                    },
                    mapper = { response ->
                        response.data?.COMMENTS?.mapNotNull { it?.toDomain() } ?: emptyList()
                    }
                )
            }
        ).flow
    }

    override fun getProductDetails(productId: Long): Flow<Result<ProductDetailsScreenModel>> =
        executeRequest(
            request = {
                vodovozService.getProductDetails(productId)
            },
            mapper = { responseDto ->
                responseDto.data?.toDomain()
                    ?: throw IllegalArgumentException("ProductDetails cannot be null")
            }
        )

    override fun getPresentInfo(): Flow<Result<PresentInfoModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: throw UserNotLoginException()
                vodovozService.getPresentInfo(userId)
            },
            mapper = {
                it.data!!.toDomain()
            }
        )
    }


    override fun getPopupWindowInfo(): Flow<Result<PopupWindowInfoModel>> = executeRequest(
        request = {
            val userId = accountManager.fetchAccountId()
                ?: throw IllegalStateException("User is not authenticated")
            vodovozService.getPopupWindowInfo(userId)
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
        return Pager(
            config = PagingConfig(pageSize = limit, initialLoadSize = limit),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = PromotionDetailsDTO::class,
                    request = { page, limit ->
                        vodovozService.getPromotionDetails(promotionId, page, limit)
                    },
                    mapper = { promotionDetailsDTOVodovozResponseDTO ->
                        promotionDetailsDTOVodovozResponseDTO.data?.TOVAR?.DATA?.mapToDomain()
                            ?: emptyList()
                    },
                )
            }
        ).flow
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
        categoryId: Int,
    ): Flow<PagingData<PromotionModel>> {
        return Pager(
            config = PagingConfig(pageSize = limit, initialLoadSize = limit),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = PromotionsDTO::class,
                    request = { page, limit ->
                        vodovozService.getAllPromotions(
                            page = page,
                            limit = limit,
                            categoryId = categoryId.takeIf { categoryId >= 0 }
                        )
                    },
                    mapper = { promotionsDTOVodovozResponseDTO ->
                        promotionsDTOVodovozResponseDTO.data?.toDomain()?.promotions ?: emptyList()
                    },
                )
            }
        ).flow
    }


    override fun getOrderMenu(userId: Long?): Flow<Result<OrderWithMenuModel>> = executeRequest(
        request = {
            vodovozService.getOrderMenu(accountManager.fetchAccountId())
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
        return Pager(
            config = PagingConfig(5),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductsSectionDTO::class,
                    request = { page, _ ->
                        vodovozService.getAllNewProducts(
                            page = page,
                            categoryId = categoryId,
                            sort = sort.value,
                            order = sort.order
                        )
                    },
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain() ?: emptyList()
                    },
                )
            }
        ).flow
    }


    override fun getHurryUpBuyProducts(): Flow<Result<SectionModel<ProductModel>>> = executeRequest(
        request = {
            vodovozService.getHurryUpBuyProducts()
        },
        mapper = { razdelDTO ->
            razdelDTO.data?.toDomain()!!
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
        return Pager(
            config = PagingConfig(4, 4),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductsSectionDTO::class,
                    request = { page, _ ->
                        vodovozService.getAllHurryUpBuyProducts(
                            page = page,
                            categoryId = categoryId,
                            sort = sort.value,
                            order = sort.order
                        )
                    },
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain() ?: emptyList()
                    },
                )
            }
        ).flow
    }

    override fun getSuperTop(): Flow<Result<TopAndBottomSectionsModel>> = executeRequest(
        request = {
            vodovozService.getSuperTop()
        },
        mapper = { topAndBottomDTO ->
            topAndBottomDTO.data?.toDomain()
                ?: throw IllegalArgumentException("SuperTop can't be null")
        }
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
        onFail = { response ->
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
        return Pager(
            config = PagingConfig(4, 4),
            pagingSourceFactory = {
                VodovozPagingSource(
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
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain() ?: emptyList()
                    },
                    onFail = { response ->
                        val placeholder =
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
    }

    override fun getViewedProducts(): Flow<Result<SectionModel<ProductModel>>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: -1
                vodovozService.getViewedProducts(userId)
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
                val userId = accountManager.fetchAccountId()
                vodovozService.getAllViewedProducts(
                    userId = userId,
                    categoryId = categoryId.takeIf { it > -1 }
                )
            },
            mapper = {
                it.data?.toDomain()
                    ?: throw IllegalArgumentException("All Viewed products can't be null")
            },
            onFail = { response ->
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
        val userId = accountManager.fetchAccountId()
        return Pager(
            config = PagingConfig(4, 4),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = ProductsSectionDTO::class,
                    request = { page, _ ->
                        vodovozService.getAllViewedProducts(
                            page = page,
                            categoryId = categoryId.takeIf { it > 0 },
                            sort = sort.value,
                            order = sort.order,
                            userId = userId
                        )
                    },
                    mapper = { response ->
                        response.data?.DATA?.mapToDomain() ?: emptyList()
                    },
                    onFail = { response ->
                        val placeholder =
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringErrorBody()).data!!.toDomain()
                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
    }


}
