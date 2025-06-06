package com.vodovoz.app.domain.general.model.addresses

data class AddressModel(
    val id: Long,
    val personTypeId: Int,
    val description: String,
    val address: String
)
