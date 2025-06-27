package com.vodovoz.app.feature.addresses.add.model

sealed interface AddAddressEvent {

    data object GoBack: AddAddressEvent
    data class GoToMap(val addressName: String) : AddAddressEvent
    data class ShowSnackbar(val message: String) : AddAddressEvent

}