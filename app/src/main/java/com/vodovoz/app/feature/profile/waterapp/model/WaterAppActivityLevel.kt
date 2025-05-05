package com.vodovoz.app.feature.profile.waterapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.vodovoz.app.R

enum class WaterAppActivityLevel(
    @StringRes
    val titleId: Int,
    @StringRes
    val descriptionId: Int,
    @DrawableRes
    val imageId: Int,
    val value: Float,
) {
    Low(
        titleId = R.string.activity_level_low_title,
        descriptionId = R.string.activity_level_low_desc,
        imageId = R.drawable.pic_activity_low,
        value = 0.25f
    ),
    Medium(
        titleId = R.string.activity_level_medium_title,
        descriptionId = R.string.activity_level_medium_desc,
        imageId = R.drawable.pic_activity_medium,
        value = 0.375f
    ),
    High(
        titleId = R.string.activity_level_high_title,
        descriptionId = R.string.activity_level_high_desc,
        imageId = R.drawable.pic_activity_high,
        value = 0.5f
    );


    companion object{
        fun getByValue(value: String): WaterAppActivityLevel {
            return entries.firstOrNull { it.value.toString() == value } ?: Low
        }
    }
}

