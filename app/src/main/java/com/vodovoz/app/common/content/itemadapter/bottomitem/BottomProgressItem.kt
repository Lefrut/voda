package com.vodovoz.app.common.content.itemadapter.bottomitem

import com.vodovoz.app.common.content.itemadapter.Item
import java.util.UUID

data class BottomProgressItem(
    val id: Long = UUID.randomUUID().toString().hashCode().toLong()
) : Item {

    override fun getItemViewType(): Int {
        return 0
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is BottomProgressItem) return false

        return id == item.id
    }

}