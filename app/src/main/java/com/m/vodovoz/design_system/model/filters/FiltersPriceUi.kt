package com.m.vodovoz.design_system.model.filters

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Parcelize
@Immutable
data class FiltersPriceUi(
    val min: Int,
    val max: Int,
    val currentMin: Int = min,
    val currentMax: Int = max
): Parcelable {
    companion object {
        val Empty = FiltersPriceUi(min = 0, max = Int.MAX_VALUE)
    }
}
