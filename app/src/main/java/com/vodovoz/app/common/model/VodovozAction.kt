package com.vodovoz.app.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed interface BaseVodovozAction


sealed interface NewVodovozAction : BaseVodovozAction {
    sealed interface Id : NewVodovozAction {
        val id: Long

        data class Product(override val id: Long) : Id
        data class Products(override val id: Long) : Id
        data class Category(override val id: Long) : Id
        data class Brand(override val id: Long) : Id
        data class AllProducts(override val id: Long) : Id
    }

    sealed interface Url : NewVodovozAction {
        val url: String

        data class Default(override val url: String) : Url
        data class WithCookie(override val url: String) : Url
    }

    sealed interface Banner : NewVodovozAction {
        val blockId: Long
        val bannerId: Long

        data class Products(
            override val blockId: Long,
            override val bannerId: Long,
        ) : Banner

        data class Promotions(
            override val blockId: Long,
            override val bannerId: Long,
        ) : Banner
    }

    enum class NoData : NewVodovozAction {
        AllServices,
        AllDiscount,
        AllNewProducts,
        AllPromotions,
        Delivery,
        Profile,
        WaterTracker,
        BuyCertificate,
        CoolerRental,
        FreeCoolerRental,
        CoolerRepair,
        SanitaryMaintenance,
    }

    data class Unknown(val data: String) : NewVodovozAction

}


@Parcelize
sealed interface VodovozAction : Parcelable, BaseVodovozAction {
    @Parcelize
    data class Product(val id: Long) : VodovozAction

    @Parcelize
    data class Products(val blockId: Long, val bannerId: Long) : VodovozAction

    @Parcelize
    data class Category(val id: Long) : VodovozAction

    @Parcelize
    data class Promotion(val id: Long) : VodovozAction

    @Parcelize
    data class Promotions(val blockId: Long, val bannerId: Long) : VodovozAction

    @Parcelize
    data class Brand(val id: Long) : VodovozAction

    @Parcelize
    data class Url(val url: String) : VodovozAction

    @Parcelize
    data class UrlWithCookie(val url: String) : VodovozAction

    @Parcelize
    data class Unknown(val action: String, val id: String?) : VodovozAction

}

@Parcelize
enum class DataAllAction : VodovozAction, ButtonAction {
    AllServices,
    AllDiscount,
    AllNewProducts,
    AllPromotions,
    Delivery,
    Profile,
    WaterTracker,
    BuyCertificate,
    CoolerRental,
    FreeCoolerRental,
    CoolerRepair,
    SanitaryMaintenance,
    Unknown;
}


sealed interface ButtonAction : BaseVodovozAction {

    data class Id(val id: Int) : ButtonAction
}
