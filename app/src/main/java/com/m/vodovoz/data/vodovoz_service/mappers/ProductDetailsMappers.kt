package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.COMMENT_DTO
import com.m.vodovoz.data.vodovoz_service.model.PresentDTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_KNOPKA_DATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_KNOPKA_VALUE_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_RAZDEL_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_RAZDEL_INFO_DATA_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_RAZDEL_INFO_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_TOVAR_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOCK_U_BLOCK_KNOPKA_DIZAIN_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOK_KNOPKA_DIZAIN_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BLOK_KNOPKA_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.BONUSCENA_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.DETAILTEXT_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.DOCUMENTS_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.DOCUMENT_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.HARAKTERISTIKI_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.HARAKTERISTIK_BIND_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.INFORMATIONS_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.KNOPKA_DESHEVLE_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.KNOPKA_KUPIT_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.PRICE_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.PRODUCT_DETAIL_HARAKTERISTIKI_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.ProductDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.RUTUBEVIDEO_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.TAGS_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.TOVAR_DETAIL_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.TOVAR_DETAIL_TEXT_DTO
import com.m.vodovoz.data.vodovoz_service.model.product_details.ZALOG_DTO
import com.m.vodovoz.domain.general.model.widgets.ContentBlockModel
import com.m.vodovoz.domain.general.model.promotion.PresentInfoModel
import com.m.vodovoz.domain.general.model.product.BlockPromoDataModel
import com.m.vodovoz.domain.general.model.product.BrandCategoryBlockModel
import com.m.vodovoz.domain.general.model.product.BrandCategoryItemDataModel
import com.m.vodovoz.domain.general.model.product.BrandCategoryItemModel
import com.m.vodovoz.domain.general.model.product.ButtonBlockModel
import com.m.vodovoz.domain.general.model.product.ButtonDesignBlockModel
import com.m.vodovoz.domain.general.model.product.BuyButtonModel
import com.m.vodovoz.domain.general.model.product.CharacteristicModel
import com.m.vodovoz.domain.general.model.product.CharacteristicsBlockModel
import com.m.vodovoz.domain.general.model.product.CommentModel
import com.m.vodovoz.domain.general.model.product.DepositModel
import com.m.vodovoz.domain.general.model.product.DesignBlockModel
import com.m.vodovoz.domain.general.model.product.DocumentModel
import com.m.vodovoz.domain.general.model.product.OldNewPriceModel
import com.m.vodovoz.domain.general.model.product.ProductBonusesModel
import com.m.vodovoz.domain.general.model.product.ProductDetailsButtonsModel
import com.m.vodovoz.domain.general.model.product.ProductDetailsModel
import com.m.vodovoz.domain.general.model.product.ProductDetailsScreenModel
import com.m.vodovoz.domain.general.model.product.ProductDetailsTabModel
import com.m.vodovoz.domain.general.model.product.ProductVideoModel
import com.m.vodovoz.domain.general.model.product.PromoProductModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel

fun PresentDTO.toDomain(): PresentInfoModel {
    return PresentInfoModel(html = TEXT ?: "")
}

private fun BLOCK_RAZDEL_DTO.toDomain(): BrandCategoryBlockModel {
    return BrandCategoryBlockModel(
        brand = BRAND?.toDomain(),
        category = RAZDEL?.toDomain()
    )
}

private fun BLOCK_RAZDEL_INFO_DATA_DTO.toDomain(): BrandCategoryItemDataModel? {
    return BrandCategoryItemDataModel(
        id = ID ?: return null,
        name = NAME ?: return null,
        detailPicture = DETAIL_PICTURE?.toVodovozUrl() ?: return null
    )
}

private fun BLOCK_RAZDEL_INFO_DTO.toDomain(): BrandCategoryItemModel? {
    return BrandCategoryItemModel(
        title = TITLE ?: return null,
        data = DATA?.toDomain() ?: return null
    )
}

private fun TOVAR_DETAIL_DTO.toDomain(
    shareUrlText: String,
    commentsCount: Int,
): ProductDetailsModel {
    val detailPicture = DETAIL_PICTURE?.toVodovozUrl()

    return ProductDetailsModel(
        id = ID ?: throw IllegalArgumentException("ID cannot be null"),
        name = NAME ?: throw IllegalArgumentException("Product name cannot be null"),
        blockBrandCategory = BLOCKRAZDEL?.toDomain() ?: BrandCategoryBlockModel(null, null),
        information = INFORMATIONS?.toDomain(),
        detailInfo = DETAIL_TEXT?.toDomain()
            ?: throw IllegalArgumentException("Detail text cannot be null"),
        characteristics = HARAKTERISTIKI?.toDomain()
            ?: throw IllegalArgumentException("Characteristics cannot be null"),
        documents = DOCUMENTS?.toDomain()
            ?: throw IllegalArgumentException("Documents cannot be null"),
        detailPicture = detailPicture
            ?: throw IllegalArgumentException("Detail picture cannot be null"),
        pictures = ((MORE_PHOTO?.map { img -> img.toVodovozUrl() }
            ?: emptyList()) + detailPicture).distinct().reversed(),
        sectionTags = TAGS?.toDomain() ?: SectionModel.empty(),
        isFavorite = FAVORITE ?: false,
        isAvailable = (KOLLTOVAR ?: 0) > 0,
        productQuantity = KOLLTOVAR ?: 0,
        labels = NALICHIE?.mapToDomain() ?: emptyList(),
        rating = PROPERTY_RATING_VALUE?.toFloat() ?: 0f,
        deposit = ZALOG?.toDomain(),
        shareUrl = DETAIL_PAGE_URL?.toVodovozUrl() ?: "",
        shareUrlText = shareUrlText,
        rutubeVideo = RUTUBE_VIDEO?.firstOrNull()?.toDomain(),
        youtubeVideo = YOUTUBE_VIDEO?.firstOrNull()?.toDomain(),
        coefficient = KOFFICIENT ?: 1f,
        pricePerUnit = DOPTSENA_ZA_EDINICY,
        articleNumber = HARAKTERISTIKI.toArticleNumber() ?: "",
        firstPrice = EXTENDEDPRICE?.firstOrNull()?.toDomain()
            ?: throw IllegalArgumentException("First extended price cannot be null"),
        prices = EXTENDEDPRICE.mapNotNull { extendedPriceDto -> extendedPriceDto.toDomain() },
        commentsCount = commentsCount,
        forAdultsModel = TOVAR18?.toDomain(),
        bonuses = BONUS_CENA?.toDomain()
    )
}

fun BONUSCENA_DTO.toDomain(): ProductBonusesModel {
    return ProductBonusesModel(
        image = IKONKA?.toVodovozUrl() ?: "",
        bonuses = BONUS ?: ""
    )
}


fun RUTUBEVIDEO_DTO.toDomain(): ProductVideoModel? {
    return ProductVideoModel(
        previewImage = IMAGE ?: return null,
        code = VIDEO ?: return null
    )
}

fun PRODUCT_DETAIL_HARAKTERISTIKI_DTO.toArticleNumber(): String {
    DATA?.map { harakteristikiDto ->
        val articleNumber = harakteristikiDto.BINDS?.firstOrNull { harakteristikBindDto ->
            harakteristikBindDto?.CODE?.contains("ARTICLE") == true
        }?.VALUE
        if (articleNumber != null) return articleNumber
    }

    return ""
}

fun DOCUMENTS_DTO.toDomain(): ContentBlockModel<List<DocumentModel>> {
    return ContentBlockModel(
        title = TITLE ?: "",
        content = DATA?.mapToDomain() ?: emptyList(),
        id = ID ?: "documents"
    )
}

fun List<DOCUMENT_DTO>.mapToDomain(): List<DocumentModel> {
    return mapNotNull { documentDto -> documentDto.toDomain() }
}

fun DOCUMENT_DTO.toDomain(): DocumentModel? {
    return DocumentModel(
        type = TYPE ?: "",
        size = FILE_SIZE?.toFloat() ?: return null,
        sizeText = FILE_SIZE_FORMAT ?: return null,
        iconUrl = IKONKA?.toVodovozUrl() ?: "",
        description = DESCRIPTION ?: "",
        src = SRC?.toVodovozUrl() ?: ""
    )
}

fun PRODUCT_DETAIL_HARAKTERISTIKI_DTO.toDomain(): ContentBlockModel<List<CharacteristicsBlockModel>> {
    return ContentBlockModel(
        title = TITLE ?: "",
        content = DATA?.mapNotNull { harakteristikDto -> harakteristikDto.toDomain() }
            ?: emptyList(),
        id = ID ?: "xarakteristik"
    )
}

fun HARAKTERISTIKI_DTO.toDomain(): CharacteristicsBlockModel? {
    return CharacteristicsBlockModel(
        id = ID?.toLong() ?: return null,
        code = CODE ?: "",
        name = NAME ?: "",
        sort = SORT ?: "",
        characteristics = BINDS?.mapNotNull { bindDto -> bindDto?.toDomain() } ?: emptyList()
    )
}

fun HARAKTERISTIK_BIND_DTO.toDomain(): CharacteristicModel? {
    return CharacteristicModel(
        id = ID ?: return null,
        code = CODE ?: "",
        name = NAME ?: return null,
        value = VALUE ?: "",
        hint = HINT?.ifBlank { null }
    )
}

fun TAGS_DTO.toDomain(): SectionModel<String> {
    return SectionModel(
        title = TITLE ?: "",
        items = TAGS?.mapNotNull { tag -> tag } ?: emptyList(),
        button = null
    )
}

fun ZALOG_DTO.toDomain(): DepositModel? {
    val description = OPISANIE
    return DepositModel(
        price = PRICE?.toFloat() ?: return null,
        description = ContentBlockModel(
            title = description?.TEXT ?: "",
            content = description?.DOPOPISANIE ?: "",
            id = ""
        ),
    )
}

fun INFORMATIONS_DTO.toDomain(): ContentBlockModel<String> {
    return ContentBlockModel(
        title = TITLE ?: "",
        content = ZNACHENIE ?: "",
        id = ""
    )
}

fun TOVAR_DETAIL_TEXT_DTO.toDomain(): ContentBlockModel<String> {
    return ContentBlockModel(
        title = TITLE ?: "",
        content = OPISANIE ?: "",
        id = ID ?: "detailtext"
    )
}

private fun KNOPKA_DESHEVLE_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = NAME ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = TEXTCOLOR ?: "",
    )
}

private fun KNOPKA_KUPIT_DTO.toDomain(): BuyButtonModel {
    return BuyButtonModel(
        textColor = COLORTEXT ?: "",
        backgroundColor = BACKGROUND ?: "",
        title = TITLE ?: "",
        productId = IDTOVAR ?: "",
        moreProductId = DOPTOVAR ?: ""
    )
}

private fun BLOK_KNOPKA_DTO.toDomain(): ButtonBlockModel? {
    return ButtonBlockModel(
        button = KNOPKA?.toDomain() ?: return null,
        data = DATA?.toDomain() ?: return null,
        buyButton = KNOPKA_KUPIT?.toDomain()
    )
}

private fun PRICE_DTO.toDomain(): OldNewPriceModel {
    return OldNewPriceModel(
        old = OLD ?: "",
        new = NEW ?: ""
    )
}

@JvmName("mapToPromoProductList")
private fun List<BLOCK_TOVAR_DTO>.mapToDomain(): List<PromoProductModel> {
    return mapNotNull { it.toDomain() }
}

private fun BLOCK_TOVAR_DTO.toDomain(): PromoProductModel? {
    return PromoProductModel(
        name = NAME ?: "",
        price = PRICE?.toDomain() ?: return null,
        image = KARTINKA?.toVodovozUrl() ?: "",
        quantity = KOLLTOVAR ?: 0
    )
}

private fun BLOCK_KNOPKA_DATA_DTO.toDomain(): BlockPromoDataModel {
    return BlockPromoDataModel(
        title = TITLE ?: "",
        description = OPISANIE ?: "",
        products = TOVAR?.mapToDomain() ?: emptyList(),
    )
}

private fun BLOCK_KNOPKA_VALUE_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = TITLE ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = COLORTEXT ?: "",
    )
}

fun ProductDetailsDTO.toDomain(): ProductDetailsScreenModel {

    val moreButtons = TOVAR?.DOPKNOPKI

    val commentsCount = COMMENTS?.COMMEN_COUNT ?: COMMENTS?.COMMENTS?.size ?: 0

    return ProductDetailsScreenModel(
        details = TOVAR?.toDomain(
            shareUrlText = SHARE?.detail_page_url_ios?.run { "$NAME $URL" } ?: "",
            commentsCount = commentsCount
        ) ?: throw NoSuchElementException("Product details not found."),
        buttons = ProductDetailsButtonsModel(
            blockButton = moreButtons?.BLOK_KNOPKA?.toDomain(),
            blockDesignButton = moreButtons?.BLOK_KNOPKA_DIZAIN?.toDomain(),
            multiBuyButton = KNOPKI?.DESHEVLE?.toDomain(),
            analogButton = KNOPKI?.ANALOG?.toDomain(),
            preOrderButton = KNOPKI?.ZAKAZAT?.toDomain()
        ),
        moreProducts = BLOCTOVAR?.map { it -> it.toDomain() } ?: emptyList(),
        comments = COMMENTS?.COMMENTS?.mapNotNull { commentDto -> commentDto?.toDomain() }
            ?: emptyList(),
        tabs = DETAILTEXT?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

private fun DETAILTEXT_DTO.toDomain(): ProductDetailsTabModel? {
    return ProductDetailsTabModel(
        title = TITLE ?: return null,
        dataId = DATAID ?: return null
    )
}

fun COMMENT_DTO.toDomain(): CommentModel {
    return CommentModel(
        userName = NAME ?: "",
        userPhoto = USER_PHOTO?.toVodovozUrl() ?: "",
        text = TEXT ?: "",
        dateText = DATA ?: "",
        rating = RATING ?: 0,
        purchased = KYPLEN ?: "",
        images = IMAGES?.map { image ->
            image.toVodovozUrl()
        } ?: emptyList()
    )
}

private fun BLOK_KNOPKA_DIZAIN_DTO.toDomain(): ButtonDesignBlockModel? {
    return ButtonDesignBlockModel(
        block = BLOCK?.toDomain() ?: return null,
        data = DATA?.toDomain() ?: return null,
        buyButton = KNOPKA_KUPIT?.toDomain()
    )
}

private fun BLOCK_U_BLOCK_KNOPKA_DIZAIN_DTO.toDomain(): DesignBlockModel? {
    return DesignBlockModel(
        title = TITLE ?: "",
        image = KARTINKA?.toVodovozUrl() ?: "",
        button = KNOPKA?.toDomain(),
        background = BACKGROUND ?: "",
        textColor = TEXTCOLOR ?: "",
        borderColor = BORDER_COLOR ?: ""
    )
}
