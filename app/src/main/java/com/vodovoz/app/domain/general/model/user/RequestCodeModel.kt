package com.vodovoz.app.domain.general.model.user

data class RequestCodeModel(
    val waitSeconds: Int,
    val remainingSeconds: Int
)
