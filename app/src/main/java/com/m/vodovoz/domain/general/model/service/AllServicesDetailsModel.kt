package com.m.vodovoz.domain.general.model.service

data class AllServicesDetailsModel(
    val title: String,
    val description: String,
    val services: List<ServiceModel>
)
