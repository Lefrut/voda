package com.vodovoz.app.domain.general.model.location

data class AddressModel(
    val id: Long,
    val personTypeId: Int,
    val description: String,
    val address: String
)
