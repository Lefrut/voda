package com.vodovoz.app.data.vodovoz_service.mappers

import com.vodovoz.app.common.model.VodovozBoolean
import com.vodovoz.app.common.model.equalsTo
import com.vodovoz.app.data.vodovoz_service.di.toVodovozUrl
import com.vodovoz.app.data.vodovoz_service.model.CancelOrderDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.vodovoz.app.data.vodovoz_service.model.OPLATA_DTO
import com.vodovoz.app.data.vodovoz_service.model.OrderPlaceholderDTO
import com.vodovoz.app.data.vodovoz_service.model.OrderQuestionDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.cart.BottomCartDTO
import com.vodovoz.app.data.vodovoz_service.model.delivery_date.DATE_INTERVALS_DTO
import com.vodovoz.app.data.vodovoz_service.model.delivery_date.DATE_INTERVAL_DTO
import com.vodovoz.app.data.vodovoz_service.model.delivery_date.DELIVERY_DATE_DTO
import com.vodovoz.app.data.vodovoz_service.model.delivery_date.DeliveryDateDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ABOUT_ORDER_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ABOUT_ORDER_OKNO_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ORDER_DETAILS_ITOG_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ORDER_DETAILS_KNOPKA_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ORDER_DETAILS_TOVAR_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ORDER_PRODUCT_PODAROK_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.ORDER_STATUS_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_details.OrderDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.FILTER_STATYS_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.ORDERS_HISTORY_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.ORDERS_HISTORY_KNOPKA_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.ORDERS_HISTORY_PRODUCT_DTO
import com.vodovoz.app.data.vodovoz_service.model.order_history.OrdersHistoryDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.CallYouItemDTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_OPLATA_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_OPLATA_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_POLYSHATEL_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_POLYSHATEL_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_PREDYP_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.ORDER_PREDYP_ITEM_DTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.OrderCallYouDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.OrderingDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.ordering.RecipientDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.payment_method.PaymentMethodDetailsDTO
import com.vodovoz.app.data.vodovoz_service.model.payment_method.PaymentMethodItemDTO
import com.vodovoz.app.data.vodovoz_service.model.payment_method.PaymentMethodSectionDTO
import com.vodovoz.app.domain.general.model.PaymentInfoModel
import com.vodovoz.app.domain.general.model.VodovozPlaceholderModel
import com.vodovoz.app.domain.general.model.cart.BottomCartModel
import com.vodovoz.app.domain.general.model.certificate.BuyCertificateModel
import com.vodovoz.app.domain.general.model.order.AboutOrderItemModel
import com.vodovoz.app.domain.general.model.order.AboutOrderPopupWindowModel
import com.vodovoz.app.domain.general.model.order.CallYouItemModel
import com.vodovoz.app.domain.general.model.order.CancelOrderDetailsModel
import com.vodovoz.app.domain.general.model.order.DeliveryDateDetailsModel
import com.vodovoz.app.domain.general.model.order.DeliveryDateOptionModel
import com.vodovoz.app.domain.general.model.order.DeliveryTimeIntervalModel
import com.vodovoz.app.domain.general.model.order.OrderCallYouDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsButtonModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderDetailsSummaryModel
import com.vodovoz.app.domain.general.model.order.OrderFilterModel
import com.vodovoz.app.domain.general.model.order.OrderNotifyItemModel
import com.vodovoz.app.domain.general.model.order.OrderPaymentItemModel
import com.vodovoz.app.domain.general.model.order.OrderProductModel
import com.vodovoz.app.domain.general.model.order.OrderProductPresentModel
import com.vodovoz.app.domain.general.model.order.OrderQuestionDetailsModel
import com.vodovoz.app.domain.general.model.order.OrderRecipientItemModel
import com.vodovoz.app.domain.general.model.order.OrderStatusModel
import com.vodovoz.app.domain.general.model.order.OrderingDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryButtonModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryDetailsModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryItemModel
import com.vodovoz.app.domain.general.model.order.OrdersHistoryProductModel
import com.vodovoz.app.domain.general.model.order.PaymentMethodDetailsModel
import com.vodovoz.app.domain.general.model.order.PaymentMethodItemModel
import com.vodovoz.app.domain.general.model.order.RecipientDetailsModel
import com.vodovoz.app.domain.general.model.product.SectionModel
import com.vodovoz.app.domain.general.model.promotion.ColorfulButtonModel
import kotlin.math.roundToInt

fun OrderCallYouDetailsDTO.toDomain(): OrderCallYouDetailsModel {

    val callYouItems = (DANNYE?.mapNotNull { it.toDomain() } ?: emptyList()).ifEmpty {
        throw IllegalArgumentException("OrderCallYouDetails items can't be empty or null")
    }

    return OrderCallYouDetailsModel(
        title = TITLE ?: "",
        items = callYouItems,
        currentItem = callYouItems.first(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("OrderCallYouDetails button can't be empty or null")
    )
}

fun CallYouItemDTO.toDomain(): CallYouItemModel? {
    return CallYouItemModel(
        name = NAME ?: return null,
        description = OPISANIE ?: "",
        value = VALUE ?: return null,
        code = CODE ?: return null
    )
}

fun RecipientDetailsDTO.toDomain(): RecipientDetailsModel {
    return RecipientDetailsModel(
        title = TITLE ?: "",
        fields = POLYA?.mapToDomain()
            ?: throw IllegalArgumentException("RecipientDetails fields can't be null"),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("RecipientDetails button can't be null")
    )
}

fun PaymentMethodDetailsDTO.toDomain(): PaymentMethodDetailsModel {
    return PaymentMethodDetailsModel(
        title = TITLE ?: "",
        items = DANNYE?.mapToDomain()
            ?: throw IllegalArgumentException("Payment method items can't be null"),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("Payment method button can't be null")
    )
}


@JvmName("mapToPaymentMethodItemModelSectionModelList")
fun List<PaymentMethodSectionDTO>.mapToDomain(): List<SectionModel<PaymentMethodItemModel>> {
    return map { it.toDomain() }
}


fun PaymentMethodSectionDTO.toDomain(): SectionModel<PaymentMethodItemModel> {
    return SectionModel(
        title = ZAGALOVOK ?: "",
        items = OPLATA?.mapToDomain() ?: emptyList(),
        button = null
    )
}

@JvmName("mapToPaymentMethodItemModelList")
fun List<PaymentMethodItemDTO>.mapToDomain(): List<PaymentMethodItemModel> {
    return map { it.toDomain() }
}


fun PaymentMethodItemDTO.toDomain(): PaymentMethodItemModel {
    return PaymentMethodItemModel(
        title = NAME ?: "",
        image = KARTINKA?.toVodovozUrl() ?: "",
        code = CODE ?: "",
        id = ID ?: "",
        field = POLE?.toDomain()
    )
}


fun DeliveryDateDetailsDTO.toDomain(): DeliveryDateDetailsModel {
    return DeliveryDateDetailsModel(
        title = TITLE ?: "",
        options = DATE?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("DeliveryDate button can't be null"),
        timeSections = INTERVAL?.mapToDomain()
            ?: throw IllegalArgumentException("DeliveryDate intervals can't be null")
    )
}


@JvmName("mapToDeliveryTimeIntervalModelSectionModelList")
fun List<DATE_INTERVALS_DTO>.mapToDomain(): List<SectionModel<DeliveryTimeIntervalModel>> {
    return mapNotNull { it.toDomain() }
}

fun DATE_INTERVALS_DTO.toDomain(): SectionModel<DeliveryTimeIntervalModel>? {
    return SectionModel(
        title = NAME ?: return null,
        items = INTERVAL?.mapToDomain() ?: emptyList(),
        placeholder = ERROR?.toDomain()
    )
}

@JvmName("mapToDeliveryTimeIntervalModelList")
fun List<DATE_INTERVAL_DTO>.mapToDomain(): List<DeliveryTimeIntervalModel> {
    return mapNotNull { it.toDomain() }
}

fun DATE_INTERVAL_DTO.toDomain(): DeliveryTimeIntervalModel? {
    return DeliveryTimeIntervalModel(
        name = NAME ?: return null,
        value = VALUE ?: return null,
        code = CODE ?: return null,
        blocked = VodovozBoolean.True equalsTo BLOCK,
        priceText = MONEY ?: ""
    )
}


@JvmName("mapToDeliveryDateOptionModelList")
fun List<DELIVERY_DATE_DTO>.mapToDomain(): List<DeliveryDateOptionModel> {
    return mapNotNull { it.toDomain() }
}

fun DELIVERY_DATE_DTO.toDomain(): DeliveryDateOptionModel? {
    return DeliveryDateOptionModel(
        code = CODE ?: return null,
        name = NAME ?: return null,
        value = VALUE ?: return null
    )
}

fun OrderingDetailsDTO.toDomain(): OrderingDetailsModel {
    return OrderingDetailsModel(
        title = TITLE ?: "",
        commentField = KOMMENT?.KOMMENTARY?.toDomain(),
        recipientSection = POLYSHATEL?.toDomain()
            ?: throw IllegalArgumentException("Ordering recipient can't be null"),
        notifySection = KOMMENT?.PREDYP?.toDomain() ?: SectionModel.empty(),
        paymentSection = OPLATA?.toDomain()
            ?: throw IllegalArgumentException("Ordering payment can't be null"),
        totals = ITOG?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("Ordering button can't be null")
    )
}


fun ORDER_OPLATA_DTO.toDomain(): SectionModel<OrderPaymentItemModel> {
    return SectionModel(
        title = ZAGOLOVOK ?: "",
        items = DANNYE?.mapToDomain() ?: emptyList(),
        button = null
    )
}

@JvmName("OrderPaymentItemModelList")
fun List<ORDER_OPLATA_ITEM_DTO>.mapToDomain(): List<OrderPaymentItemModel> {
    return mapNotNull { it -> it.toDomain() }
}


fun ORDER_OPLATA_ITEM_DTO.toDomain(): OrderPaymentItemModel {
    return OrderPaymentItemModel(
        image = KARTINKA?.toVodovozUrl() ?: "",
        name = NAME ?: "",
        description = OPISANIE ?: "",
        id = ID ?: ""
    )
}


fun ORDER_POLYSHATEL_DTO.toDomain(): SectionModel<OrderRecipientItemModel> {
    return SectionModel(
        title = ZAGOLOVOK ?: "",
        items = DANNYE?.mapToDomain() ?: emptyList(),
        button = null
    )
}

@JvmName("mapToOrderRecipientItemModelList")
fun List<ORDER_POLYSHATEL_ITEM_DTO>.mapToDomain(): List<OrderRecipientItemModel> {
    return mapNotNull { it.toDomain() }
}

fun ORDER_POLYSHATEL_ITEM_DTO.toDomain(): OrderRecipientItemModel? {
    return OrderRecipientItemModel(
        image = KARTINKA?.toVodovozUrl() ?: "",
        name = NAME ?: "",
        description = OPISANIE ?: "",
        id = ID ?: return null
    )
}

fun ORDER_PREDYP_DTO.toDomain(): SectionModel<OrderNotifyItemModel> {
    return SectionModel(
        title = NAME ?: "",
        items = DANNYE?.mapToDomain() ?: emptyList(),
        button = null
    )
}

@JvmName("mapToOrderNotifyItemModel")
fun List<ORDER_PREDYP_ITEM_DTO>.mapToDomain(): List<OrderNotifyItemModel> {
    return mapNotNull { it.toDomain() }
}

fun ORDER_PREDYP_ITEM_DTO.toDomain(): OrderNotifyItemModel? {
    return OrderNotifyItemModel(
        name = NAME ?: return null,
        value = VALUE ?: return null,
        code = CODE ?: return null
    )
}

fun BottomCartDTO.toDomain(): BottomCartModel {
    return BottomCartModel(
        total = ALLSUMA?.roundToInt() ?: 0,
        count = TOVAROV ?: 0
    )
}

fun OrderPlaceholderDTO.toDomain(): BuyCertificateModel {

    return BuyCertificateModel(
        placeholder = toVodovozPlaceholder(),
        payment = button?.oplate?.toDomain()
            ?: throw IllegalArgumentException("Payment cannot be null")
    )
}

fun OPLATA_DTO.toDomain(): PaymentInfoModel {
    return PaymentInfoModel(
        id = ID ?: throw IllegalArgumentException("Payment ID cannot be null"),
        name = NAME ?: "",
        browser = VodovozBoolean.True equalsTo BRAYZER,
        url = URL ?: throw IllegalArgumentException("Payment URL cannot be null")
    )
}


fun OrderPlaceholderDTO.toVodovozPlaceholder(): VodovozPlaceholderModel {

    return VodovozPlaceholderModel(
        title = title ?: "",
        headerHtml = header ?: "",
        descriptionHtml = message ?: "",
        imageUrl = imageUrl?.toVodovozUrl() ?: "",
        button = ColorfulButtonModel(
            name = button?.text ?: "",
            backgroundColor = button?.background ?: "",
            textColor = button?.color ?: "",
        )
    )
}

fun OrderDetailsDTO.toDomain(): OrderDetailsModel {
    return OrderDetailsModel(
        title = TITLE?.ZAGOLOVOK ?: "",
        subtitle = TITLE?.OPIS ?: "",
        currentStatus = BLOCK?.STATUS?.mapToDomain() ?: emptyList(),
        header = BLOCK?.GLAV ?: "",
        statuses = BLOCK?.STATUSY?.mapToDomain() ?: emptyList(),
        topButtons = BLOCK?.KNOPKI?.mapToDomain() ?: emptyList(),
        products = TOVARY?.TOVAR?.mapToDomain() ?: emptyList(),
        productsTitle = TOVARY?.TITLE ?: "",
        bottomButtons = KNOPKI_NIZ?.mapToDomain() ?: emptyList(),
        orderSummary = ITOG?.toDomain()
            ?: throw IllegalArgumentException("Order details summary can't be null")
    )
}

fun OrderQuestionDetailsDTO.toDomain(): OrderQuestionDetailsModel {
    return OrderQuestionDetailsModel(
        title = TITLE ?: "",
        description = INFORMIROVANIE ?: "",
        fields = LISTADATA?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("Order question details can't be null")
    )
}

fun KNOPKA_ORDER_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = NAME ?: TEXT ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLOR ?: "",
        id = ID ?: ""
    )
}

fun CancelOrderDetailsDTO.toDomain(): CancelOrderDetailsModel {
    return CancelOrderDetailsModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        warningText = DOPOPISANIE ?: "",
        checkboxesNames = STATYS?.ZNACHWNIYA?.mapNotNull { it.VALUE } ?: emptyList(),
        checkboxesGroupId = STATYS?.ZNACHWNIYA?.firstOrNull { it.GROUP_ID != null }?.GROUP_ID
            ?: "statys",
        field = SOOBSHENIE?.toDomain(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("CancelOrderDetails button can't be null")
    )
}

fun ORDER_DETAILS_ITOG_DTO.toDomain(): OrderDetailsSummaryModel {
    return OrderDetailsSummaryModel(
        finalPriceText = finalPrice ?: "",
        productsPriceText = productsPrice ?: "",
        depositText = deposit ?: "",
        deliveryText = delivery ?: "",
        parkingText = parking ?: "",
    )
}

@JvmName("mapToOrderProductModelList")
fun List<ORDER_DETAILS_TOVAR_DTO>.mapToDomain(): List<OrderProductModel> {
    return mapNotNull { it.toDomain() }
}

fun ORDER_DETAILS_TOVAR_DTO.toDomain(): OrderProductModel? {
    return OrderProductModel(
        id = ID ?: return null,
        name = NAME ?: "",
        quantity = QUANTITY?.toDoubleOrNull()?.toInt() ?: 1,
        articleNumberText = CML2_ARTICLE ?: "",
        depositText = PROPERTY_ZALOG_VALUE,
        price = EXTENDED_PRICE?.minByOrNull { (it?.PRICE ?: Int.MAX_VALUE) }?.toDomain(),
        isShowcaseProduct = URL == true,
        image = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        labels = NALICHIE_MORE?.mapToDomain() ?: emptyList(),
        present = PODAROK?.toDomain(),
        pricePerUnit = PROPERTY_TSENA_ZA_EDINITSU_TOVARA_VALUE.takeIf { (it ?: 0) > 0 },
        unitOfMeasurement = EDINICAIZMERENIYA,
        catalogQuantity = CATALOG_QUANTITY ?: 0,
        isFavorite = FAVORITE ?: false,
        restrictionCode = ZAPRET_FISHKAM ?: 0
    )
}

fun ORDER_PRODUCT_PODAROK_DTO.toDomain(): OrderProductPresentModel? {
    return OrderProductPresentModel(
        title = TITLE ?: return null,
        color = COLOR ?: ""
    )
}

@JvmName("mapToOrderDetailsButtonModelList")
fun List<ORDER_DETAILS_KNOPKA_DTO>.mapToDomain(): List<OrderDetailsButtonModel> {
    return mapNotNull { it.toDomain() }
}

fun ORDER_DETAILS_KNOPKA_DTO.toDomain(): OrderDetailsButtonModel {
    return OrderDetailsButtonModel(
        name = NAME ?: "",
        description = OPIS ?: "",
        id = ID ?: "",
        image = IMAGE?.toVodovozUrl() ?: "",
        popupWindow = OKNO?.toDomain(),
        backgroundColor = COLOR_BACKGROUND ?: "",
        textColor = COLOR_TEXT ?: "",
        url = URL?.toVodovozUrl() ?: "",
        browser = (BRAYZER == "Y").takeIf { useBrowser -> useBrowser },
        driverId = VODITEL
    )
}

fun ABOUT_ORDER_OKNO_DTO.toDomain(): AboutOrderPopupWindowModel {
    return AboutOrderPopupWindowModel(
        title = TITLE ?: "",
        items = DANNYE?.map { it.toDomain() } ?: emptyList()
    )
}

fun ABOUT_ORDER_ITEM_DTO.toDomain(): AboutOrderItemModel {
    return AboutOrderItemModel(
        image = IMAGE?.toVodovozUrl() ?: "",
        name = NAME ?: "",
        description = OPIS ?: ""
    )
}

@JvmName("mapToOrderStatusModelList")
fun List<ORDER_STATUS_DTO>.mapToDomain(): List<OrderStatusModel> {
    return mapNotNull {
        it.toDomain()
    }
}

fun ORDER_STATUS_DTO.toDomain(): OrderStatusModel? {
    return OrderStatusModel(
        name = NAME ?: return null,
        background = BACKGROUND ?: "",
        image = IMAGE?.toVodovozUrl() ?: "",
        color = COLOR ?: ""
    )
}

fun OrdersHistoryDetailsDTO.toDomain(): OrdersHistoryDetailsModel {
    return OrdersHistoryDetailsModel(
        title = TITLE ?: "",
        filters = FILTERSTATYS?.mapNotNull { it.toDomain() } ?: emptyList(),
        items = DANNYE?.mapToDomain()
            ?: throw IllegalArgumentException("OrderHistory items can't be null")
    )
}


@JvmName("mapToOrdersHistoryItemModelList")
fun List<ORDERS_HISTORY_ITEM_DTO>.mapToDomain(): List<OrdersHistoryItemModel> {
    return mapNotNull { it.toDomain() }.ifEmpty {
        throw IllegalArgumentException("OrderHistory items can't be null")
    }
}

fun ORDERS_HISTORY_ITEM_DTO.toDomain(): OrdersHistoryItemModel? {
    return OrdersHistoryItemModel(
        id = ID ?: return null,
        date = DATE_INSERT?.toLocalDate(),
        products = ITEMS?.mapToDomain() ?: emptyList(),
        priceText = PRICE ?: "",
        address = ADDRESS ?: "",
        description = NAME ?: "",
        status = STATUS?.toDomain(),
        button = KNOPKA?.toDomain()
    )
}

fun ORDERS_HISTORY_KNOPKA_DTO.toDomain(): OrdersHistoryButtonModel? {
    return OrdersHistoryButtonModel(
        id = ID ?: "",
        name = NAME ?: "",
        color = COLOR_TEXT ?: "",
        background = COLOR_BACKGROUND ?: "",
        image = IMAGE?.toVodovozUrl() ?: "",
        url = URL ?: "",
        browserUrl = BRAYZER == "Y"
    )
}


@JvmName("mapToOrdersHistoryProductModelList")
fun List<ORDERS_HISTORY_PRODUCT_DTO>.mapToDomain(): List<OrdersHistoryProductModel> {
    return mapNotNull { it.toDomain() }
}

fun ORDERS_HISTORY_PRODUCT_DTO.toDomain(): OrdersHistoryProductModel? {
    return OrdersHistoryProductModel(
        showcaseProduct = ACTIVE == "Y",
        image = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        id = ID ?: return null,
        quantity = QUANTITY ?: 1
    )
}


fun FILTER_STATYS_DTO.toDomain(): OrderFilterModel? {
    return OrderFilterModel(
        id = ID ?: return null,
        name = NAME ?: return null
    )
}