package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.equalsTo
import com.m.vodovoz.common.model.from
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.CancelOrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.KNOPKA_ORDER_DTO
import com.m.vodovoz.data.vodovoz_service.model.OPLATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.OrderPlaceholderDTO
import com.m.vodovoz.data.vodovoz_service.model.OrderQuestionDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.BottomCartDTO
import com.m.vodovoz.data.vodovoz_service.model.delivery_date.DATE_INTERVALS_DTO
import com.m.vodovoz.data.vodovoz_service.model.delivery_date.DATE_INTERVAL_DTO
import com.m.vodovoz.data.vodovoz_service.model.delivery_date.DELIVERY_DATE_DTO
import com.m.vodovoz.data.vodovoz_service.model.delivery_date.DeliveryDateDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.ABOUT_ORDER_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ABOUT_ORDER_OKNO_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.CallYouItemDTO
import com.m.vodovoz.data.vodovoz_service.model.order.FILTER_STATYS_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDERING_COMMENT_OKNO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDERS_HISTORY_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDERS_HISTORY_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDERS_HISTORY_PRODUCT_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_DETAILS_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_DETAILS_TOVAR_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_MENU_SECTION_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_MENU_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_PREDYP_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_PREDYP_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_PRODUCT_PODAROK_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.ORDER_STATUS_DTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderCallYouDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrderingDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.OrdersHistoryDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.order.RecipientDTO
import com.m.vodovoz.data.vodovoz_service.model.order.RecipientDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.payment_method.PaymentMethodDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.payment_method.PaymentMethodItemDTO
import com.m.vodovoz.data.vodovoz_service.model.payment_method.PaymentMethodSectionDTO
import com.m.vodovoz.domain.general.model.cart.BottomCartModel
import com.m.vodovoz.domain.general.model.exceptions.VodovozPlaceholderModel
import com.m.vodovoz.domain.general.model.order.AboutOrderItemModel
import com.m.vodovoz.domain.general.model.order.AboutOrderPopupWindowModel
import com.m.vodovoz.domain.general.model.order.CallYouItemModel
import com.m.vodovoz.domain.general.model.order.CancelOrderDetailsModel
import com.m.vodovoz.domain.general.model.order.CertificatePaymentInfoModel
import com.m.vodovoz.domain.general.model.order.DeliveryDateDetailsModel
import com.m.vodovoz.domain.general.model.order.DeliveryDateOptionModel
import com.m.vodovoz.domain.general.model.order.DeliveryTimeIntervalModel
import com.m.vodovoz.domain.general.model.order.FormModel
import com.m.vodovoz.domain.general.model.order.OrderCallYouDetailsModel
import com.m.vodovoz.domain.general.model.order.OrderDetailsButtonModel
import com.m.vodovoz.domain.general.model.order.OrderDetailsModel
import com.m.vodovoz.domain.general.model.order.OrderFilterModel
import com.m.vodovoz.domain.general.model.order.OrderNotifyItemModel
import com.m.vodovoz.domain.general.model.order.OrderNotifySectionModel
import com.m.vodovoz.domain.general.model.order.OrderProductModel
import com.m.vodovoz.domain.general.model.order.OrderProductPresentModel
import com.m.vodovoz.domain.general.model.order.OrderStatusModel
import com.m.vodovoz.domain.general.model.order.OrderingDetailsModel
import com.m.vodovoz.domain.general.model.order.OrderingMenuItemModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryButtonModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryDetailsModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryItemModel
import com.m.vodovoz.domain.general.model.order.OrdersHistoryProductModel
import com.m.vodovoz.domain.general.model.order.PaymentMethodDetailsModel
import com.m.vodovoz.domain.general.model.order.PaymentMethodItemModel
import com.m.vodovoz.domain.general.model.order.RecipientDetailsModel
import com.m.vodovoz.domain.general.model.order.RecipientModel
import com.m.vodovoz.domain.general.model.product.BuyCertificateModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.widgets.FieldModel
import com.m.vodovoz.domain.general.model.widgets.FieldPopupWindowModel
import com.m.vodovoz.util.toRoundIntOrNull
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

fun RecipientDTO.toDomain(): RecipientModel {
    return RecipientModel(
        phone = PHONE ?: "",
        fio = FIO ?: ""
    )
}

fun CallYouItemDTO.toDomain(): CallYouItemModel? {
    return CallYouItemModel(
        name = NAME ?: return null,
        description = OPISANIE ?: "",
        value = VALUE ?: return null,
        code = CODE ?: return null,
        enabled = !VodovozBoolean.from(ZABLOCKPOLE).boolean
    )
}

fun RecipientDetailsDTO.toDomain(): RecipientDetailsModel {
    return RecipientDetailsModel(
        title = TITLE ?: "",
        fields = POLYA?.mapToDomain()
            ?: throw IllegalArgumentException("RecipientDetails fields can't be null"),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("RecipientDetails button can't be null"),
        checkboxes = CHECKBOX?.mapToDomain() ?: emptyList()
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
    )
}

@JvmName("mapToPaymentMethodItemModelList")
fun List<PaymentMethodItemDTO>.mapToDomain(): List<PaymentMethodItemModel> {
    return map { it.toDomain() }
}


fun PaymentMethodItemDTO.toDomain(): PaymentMethodItemModel {
    val field = POLE?.toDomain()
    return PaymentMethodItemModel(
        title = NAME ?: "",
        image = KARTINKA?.toVodovozUrl() ?: "",
        code = CODE ?: "",
        id = ID ?: "",
        field = field,
        maxFieldValue = field?.value?.toRoundIntOrNull(),
        description = OPISANIE ?: ""
    )
}


fun DeliveryDateDetailsDTO.toDomain(): DeliveryDateDetailsModel {
    return DeliveryDateDetailsModel(
        title = TITLE ?: "",
        options = DATE?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("DeliveryDate button can't be null"),
        timeSections = INTERVAL?.mapToDomain()
            ?: throw IllegalArgumentException("DeliveryDate intervals can't be null"),
        earlierCheckbox = RANSHE?.toDomain()
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
        priceText = MONEY ?: "",
        label = TEXTOPIS?.toDomain()
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
    var popupWindow: FieldPopupWindowModel? = null

    return OrderingDetailsModel(
        title = TITLE ?: "",
        recipientSection = requireNotNull(POLYSHATEL) { "Ordering payment can't be null" }.toDomain {
            popupWindow = it
        },
        notifySection = KOMMENT?.PREDYP?.toDomain() ?: OrderNotifySectionModel.Empty,
        paymentSection = requireNotNull(OPLATA) { "Ordering payment can't be null" }.toDomain(),
        totals = ITOG.orEmpty().mapToDomain(),
        button = requireNotNull(KNOPKA) { "Ordering button can't be null" }.toDomain(),
        commentPopupWindow = popupWindow
    )
}

fun ORDER_MENU_SECTION_DTO.toDomain(onPopupWindow: (FieldPopupWindowModel) -> Unit = {}): SectionModel<OrderingMenuItemModel> {
    return SectionModel(
        title = ZAGOLOVOK ?: "",
        items = DANNYE.orEmpty().mapToDomain(onPopupWindow),
        button = null
    )
}

@JvmName("mapToOrderRecipientItemModelList")
fun List<ORDER_MENU_ITEM_DTO>.mapToDomain(onPopupWindow: (FieldPopupWindowModel) -> Unit): List<OrderingMenuItemModel> {
    return mapNotNull { it.toDomain(onPopupWindow) }
}

fun ORDER_MENU_ITEM_DTO.toDomain(onPopupWindow: (FieldPopupWindowModel) -> Unit): OrderingMenuItemModel? {


    return OrderingMenuItemModel(
        image = KARTINKA?.toVodovozUrl().orEmpty(),
        name = NAME.orEmpty(),
        description = OPISANIE.orEmpty(),
        id = ID ?: return null,
        defaultValue = DEFAULT?.toString() ?: DEFAULTVALUE.orEmpty(),
        type = TYPE.orEmpty(),
    ).also {
        DOPOKNO?.toDomain()?.also {
            if (ID == OrderingDetailsModel.COMMENT_MENU) {
                onPopupWindow(it)
            }
        }
    }
}

fun ORDERING_COMMENT_OKNO.toDomain(): FieldPopupWindowModel? {
    return FieldPopupWindowModel(
        title = NAME.orEmpty(),
        description = OPISANIE.orEmpty(),
        field = FieldModel(
            id = FieldModel.COMMENT_ID,
            label = "",
            value = VALUE.orEmpty(),
            valueType = "",
            isRequired = false,
            readOnly = false,
            supportingText = "",
            hint = HINT.orEmpty(),
        ),
        button = (KNOPKA ?: return null).toDomain()
    )
}

fun ORDER_PREDYP_DTO.toDomain(): OrderNotifySectionModel {
    return OrderNotifySectionModel(
        title = NAME ?: "",
        notifyItems = DANNYE?.mapToDomain() ?: emptyList(),
        field = POLE?.toDomain(),
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

fun OPLATA_DTO.toDomain(): CertificatePaymentInfoModel {
    return CertificatePaymentInfoModel(
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

    val topButtons = BLOCK?.KNOPKI?.mapToDomain() ?: emptyList()
    val middleButtons = BLOCK?.KNOPKIOPIS?.mapToDomain() ?: emptyList()

    return OrderDetailsModel(
        title = TITLE?.ZAGOLOVOK ?: "",
        subtitle = TITLE?.OPIS ?: "",
        currentStatus = BLOCK?.STATUS?.mapToDomain() ?: emptyList(),
        header = BLOCK?.GLAV ?: "",
        statuses = BLOCK?.STATUSY?.mapToDomain() ?: emptyList(),
        topButtons = topButtons + middleButtons,
        products = TOVARY?.TOVAR?.mapToDomain() ?: emptyList(),
        productsTitle = TOVARY?.TITLE ?: "",
        bottomButtons = KNOPKI_NIZ?.mapToDomain() ?: emptyList(),
        orderSummary = ITOG?.mapToDomain()
            ?: throw IllegalArgumentException("Order details summary can't be null")
    )
}

fun OrderQuestionDetailsDTO.toDomain(): FormModel {
    return FormModel(
        title = TITLE ?: "",
        description = INFORMIROVANIE ?: "",
        fields = LISTADATA?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("Order question details can't be null"),
        checkbox = null
    )
}

fun KNOPKA_ORDER_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = NAME ?: TEXT ?: TITLE ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLOR ?: TEXTCOLOR ?: "",
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
        price = EXTENDED_PRICE?.minByOrNull { (it.PRICE ?: Int.MAX_VALUE) }?.toDomain(),
        showcase = URL == true,
        image = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        labels = NALICHIE_MORE?.mapToDomain() ?: emptyList(),
        present = PODAROK?.toDomain(),
        pricePerUnit = PROPERTY_TSENA_ZA_EDINITSU_TOVARA_VALUE ?: "",
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
        url = URL ?: "",
        browser = (BRAYZER == "Y").takeIf { useBrowser -> useBrowser },
        driverId = VODITEL,
        isSmall = KNOPKASTYLE == "low"
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
        color = COLOR ?: "",
        backgroundAlpha = BACKGROUNDOPACITY ?: 1f
    )
}

fun OrdersHistoryDetailsDTO.toDomain(): OrdersHistoryDetailsModel {
    return OrdersHistoryDetailsModel(
        title = TITLE ?: "",
        filters = FILTERSTATYS?.mapNotNull { it.toDomain() } ?: emptyList(),
        banners = BANNER?.mapToDomain() ?: emptyList(),
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