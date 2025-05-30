package com.vodovoz.app.ui.model.custom

import androidx.compose.runtime.Immutable
import com.vodovoz.app.R
import com.vodovoz.app.common.content.itemadapter.Item

@Immutable
data class BuyCertificatePropertyUI(
    val code: String,
    val name: String,
    val required: Boolean,
    val field: String? = null,
    val text: String,
    val value: String,
    val buyCertificateFieldUIList: List<BuyCertificateFieldUI>? = null,
    val showAmount: Boolean = false,
    val count: Int = 1,
    val error: Boolean = false,
    val currentValue: String = "",
) : Item {
    override fun getItemViewType(): Int {
        return when (code) {
            "buyMoney" -> 5
            "email" -> 0
            "opisanie" -> 1
            else -> -1
        }
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is BuyCertificatePropertyUI) return false

        return code == item.code
    }


}