package com.vodovoz.app.feature.block_app.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.common.model.BlockSiteContact

@Immutable
data class BlockAppContactUi(
    val url: String,
    val image: String,
    val urlType: String,
)

fun BlockSiteContact.toUi(): BlockAppContactUi {
    return BlockAppContactUi(
        url  = url,
        urlType = urlType,
        image = image
    )
}

fun List<BlockSiteContact>.mapToUi(): List<BlockAppContactUi>{
    return map { it.toUi() }
}
