package com.vodovoz.app.feature.addresses.model

import androidx.compose.runtime.Immutable
import com.vodovoz.app.domain.general.model.location.AddressModel

@Immutable
data class AddressUi(
    val id: Long,
    val personTypeId: Int,
    val description: String,
    val address: String,
){
    companion object{
        val Empty = AddressUi(-1, -1, "", "")
    }
}


fun List<AddressModel>.mapToUi(): List<AddressUi> {
    return map { it.toUi() }
}

fun AddressModel.toUi(): AddressUi {
    return AddressUi(id, personTypeId, description, address)
}