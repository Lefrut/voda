package com.m.vodovoz.feature.buy_certificate.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.product.BuyCertificateTabModel
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.mapToUi

@Immutable
data class BuyCertificateTabUi(
    val id: Int,
    val name: String,
    val fields: List<FieldUi>
){
    companion object{
        val Empty = BuyCertificateTabUi(-1, "", emptyList())
    }
}

fun List<BuyCertificateTabModel>.mapToUi(): List<BuyCertificateTabUi>{
    return map { it.toUi() }
}

fun BuyCertificateTabModel.toUi(): BuyCertificateTabUi{
    return BuyCertificateTabUi(
        id = id,
        name = name,
        fields = fields.mapToUi()
    )
}