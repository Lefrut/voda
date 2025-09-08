package com.vodovoz.app.data.vodovoz_service.model

import com.squareup.moshi.Json

data class FormDTO(
    @Json(name = "TITLE")
    val TITLE: String?,
    @Json(name = "DANNYE")
    val DANNYE: List<FIELD_DTO>?,
    @Json(name = "POLYA")
    val POLYA: List<FIELD_DTO>?,
    @Json(name = "KNOPKA")
    val KNOPKA: COLORFUL_KNOPKA_DTO?,
    @Json(name = "PODOFERTA")
    val PODOFERTA: CHECKBOX_DTO?,
    @Json(name = "INFORMIROVANIE")
    val INFORMIROVANIE: String?
)


