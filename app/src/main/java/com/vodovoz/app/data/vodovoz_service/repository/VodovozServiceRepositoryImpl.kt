package com.vodovoz.app.data.vodovoz_service.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.vodovoz.app.common.account.AccountManager
import com.vodovoz.app.common.cookie.CookieManager
import com.vodovoz.app.common.model.VodovozAddressType
import com.vodovoz.app.common.model.VodovozSiteState
import com.vodovoz.app.core.network.retrofit.messageWithCode
import com.vodovoz.app.core.network.retrofit.stringBody
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
import com.vodovoz.app.data.vodovoz_service.model.order_details.OrderDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.OrdersHistoryDetailsDTO
import com.vodovoz.app.data.vodovoz_service.paging.VodovozPagingSource
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.domain.general.model.CatalogDetailsModel
import com.vodovoz.app.domain.general.model.EmptyResultException
import com.vodovoz.app.domain.general.model.FieldModel
import com.vodovoz.app.domain.general.model.FilterValueModel
import com.vodovoz.app.domain.general.model.FiltersModel
import com.vodovoz.app.domain.general.model.OrderWithMenuModel
import com.vodovoz.app.domain.general.model.ParentCategoryModel
import com.vodovoz.app.domain.general.model.PopularCategoryModel
import com.vodovoz.app.domain.general.model.PresentInfoModel
import com.vodovoz.app.domain.general.model.ProductCommentsInfoModel
import com.vodovoz.app.domain.general.model.RequestException
import com.vodovoz.app.domain.general.model.SearchRecommendationsModel
import com.vodovoz.app.domain.general.model.SortModel
import com.vodovoz.app.domain.general.model.TooManyRequestsException
import com.vodovoz.app.domain.general.model.UserBlockedException
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.model.ValidationException
import com.vodovoz.app.domain.general.model.VodovozPlaceholderModel
import com.vodovoz.app.domain.general.model.brand.BrandModel
import com.vodovoz.app.domain.general.model.brand.BrandSectionModel
import com.vodovoz.app.domain.general.model.cart.BottomCartModel
import com.vodovoz.app.domain.general.model.cart.CartDetailsModel
import com.vodovoz.app.domain.general.model.certificate.BuyCertificateDetailsModel
import com.vodovoz.app.domain.general.model.certificate.BuyCertificateModel
import com.vodovoz.app.domain.general.model.certificate.CertificateActivationDetailsModel
import com.vodovoz.app.domain.general.model.format
import com.vodovoz.app.domain.general.model.location.AddressDetailsModel
import com.vodovoz.app.domain.general.model.location.AddressModel
import com.vodovoz.app.domain.general.model.location.MapAddressModel
import com.vodovoz.app.domain.general.model.order.CancelOrderDetailsModel
import com.vodovoz.app.domain.general.model.order.DeliveryDateDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderQuestionDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderingDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryItemModel
import com.vodovoz.app.domain.general.model.order.PaymentMethodDetailsModel
import com.vodovoz.app.domain.general.model.order.PreOrderSectionModel
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
import com.vodovoz.app.domain.general.model.toQueries
import com.vodovoz.app.domain.general.model.user.AuthDetailsModel
import com.vodovoz.app.domain.general.model.user.ChangePasswordDetailsModel
import com.vodovoz.app.domain.general.model.user.NotificationSettingsDetailsModel
import com.vodovoz.app.domain.general.model.user.ProfileDetailsModel
import com.vodovoz.app.domain.general.model.user.QuestionnairesDetailsModel
import com.vodovoz.app.domain.general.model.user.QuestionnairesWelcomeDetailsModel
import com.vodovoz.app.domain.general.model.user.RequestCodeModel
import com.vodovoz.app.domain.general.model.user.UserAuthInfoModel
import com.vodovoz.app.domain.general.model.user.UserDataModel
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.util.extensions.singleResult
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
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

    override fun addAddress(address: MapAddressModel): Flow<Result<Long>> {
        return executeRequest(
            request = {
                val point = address.point
                vodovozService.addAddress(
                    userId = accountManager.fetchAccountId(),
                    address = address.name,
                    type = VodovozAddressType.Personal.value,
                    geo = "${point.lat},${point.lon}",
                    city = address.city,
                    street = address.street,
                    house = address.house,
                )
            },
            mapper = {
                it.data!!
            }
        )
    }

    override fun updateAddress(address: AddressDetailsModel): Flow<Result<Long>> {
        return executeRequest(
            request = {
                vodovozService.updateAddress(
                    userId = accountManager.fetchAccountId(),
                    addressId = address.id,
                    address = address.name,
                    type = VodovozAddressType.Personal.value,
                    geo = "${address.lat},${address.lon}",
                    city = address.city,
                    street = address.street,
                    house = address.house,
                    intercom = address.intercom,
                    entrance = address.entrance,
                    flat = address.flat,
                    floor = address.floor,
                    needPass = address.needPass.value
                )
            },
            mapper = {
                it.data!!
            }
        )
    }

    override fun getPaymentMethodDetails(
        addressId: Int,
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
        addressId: Int,
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

    override fun getOrderingDetails(): Flow<Result<OrderingDetailsModel>> {
        return executeRequest(
            request = {
                vodovozService.getOrderingDetails(accountManager.fetchAccountId())
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
                        response.stringBody()
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
                        response.stringBody()
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
                    moshi.fromJson<VodovozResponseDTO<String>>(it.stringBody()).message ?: ""

                throw RequestException(errorMessage)
            }
        )
    }

    override fun requestPhoneCode(url: String, phone: String): Flow<Result<RequestCodeModel>> {
        return executeRequest(
            request = {
                vodovozService.requestPhoneCode(url, phone)
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
                val json = it.stringBody()
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
                    moshi.fromJson<VodovozResponseDTO<String>>(response.stringBody()).data ?: ""

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

    override fun getOrderQuestionDetails(orderId: Long): Flow<Result<OrderQuestionDetailsModel>> {
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
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
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
                val body = response.stringBody()
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(body).data!!
                throw EmptyResultException(placeholder = placeholder.toDomain())
            }
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
                        response.stringBody()
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
                                response.stringBody()
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
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
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
                                response.stringBody()
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
                    response.stringBody()
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
                            response.stringBody()
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
                    response.stringBody()
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
                            response.stringBody()
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

    override fun updatePassword(password: String): Flow<Result<Unit>> {
        return executeRequest(
            request = {
                vodovozService.updatePassword(accountManager.fetchAccountId() ?: -1, password)
            },
            mapper = {},
            onFail = { response ->
                throw try {
                    val message =
                        moshi.fromJson<VodovozErrorResponseDTO>(response.stringBody()).message
                    ValidationException(message = message ?: "")
                } catch (_: Throwable) {
                    val placeholder =
                        moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data
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
                val filePart =
                    MultipartBody.Part.createFormData("userpic", avatarFile.name, requestBody)
                vodovozService.updateUserAvatar(accountManager.fetchAccountId() ?: -1, filePart)
            },
            mapper = {
                it.message ?: ""
            },
            onFail = {
                val info = it.stringBody() ?: ""
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
                    response.stringBody()
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
                    response.stringBody()
                ).data

                Result.failure(UserNotLoginException(placeholder = errorData!!.toDomain()))
            }
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
        ).map { result ->
            //todo - delete after back fix
            result.mapCatching { filtersModel ->
                val updatedFilters = coroutineScope {
                    filtersModel.filters.map { filter ->
                        async {
                            if (filter.values.isEmpty()) {
                                val values = getFilterValues(categoryId, filter.id)
                                    .singleResult()
                                    .getOrNull() ?: emptyList()
                                filter.copy(
                                    values = values.take(6),
                                    totalValues = values.size
                                )
                            } else {
                                filter
                            }
                        }
                    }.awaitAll()
                }

                filtersModel.copy(
                    filters = updatedFilters
                )
            }
        }
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
                it.data!!.map { filterValue -> FilterValueModel(filterValue, filterValue) }
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
                    response.stringBody(),
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
            mapper = {
                it.data!!
            },
            onResponse = onResponse@{ response ->
                if (response.code() != 200) return@onResponse

                val cookies = response.headers().values("Set-Cookie")
                val sessionId = cookies.firstOrNull { s -> s.startsWith("PHPSESSID=") }
                cookieManager.updateCookieSessionId(sessionId)
            },
            onFail = { response ->
                val code = response.code()

                when (code) {
                    402, 404 -> {
                        throw UserBlockedException(message = response.messageWithCode())
                    }

                    else -> {
                        throw RequestException()
                    }
                }
            }
        )
    }

    override fun register(fields: List<FieldModel>): Flow<Result<UserAuthInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.register(fields.toQueries())
            },
            mapper = { registerDTO ->
                registerDTO.data!!.toDomain()
            },
            onFail = { response ->
                val jsonBody = response.stringBody()

                throw when (response.code()) {
                    404 -> {
                        val message = moshi.fromJson<VodovozResponseDTO<String>>(
                            json = jsonBody
                        ).data ?: ""
                        ValidationException(message = message)
                    }

                    else -> {
                        RequestException(response.messageWithCode())
                    }
                }
            }
        )
    }

    override fun loginByEmail(fields: List<FieldModel>): Flow<Result<UserAuthInfoModel>> {
        return executeRequest(
            request = {
                vodovozService.loginByEmail(fields.toQueries())
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            onFail = { response ->
                val jsonBody = response.stringBody()
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

        return executeRequest(
            request = {
                vodovozService.getCategoryProducts(
                    categoryId = categoryId,
                    filters = filtersQuery.takeIf { s -> s.isNotBlank() },
                    filtersAndValues = filtersAndValuesQuery.takeIf { s -> s.isNotBlank() },
                    priceTo = filters.priceRange.last.toFloat(),
                    priceFrom = filters.priceRange.first.toFloat()
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(
                        response.stringBody()
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
                            priceFrom = filters.priceRange.first.toFloat()
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
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data
                        throw EmptyResultException(placeholder = placeholder?.toDomain())
                    }
                )
            }
        ).flow
    }

    override fun getSearchProducts(query: String): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                vodovozService.getSearchProducts(query = query)
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data
                throw EmptyResultException(placeholder = placeholder?.toDomain())
            }
        )
    }

    override fun getBarCodeProducts(barCode: String): Flow<Result<List<ProductModel>>> {
        return executeRequest(
            request = {
                vodovozService.getSearchProducts(query = barCode, isCamera = "Y")
            },
            mapper = {
                it.data!!.TOVAR!!.mapToDomain()
            },
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data
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
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody())
                throw EmptyResultException(placeholder = value.data!!.toDomain())
            }
        )
    }

    override fun getSiteState(): Flow<Result<VodovozSiteState>> {
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
                    402 -> Result.success(VodovozSiteState.Blocked)
                    else -> Result.failure(RequestException(response.messageWithCode()))
                }
            }
        )
    }

    override fun getPreorderFields(productId: Long): Flow<Result<PreOrderSectionModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: -1L
                vodovozService.getPreOrderFields(userId, productId)
            },
            mapper = {
                it.data?.toDomain() ?: throw IllegalArgumentException("PreorderDTO can't be null")
            }
        )
    }

    override fun sendPreorder(productId: Long, fields: List<FieldModel>): Flow<Result<String>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.sendPreorder(userId, productId, fields.toQueries())
            },
            mapper = { response -> response.message ?: "" },
            onFail = { response ->
                val jsonBody = response.stringBody()
                val responseBody = moshi.fromJson<VodovozErrorResponseDTO>(jsonBody)
                Result.failure(ValidationException(responseBody.message ?: ""))
            }
        )
    }

    override fun getUnratedProductsDetails(): Flow<Result<UnratedProductsSectionModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId() ?: throw UserNotLoginException()
                vodovozService.getUnratedProductsDetails(userId)
            },
            mapper = {
                it.data!!.toDomain()
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
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody())
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
                val value =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody())
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


    override fun getPromotionsWithSections(categoryId: Int): Flow<Result<PromotionsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getPromotionsWithSections(
                    categoryId = categoryId.takeIf { categoryId >= 0 }
                )
            },
            mapper = { response ->
                response.data!!.toDomain()
            },
        )

    override fun getPromotionsPaged(limit: Int, categoryId: Int): Flow<PagingData<PromotionModel>> {
        return Pager(
            config = PagingConfig(pageSize = limit, initialLoadSize = limit),
            pagingSourceFactory = {
                VodovozPagingSource(
                    clazz = PromotionsDTO::class,
                    request = { page, limit ->
                        vodovozService.getPromotionsWithSections(
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

    override fun getAllNewProducts(): Flow<Result<ProductsSectionModel>> = executeRequest(
        request = {
            vodovozService.getAllNewProducts()
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

    override suspend fun getAllHurryUpBuyProducts(): Flow<Result<ProductsSectionModel>> =
        executeRequest(
            request = {
                vodovozService.getAllHurryUpBuyProducts()
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


    override fun getAllSuperTop(buttonId: Int): Flow<Result<ProductsSectionModel>> = executeRequest(
        request = {
            vodovozService.getAllSuperTop(buttonId.toLong())
        },
        mapper = { superTopResponse ->
            superTopResponse.data!!.toDomain()
        },
        onFail = { response ->
            val placeholder =
                moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
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
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
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

    override fun getAllViewedProducts(): Flow<Result<ProductsSectionModel>> {
        return executeRequest(
            request = {
                val userId = accountManager.fetchAccountId()
                vodovozService.getAllViewedProducts(userId)
            },
            mapper = {
                it.data?.toDomain()
                    ?: throw IllegalArgumentException("All Viewed products can't be null")
            },
            onFail = { response ->
                val placeholder =
                    moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
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
                            moshi.fromJson<VodovozResponseDTO<VodovozPlaceholderDTO>>(response.stringBody()).data!!.toDomain()
                        throw EmptyResultException(placeholder = placeholder)
                    }
                )
            }
        ).flow
    }


}
