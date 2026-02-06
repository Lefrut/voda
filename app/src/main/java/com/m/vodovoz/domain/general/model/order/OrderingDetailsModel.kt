package com.m.vodovoz.domain.general.model.order

import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.cart.OrderSummaryItemModel
import com.m.vodovoz.domain.general.model.widgets.FieldPopupWindowModel

data class OrderingDetailsModel(
    val title: String,
    val recipientSection: SectionModel<OrderingMenuItemModel>,
    val notifySection: OrderNotifySectionModel,
    val paymentSection: SectionModel<OrderingMenuItemModel>,
    val totals: List<OrderSummaryItemModel>,
    val button: ColorfulButtonModel,
    val commentPopupWindow: FieldPopupWindowModel?
){
    companion object{
        const val ADDRESS_MENU = "adress"
        const val RECIPIENT_MENU = "klient"
        const val DELIVERY_TIME_MENU = "time"
        const val PAYMENT_MENU = "oplata"
        const val CALL_YOU_MENU = "vampozvonit"

        const val COMMENT_MENU = "komment"

        const val DOOR_MENU = "dveri"
    }
}

