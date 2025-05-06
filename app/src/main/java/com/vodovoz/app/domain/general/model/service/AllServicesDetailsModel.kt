package com.vodovoz.app.domain.general.model.service

data class AllServicesDetailsModel(
    val title: String,
    val description: String,
    val services: List<ServiceModel>
)
