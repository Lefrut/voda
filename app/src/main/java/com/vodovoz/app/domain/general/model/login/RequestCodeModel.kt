package com.vodovoz.app.domain.general.model.login

data class RequestCodeModel(
    val waitSeconds: Int,
    val remainingSeconds: Int
)
