package com.m.vodovoz.common.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


sealed interface BaseVodovozAction


@Parcelize
sealed interface VodovozAction : Parcelable, BaseVodovozAction {

    val actionName: String get() = ""

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
    data class Unknown(val action: String, val id: String?) : VodovozAction {

        override val actionName: String get() = action.lowercase()

    }

    data object Name {

        const val CLOSE = "close"

    }

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

    override val actionName: String
        get() = name

}


sealed interface ButtonAction : BaseVodovozAction {

    data class Id(val id: Int) : ButtonAction
}