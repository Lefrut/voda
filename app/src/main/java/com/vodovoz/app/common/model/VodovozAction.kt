package com.vodovoz.app.common.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize


@Parcelize
sealed interface VodovozAction: Parcelable {
    data class Product(val id: Long) : VodovozAction
    data class Products(val blockId: Long, val bannerId: Long) : VodovozAction
    data class Category(val id: Long) : VodovozAction
    data class Promotion(val id: Long) : VodovozAction
    data class Promotions(val blockId: Long, val bannerId: Long) : VodovozAction
    data class Brand(val id: Long) : VodovozAction
    data class Url(val url: String) : VodovozAction
    data class UrlWithCookie(val url: String) : VodovozAction

    data class Unknown(val action: String, val id: String?) : VodovozAction

}

enum class DataAllAction : VodovozAction {
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


@Immutable
sealed class ButtonAction {

    @Immutable
    data class Id(val id: Int) : ButtonAction()

    @Immutable
    data class Action(val value: DataAllAction) : ButtonAction()

}
