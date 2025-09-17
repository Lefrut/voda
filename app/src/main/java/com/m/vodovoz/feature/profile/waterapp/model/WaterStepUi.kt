package com.m.vodovoz.feature.profile.waterapp.model

import androidx.compose.runtime.Immutable

@Immutable
data class WaterStepUi(
    val ml: Int
){
    companion object{
        val Default250 = WaterStepUi(250)
    }
}