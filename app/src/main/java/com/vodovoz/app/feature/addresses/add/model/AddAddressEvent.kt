package com.vodovoz.app.feature.addresses.add.model

sealed interface AddAddressEvent {

    data object GoBack: AddAddressEvent

}