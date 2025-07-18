package com.vodovoz.app.feature.block_app.model

import androidx.compose.runtime.Immutable

@Immutable
data class BlockAppState(
    val title: String = "",
    val image: String = "",
    val description: String = "",
    val contacts: List<BlockAppContactUi> = emptyList(),
    val showTime: Boolean = false,
    val days: String = "",
    val hours: String = "",
    val minutes: String = "",
    val seconds: String = ""
)
