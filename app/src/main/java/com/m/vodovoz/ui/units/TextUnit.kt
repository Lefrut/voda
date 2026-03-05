package com.m.vodovoz.ui.units

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

operator fun TextUnit.minus(unit: TextUnit): TextUnit = when {
    isUnspecified -> this
    type == TextUnitType.Sp -> (value - unit.value).sp
    else -> this
}
