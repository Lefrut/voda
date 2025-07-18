package com.vodovoz.app.feature.block_app.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.common.model.VodovozSiteStateContact

@Immutable
data class BlockAppContactUi(
    val url: String,
    val image: String,
    val urlType: String,
)

fun VodovozSiteStateContact.toUi(): BlockAppContactUi {
    return BlockAppContactUi(
        url  = url,
        urlType = urlType,
        image = image
    )
}

fun List<VodovozSiteStateContact>.mapToUi(): List<BlockAppContactUi>{
    return map { it.toUi() }
}
