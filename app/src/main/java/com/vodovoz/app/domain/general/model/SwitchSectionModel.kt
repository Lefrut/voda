package com.vodovoz.app.domain.general.model

data class SwitchSectionModel(
    val title: String,
    val description: String,
    val switches: List<SwitchModel>,
)
