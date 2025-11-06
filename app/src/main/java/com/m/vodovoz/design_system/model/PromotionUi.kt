package com.m.vodovoz.design_system.model

import androidx.compose.runtime.Immutable
import com.m.vodovoz.domain.general.model.promotion.AboutAdvertisingModel
import com.m.vodovoz.domain.general.model.promotion.PromotionCategoryModel
import com.m.vodovoz.domain.general.model.promotion.PromotionDetailsModel
import com.m.vodovoz.domain.general.model.promotion.PromotionModel
import com.m.vodovoz.domain.general.model.promotion.SpecialPromotionModel
import java.time.Duration
import java.time.ZonedDateTime
import java.util.Locale


@Immutable
data class SpecialPromotionUi(
    val id: Int,
    val name: String,
    val text: String,
    val picture: String,
    val actionWithButton: ActionWithButtonUi?,
    val aboutAdvertising: AboutAdvertisingUi?
) {

    companion object {
        val Empty = SpecialPromotionUi(-1, "", "", "", null, null)
    }

}


fun SpecialPromotionModel.toUi(): SpecialPromotionUi {
    return SpecialPromotionUi(
        id = id,
        name = name,
        text = text,
        picture = picture,
        actionWithButton = actionWithButton?.toUi(),
        aboutAdvertising = aboutAdvertising?.toUi()
    )
}

@Immutable
data class PromotionDetailsUi(
    val id: Int,
    val picture: String,
    val name: String,
    val description: String,
    val timeLeft: String?,
    val advertising: AboutAdvertisingUi?,
    val label: LabelUi?,
) {
    companion object {
        val Empty = PromotionDetailsUi(
            id = -1,
            picture = "",
            name = "",
            description = "",
            timeLeft = "",
            advertising = AboutAdvertisingUi.Empty,
            label = null
        )
    }
}

fun PromotionDetailsModel.toUi(): PromotionDetailsUi {
    return PromotionDetailsUi(
        id = this.id,
        picture = this.picture,
        name = this.name,
        description = this.description,
        timeLeft = endDate?.let { timeRemainingToEnd(endDate) },
        advertising = this.advertising?.toUi(),
        label = label?.toUi()
    )
}


@Immutable
data class PromotionUi(
    val id: Long,
    val picture: String,
    val label: LabelUi?,
    val categoryId: Int,
    val blockId: Int,
    val timeLeft: String?,
    val name: String,
    val aboutAdvertisingUi: AboutAdvertisingUi?,
)


@Immutable
data class AboutAdvertisingUi(
    val name: String,
    val title: String,
    val aboutCompanyTitle: String,
    val aboutCompany: String,
) {
    companion object {
        val Empty = AboutAdvertisingUi(
            name = "",
            title = "",
            aboutCompanyTitle = "",
            aboutCompany = ""
        )
    }

}

@Immutable
data class PromotionCategoryUi(
    val id: Int,
    val code: String,
    val name: String,
) {

    companion object {
        val Empty = PromotionCategoryUi(
            -1, "", ""
        )
    }

}


@JvmName("mapPromotionSectionListToDomain")
fun List<PromotionCategoryUi>.mapToDomain(): List<PromotionCategoryModel> {
    return map { promotionCategoryUi -> promotionCategoryUi.toDomain() }
}

fun PromotionCategoryUi.toDomain(): PromotionCategoryModel {
    return PromotionCategoryModel(
        id = id,
        code = code,
        name = name
    )
}


@JvmName("mapPromotionSectionListToUi")
fun List<PromotionCategoryModel>.mapToUi(): List<PromotionCategoryUi> {
    return map { it.toUi() }
}

fun PromotionCategoryModel.toUi(): PromotionCategoryUi {
    return PromotionCategoryUi(
        id = id,
        code = code,
        name = name
    )
}


fun AboutAdvertisingModel.toUi(): AboutAdvertisingUi {
    return AboutAdvertisingUi(
        name = name,
        title = title,
        aboutCompanyTitle = aboutCompanyTitle,
        aboutCompany = aboutCompany
    )
}

fun PromotionModel.toUi(): PromotionUi {
    return PromotionUi(
        id = id,
        picture = detailPicture,
        label = label?.toUi(),
        categoryId = sectionId,
        blockId = blockId,
        timeLeft = endDate?.let { timeRemainingToEnd(endDate) },
        name = name,
        aboutAdvertisingUi = advertising?.toUi()
    )
}

fun timeRemainingToEnd(endDateTime: ZonedDateTime): String? {
    val now = ZonedDateTime.now()
    val duration = Duration.between(now, endDateTime)

    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60

    return runCatching {
        String.format(Locale.ROOT, "%02dд : %dч : %02dм", days, hours, minutes)
    }.getOrNull()
}

fun List<PromotionModel>.mapToUi(): List<PromotionUi> {
    return map { it.toUi() }
}
