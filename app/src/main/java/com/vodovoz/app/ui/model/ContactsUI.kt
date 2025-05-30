package com.vodovoz.app.ui.model

import com.vodovoz.app.R
import com.vodovoz.app.common.content.itemadapter.Item

data class ContactsBundleUI(
    val title: String,
    val phoneUIList: List<PhoneUI>,
    val emailUIList: List<EmailUI>,
    val chatsBundleUI: ChatsBundleUI?
)

data class ChatsBundleUI(
    val name: String,
    val chatUIList: List<ChatUI>
) : Item {

    override fun getItemViewType(): Int {
        return 0
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is ChatsBundleUI) return false

        return this == item
    }

}

data class ChatUI(
    val icon: String,
    val action: String,
    val type: String
) : Item {

    override fun getItemViewType(): Int {
        return 0
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is ChatUI) return false

        return this == item
    }

}

data class PhoneUI(
    val name: String,
    val value: String,
    val type: String
) : Item {

    override fun getItemViewType(): Int {
        return 0
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is PhoneUI) return false

        return this == item
    }

}

data class EmailUI(
    val name: String,
    val value: String,
    val type: String
) : Item {

    override fun getItemViewType(): Int {
        return 0
    }

    override fun areItemsTheSame(item: Item): Boolean {
        if (item !is EmailUI) return false

        return this == item
    }

}