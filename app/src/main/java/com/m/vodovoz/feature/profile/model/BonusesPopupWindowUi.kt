package com.m.vodovoz.feature.profile.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.user.BonusesPopupWindowModel

@Immutable
data class BonusesPopupWindowUi(
    val title: String,
    val coupon: String,
    val description: String,
    val warmAboutExpiration: Boolean,
    val messageAboutExpiration: String,
    val button: ColorfulButtonUi,
    val url: String,
    val browser: Boolean,
)

fun BonusesPopupWindowModel.toUi(): BonusesPopupWindowUi {
    return BonusesPopupWindowUi(
        title = title,
        coupon = coupon,
        description = description,
        warmAboutExpiration = warmAboutExpiration,
        messageAboutExpiration = messageAboutExpiration,
        button = button.toUi(),
        url = url,
        browser = browser
    )
}
