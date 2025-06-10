package com.vodovoz.app.domain.general.model.promotion

import com.vodovoz.app.common.model.VodovozAction

data class BannerModel(
    val id: Int,
    val name: String,
    val detailPicture: String,
    val action: VodovozAction,
    val advertising: AboutAdvertisingModel?,
)