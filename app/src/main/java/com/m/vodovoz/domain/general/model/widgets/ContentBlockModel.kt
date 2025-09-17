package com.m.vodovoz.domain.general.model.widgets

data class ContentBlockModel<T>(
    val title: String,
    val content: T,
    val id: String,
)