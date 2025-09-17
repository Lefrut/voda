package com.m.vodovoz.domain.general.model.promotion

import com.m.vodovoz.common.model.VodovozAction

data class BannerModel(
    val id: Int,
    val name: String,
    val detailPicture: String,
    val action: VodovozAction,
    val advertising: AboutAdvertisingModel?,
)