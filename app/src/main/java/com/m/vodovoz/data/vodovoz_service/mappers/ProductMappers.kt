package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.AnalogsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.CATEGORY_DTO
import com.m.vodovoz.data.vodovoz_service.model.EXTENDED_PRICE_DTO
import com.m.vodovoz.data.vodovoz_service.model.NALICHIE_MORE_DTO
import com.m.vodovoz.data.vodovoz_service.model.PODELITCA_DTO
import com.m.vodovoz.data.vodovoz_service.model.ProductsSectionDTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_18_DTO
import com.m.vodovoz.data.vodovoz_service.model.TOVAR_DATA_DTO
import com.m.vodovoz.domain.general.model.product.CategoryModel
import com.m.vodovoz.domain.general.model.product.PriceModel
import com.m.vodovoz.domain.general.model.product.ProductModel
import com.m.vodovoz.domain.general.model.product.ProductsSectionModel
import com.m.vodovoz.domain.general.model.product.ShareModel
import com.m.vodovoz.domain.general.model.user.ForAdultsModel
import com.m.vodovoz.domain.general.model.widgets.LabelModel

fun ProductsSectionDTO.toDomain(): ProductsSectionModel {

    val sorting = SORTIROVKA?.DANNIESORT?.mapNotNull { it?.toDomain() } ?: emptyList()
    val products = (DATA ?: TOVAR)?.mapToDomain() ?: emptyList()

    if (sorting.isEmpty() && products.isEmpty()) {
        throw IllegalArgumentException("Product section products and sorting can't be bull")
    }

    return ProductsSectionModel(
        banners = BANNER.orEmpty().mapNotNull { it?.toDomain() },
        title = TITLE ?: "",
        sortingTitle = SORTIROVKA?.NAMEGLAV ?: "",
        productsQuantityText = TOVARVSEGO ?: COUNT?.toString() ?: "",
        sorting = sorting,
        products = products,
        categories = RAZDEL?.LISTRAZDEL?.mapNotNull { it?.toDomain() } ?: emptyList(),
        share = SHARE?.toDomain() ?: ShareModel.Empty,
        forAdults = TOVAR18?.toDomain()
    )
}

fun TOVAR_18_DTO.toDomain(): ForAdultsModel {
    return ForAdultsModel(
        title = TITLE ?: "",
        textBlur = TEXTBLUR ?: "",
        description = OPISANIE ?: "",
        button = KNOPKA?.toDomain()
            ?: throw IllegalArgumentException("TOVAR18 button can't be null")
    )
}

fun CATEGORY_DTO.toDomain(): CategoryModel? {
    return CategoryModel(ID ?: return null, NAME ?: return null, DEPTH_LEVEL)
}

fun PODELITCA_DTO.toDomain(): ShareModel? {
    return ShareModel(
        detailPageUrlIOS?.url ?: return null,
        detailPageUrlIOS.name ?: return null
    )
}

fun AnalogsSectionDTO.toDomain(): ProductsSectionModel {
    val sorting = SORTIROVKA?.DANNIESORT?.mapNotNull { sortDto ->
        sortDto?.toDomain()
    } ?: emptyList()

    val products = TOVAR?.mapNotNull { tovarDto ->
        tovarDto.toDomain()
    } ?: emptyList()

    return ProductsSectionModel(
        banners = emptyList(),
        title = TITLE ?: "",
        sortingTitle = SORTIROVKA?.NAMEGLAV ?: "",
        sorting = sorting,
        products = products,
        categories = emptyList(),
        productsQuantityText = "",
        share = ShareModel.Empty,
        forAdults = TOVAR18?.toDomain()
    )
}

fun List<TOVAR_DATA_DTO>.mapToDomain(): List<ProductModel> {
    return mapNotNull { productDTO ->
        productDTO.toDomain()
    }
}

fun TOVAR_DATA_DTO.toDomain(): ProductModel? {
    return ProductModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        isFavorite = FAVORITE ?: false,
        deposit = PROPERTY_ZALOG_VALUE ?: 0,
        rating = PROPERTY_RATING_VALUE ?: return null,
        picture = DETAIL_PICTURE?.toVodovozUrl() ?: return null,
        pricePerUnit = PROPERTY_TSENA_ZA_EDINITSU_TOVARA_VALUE,
        unitOfMeasurement = EDINICAIZMERENIYA,
        coefficient = KOFFICIENT?.toFloat() ?: 1f,
        quantity = CATALOG_QUANTITY ?: 0,
        firstPrice = EXTENDED_PRICE?.firstOrNull()?.toDomain() ?: return null,
        prices = EXTENDED_PRICE.mapNotNull { it?.toDomain() },
        labels = NALICHIE_MORE?.mapToDomain() ?: emptyList(),
        cartQuantity = 0,
        forAdults = TOVAR18?.toDomain(),
        analogButton = KNOPKI?.toDomain()
    )
}

fun EXTENDED_PRICE_DTO.toDomain(): PriceModel? {
    return PriceModel(
        price = PRICE?.toFloat() ?: return null,
        oldPrice = OLD_PRICE?.toFloat() ?: return null,
        quantityFrom = QUANTITY_FROM ?: return null,
        quantityTo = QUANTITY_TO ?: return null
    )
}

@JvmName("mapLabelToDomain")
fun List<NALICHIE_MORE_DTO>.mapToDomain(): List<LabelModel> {
    return mapNotNull { labelDTO ->
        LabelModel(
            name = labelDTO.NAME ?: return@mapNotNull null,
            textColor = "",
            backgroundColor = labelDTO.CVET ?: labelDTO.BACKGROUND ?: "",
            backgroundAlpha = 1f
        )
    }
}

fun NALICHIE_MORE_DTO.toDomain(): LabelModel {
    return LabelModel(
        name = NAME ?: "",
        textColor = CVET ?: "",
        backgroundColor = BACKGROUND ?: ""
    )

}
