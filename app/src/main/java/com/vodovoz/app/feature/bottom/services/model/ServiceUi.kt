package com.vodovoz.app.feature.bottom.services.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.service.ServiceModel

@Immutable
data class ServiceUi(
    val name: String,
    val image: String,
    val id: Int,
)

fun List<ServiceModel>.mapToUi(): List<ServiceUi> {
    return map { service -> service.toUi() }
}

fun ServiceModel.toUi(): ServiceUi {
    return ServiceUi(
        name, image, id
    )
}
