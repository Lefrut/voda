package com.m.vodovoz.domain.general.model.promotion

import androidx.compose.runtime.Immutable

@Immutable
data class FloatingPromoButtonModel(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val action: String,
    val actionId: String,
    val blockId: Long,
    val leftScreenNames: Set<String>,
    val rightScreenNames: Set<String>,
    val width: Int?,
    val height: Int?,
)
