package com.m.vodovoz.feature.home.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.design_system.model.ColorfulButtonUi
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.domain.general.model.promotion.AppUpdateInfoModel

@Immutable
data class AppUpdateInfoUi(
    val id: Int,
    val title: String,
    val text: String,
    val playMarketUrl: String,
    val androidVersion: String,
    val picture: String,
    val colorfulButton: ColorfulButtonUi?,
)


fun AppUpdateInfoModel.toUi(): AppUpdateInfoUi {
    return AppUpdateInfoUi(
        id, title, text, playMarketUrl, androidVersion, picture, colorfulButton?.toUi()
    )
}

fun compareVersions(v1: String, v2: String): Int {
    val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
    val maxLength = maxOf(parts1.size, parts2.size)

    val extended1 = parts1 + List(maxLength - parts1.size) { 0 }
    val extended2 = parts2 + List(maxLength - parts2.size) { 0 }

    return extended1.zip(extended2).map { it.first.compareTo(it.second) }
        .firstOrNull { it != 0 } ?: 0
}
