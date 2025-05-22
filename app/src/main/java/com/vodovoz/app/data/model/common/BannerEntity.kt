package com.vodovoz.app.data.model.common

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BannerEntity(
    val id: Long,
    val name: String,
    val detailPicture: String,
    val actionEntity: ActionEntity? = null,
) : Parcelable