package com.m.vodovoz.data.vodovoz_service.model.auth


import com.squareup.moshi.Json
import androidx.annotation.Keep
import com.m.vodovoz.data.vodovoz_service.model.address.SWITCH_DTO

@Keep
data class AccountTypeSectionDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "CHEKBOX")
    val CHEKBOX: List<SWITCH_DTO>?
)