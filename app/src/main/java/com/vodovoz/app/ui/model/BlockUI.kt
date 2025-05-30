package com.vodovoz.app.ui.model

import android.os.Parcelable
import com.vodovoz.app.R
import com.vodovoz.app.common.content.itemadapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class BlockUI(
    val description: String,
    val button: ButtonUI,
    val productId: String,
    val extProductId: String
) : Parcelable, Item {

    override fun getItemViewType(): Int {
        return 100
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is BlockUI) return false

        return this == item
    }

}
