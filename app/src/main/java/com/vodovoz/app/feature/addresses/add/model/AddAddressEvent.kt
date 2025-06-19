package com.vodovoz.app.feature.addresses.add.model

sealed interface AddAddressEvent {

    data object GoBack: AddAddressEvent
    data class GoToMap(val addressId: Long, val addressName: String) : AddAddressEvent

}