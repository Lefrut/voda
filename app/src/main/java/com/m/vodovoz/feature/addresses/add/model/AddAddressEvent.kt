package com.m.vodovoz.feature.addresses.add.model

sealed interface AddAddressEvent {

    data object GoBackToMap: AddAddressEvent
    data object GoBackToAddresses: AddAddressEvent
    data class GoToMap(val addressName: String) : AddAddressEvent
    data class ShowSnackbar(val message: String) : AddAddressEvent

}