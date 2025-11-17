package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.cart.CART_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.CartDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.cart.DOPTOVARY_TEXT_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.ITOG_ITEM_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.KNOPKA_PROMOKOD_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.KORZINA_PRODUCT_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.OKNO_PODAROK_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.OKNO_PROMOKOD_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.PODAROK_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.PODAROK_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.PRODUCT_PODAROK_DTO
import com.m.vodovoz.data.vodovoz_service.model.cart.RecommendationsDTO
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsBSModel
import com.m.vodovoz.domain.general.model.cart.AdditionalProductsTextModel
import com.m.vodovoz.domain.general.model.cart.CartButtonModel
import com.m.vodovoz.domain.general.model.cart.CartDetailsModel
import com.m.vodovoz.domain.general.model.cart.CartItemModel
import com.m.vodovoz.domain.general.model.cart.CartPresentItemModel
import com.m.vodovoz.domain.general.model.cart.CartPresentModel
import com.m.vodovoz.domain.general.model.cart.CartPresentPopupWindowModel
import com.m.vodovoz.domain.general.model.cart.CartPromoButtonModel
import com.m.vodovoz.domain.general.model.cart.CartPromoPopupWindowModel
import com.m.vodovoz.domain.general.model.cart.OrderSummaryItemModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

fun CartDetailsDTO.toDomain(): CartDetailsModel {

    val orderSummary =
        ITOG?.mapToDomain() ?: throw IllegalArgumentException("OrderSummary can't be null")

    return CartDetailsModel(
        title = TITLE ?: "",
        countText = COUNT ?: "",
        items = KORZINA?.mapToDomain()
            ?: throw IllegalArgumentException("Cart items can't be null"),
        present = PODAROK?.toDomain(
            orderPrice = orderSummary.getOrElse(1) {
                OrderSummaryItemModel.Empty
            }.value.replace(" ", "").takeWhile { c -> c.isDigit() }.toIntOrNull() ?: 0
        ),
        bottlesButton = KNOPKI?.BYTYLI?.toDomain(),
        promotionalCodeButton = KNOPKI?.PROMOKOD?.toDomain(),
        presentButton = KNOPKI?.PODARKI?.toDomain(),
        orderSummary = orderSummary
    )
}

fun RecommendationsDTO.toDomain(): AdditionalProductsBSModel {
    return AdditionalProductsBSModel(
        title = title ?: "",
        description = description ?: "",
        products = products?.mapToDomain() ?: emptyList(),
        button = button?.toDomain()
    )
}

@JvmName("mapToOrderSummaryItemModelList")
fun List<ITOG_ITEM_DTO>.mapToDomain(): List<OrderSummaryItemModel> {
    return mapNotNull { it.toDomain() }.ifEmpty { throw IllegalArgumentException("OrderSummary can't be null") }
}

fun ITOG_ITEM_DTO.toDomain(): OrderSummaryItemModel {
    return OrderSummaryItemModel(
        name = name ?: "",
        value = value ?: "",
        color = color ?: "",
        displayValue = displayValue ?: "",
        id = id ?: ""
    )
}

fun KNOPKA_PROMOKOD_DTO.toDomain(): CartPromoButtonModel {
    return CartPromoButtonModel(
        title = TITLE ?: "",
        textColor = VALUE?.TEXT?.COLOR ?: "",
        text = VALUE?.TEXT?.TITLE ?: "",
        coupon = VALUE?.COUPON ?: "",
        image = IMAGE?.toVodovozUrl() ?: "",
        id = ID ?: "",
        popupWindow = OKNO?.toDomain() ?: CartPromoPopupWindowModel.Empty
    )
}

fun OKNO_PROMOKOD_DTO.toDomain(): CartPromoPopupWindowModel {
    return CartPromoPopupWindowModel(
        title = TITLE ?: "",
        fieldHint = TEXT_V_POLE ?: "",
        buttonName = KNOPKA?.TITLE ?: "",
        errorText = VALUE?.OSHIBKA,
        borderColor = VALUE?.BORDER ?: "",
        color = VALUE?.COLOR ?: "",
        value = VALUE?.VALUE ?: ""
    )
}


fun CART_KNOPKA_DTO.toDomain(): CartButtonModel {
    return CartButtonModel(
        id = ID ?: "",
        image = IMAGE?.toVodovozUrl() ?: "",
        name = TITLE ?: ""
    )
}

fun PODAROK_DTO.toDomain(orderPrice: Int): CartPresentModel {
    return CartPresentModel(
        id = ID ?: -1,
        title = TITLE ?: "",
        description = OPIS ?: "",
        image = KARTINKA?.toVodovozUrl() ?: "",
        leftToGift = MAXSYMMA ?: OPIS?.filter { it.isDigit() }?.toIntOrNull() ?: 0,
        button = KNOPKA?.toDomain(),
        popupWindow = KNOPKA?.OKNOPODAROK?.toDomain(orderPrice),
        currentGift = orderPrice
    )
}

fun OKNO_PODAROK_DTO.toDomain(orderPrice: Int): CartPresentPopupWindowModel {
    return CartPresentPopupWindowModel(
        items = PODAROK?.mapToDomain() ?: emptyList(),
        button = KNOPKA?.toDomain() ?: ColorfulButtonModel.Empty,
        present = PODAROK_BANNER?.toDomain(orderPrice)
    )
}

@JvmName("mapToCartPresentItemModeList")
fun List<PRODUCT_PODAROK_DTO>.mapToDomain(): List<CartPresentItemModel> {
    return mapNotNull { it.toDomain() }
}

fun PRODUCT_PODAROK_DTO.toDomain(): CartPresentItemModel? {
    return CartPresentItemModel(
        id = ID ?: return null,
        name = NAME ?: "",
        image = DETAIL_PICTURE?.toVodovozUrl() ?: "",
        price = EXTENDED_PRICE?.PRICE,
        oldPrice = EXTENDED_PRICE?.OLD_PRICE,
        forAdults = TOVAR18?.toDomain()
    )
}

fun PODAROK_KNOPKA_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = TEXT ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLOR ?: "",
        id = ID ?: ""
    )
}

fun KORZINA_PRODUCT_DTO.toDomain(): CartItemModel? {
    return CartItemModel(
        id = ID ?: return null,
        productId = PODROBNO?.ID ?: return null,
        priceText = PRICE_FORMATED ?: "",
        productName = PODROBNO.NAME ?: "",
        isFavorite = PODROBNO.FAVORITE ?: false,
        depositText = PODROBNO.PROPERTY_ZALOG_VALUE ?: "",
        articleText = PODROBNO.CML2_ARTICLE ?: "",
        quantity = QUANTITY?.toDoubleOrNull()?.toInt() ?: 0,
        basePrice = BASE_PRICE ?: 0f,
        currentPrice = PRICE ?: 0f,
        canBuy = VodovozBoolean.from(CAN_BUY).boolean,
        discountPrice = DISCOUNT_PRICE ?: 0f,
        discountPercentsText = DISCOUNT_PRICE_PERCENT ?: "",
        image = PODROBNO.DETAIL_PICTURE?.toVodovozUrl() ?: "",
        leftItems = PODROBNO.CATALOG_QUANTITY ?: 0,
        label = PODROBNO.NALICHIE_MORE?.toDomain(),
        hasDiscount = DISCOUNTS_APPLY ?: false,
        restrictionsCode = PODROBNO.ZAPRET_FISHKAM ?: 0,
        showcase = PODROBNO.URL == true,
        forAdults = (TOVAR18 ?: PODROBNO.TOVAR18)?.toDomain(),
        additionalProductsText = DOPTOVARY?.toDomain(),
    )
}

fun DOPTOVARY_TEXT_DTO.toDomain(): AdditionalProductsTextModel? {
    return AdditionalProductsTextModel(
        text = TEXT ?: return null,
        articleNumber = ARTICLE ?: "",
        productsId = TOVARY_ID ?: return null
    )
}

fun List<KORZINA_PRODUCT_DTO>.mapToDomain(): List<CartItemModel> {
    return mapNotNull { it.toDomain() }
}