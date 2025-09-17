package com.m.vodovoz.feature.addresses.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.location.AddressModel
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
data class AddressUi(
    val id: Long,
    val personTypeId: Int,
    val description: String,
    val address: String,
): Parcelable {
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