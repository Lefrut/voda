package com.m.vodovoz.data.vodovoz_service.mappers

import com.m.vodovoz.common.model.VodovozBoolean
import com.m.vodovoz.common.model.boolean
import com.m.vodovoz.common.model.from
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.data.vodovoz_service.di.toVodovozUrl
import com.m.vodovoz.data.vodovoz_service.model.profile.BonusesPopupWindowDTO
import com.m.vodovoz.data.vodovoz_service.model.profile.CHAT_MENU_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.DENIGI_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.KNOPKA_BONUSES_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFILE_BLOCK_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFILE_MENO_OKNO_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFILE_MINI_MENU_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFILE_NORMAL_MENU_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFILE_TEXT_OKNO_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.PROFIL_DTO
import com.m.vodovoz.data.vodovoz_service.model.profile.ProfileDetailsDTO
import com.m.vodovoz.data.vodovoz_service.model.profile.TEXT_KNOPKA_DTO
import com.m.vodovoz.domain.general.model.product.SectionModel
import com.m.vodovoz.domain.general.model.promotion.ColorfulButtonModel
import com.m.vodovoz.domain.general.model.user.BonusesPopupWindowModel
import com.m.vodovoz.domain.general.model.user.ProfileCardModel
import com.m.vodovoz.domain.general.model.user.ProfileChatItemModel
import com.m.vodovoz.domain.general.model.user.ProfileChatsPopupWindowModel
import com.m.vodovoz.domain.general.model.user.ProfileDetailsModel
import com.m.vodovoz.domain.general.model.user.ProfileMenuItemModel
import com.m.vodovoz.domain.general.model.user.ProfilePopupWindowModel
import com.m.vodovoz.domain.general.model.user.ProfileWalletItemModel
import com.m.vodovoz.domain.general.model.user.TextButtonModel
import com.m.vodovoz.domain.general.model.user.UserInfoBlockModel


fun ProfileDetailsDTO.toDomain(): ProfileDetailsModel {
    return ProfileDetailsModel(
        userInfoBlock = PROFIL?.toDomain()
            ?: throw IllegalArgumentException("UserInfoBlock can't be null"),
        cards = BLOCK?.map { it.toDomain() } ?: emptyList(),
        walletItems = DENIGI?.map { it.toDomain() } ?: emptyList(),
        banners = BANNER?.mapToDomain() ?: emptyList(),
        smallMenu = MENU?.MINI?.map { it.toDomain() } ?: emptyList(),
        normalMenu = MENU?.NORMAL?.map { it.toDomain() } ?: emptyList(),
        //sectionProducts = TOVARY?.toDomain() ?: SectionModel.empty()
    )
}

fun DENIGI_DTO.toDomain(): ProfileWalletItemModel {
    return ProfileWalletItemModel(
        background = BACKGROUND ?: "",
        titleColor = ZAGALOVOK?.TEXTCOLOR ?: "",
        title = ZAGALOVOK?.TITLE ?: "",
        description = OPISANIE?.TITLE ?: "",
        descriptionColor = OPISANIE?.TEXTCOLOR ?: "",
        imageUrl = IMAGE?.toVodovozUrl() ?: "",
        id = ID ?: "",
        popupWindow = TEXT_OKNO?.toDomain()
    )
}

fun PROFILE_TEXT_OKNO_DTO.toDomain(): ProfilePopupWindowModel {
    return ProfilePopupWindowModel(
        title = TITLE ?: "",
        text = TEXT ?: ""
    )
}


fun PROFILE_BLOCK_DTO.toDomain(): ProfileCardModel {
    return ProfileCardModel(
        title = ZAGALOVOK?.TITLE ?: "",
        titleColor = ZAGALOVOK?.TEXTCOLOR ?: "",
        description = OPISANIE?.TITLE ?: "",
        descriptionColor = OPISANIE?.TEXTCOLOR ?: "",
        imageUrl = IMAGE?.toVodovozUrl() ?: "",
        id = ID ?: "",
        popupWindow = TEXT_OKNO?.toDomain()
    )
}


fun PROFIL_DTO.toDomain(): UserInfoBlockModel {
    return UserInfoBlockModel(
        phoneNumber = this.FIO ?: "",
        fullName = this.FIO ?: "",
        imageUrl = this.IMAGE?.toVodovozUrl() ?: "",
        textButton = this.TEXT_KNOPKA?.toDomain() ?: TextButtonModel.Empty
    )
}


fun TEXT_KNOPKA_DTO.toDomain(): TextButtonModel {
    return TextButtonModel(
        text = this.TITLE ?: "",
        textColor = this.TEXTCOLOR ?: ""
    )
}


fun PROFILE_MINI_MENU_DTO.toDomain(): ProfileMenuItemModel {
    return ProfileMenuItemModel(
        text = this.TEXT ?: "",
        imageUrl = this.IMAGE?.toVodovozUrl() ?: "",
        id = this.ID ?: "",
        description = "",
        popupWindow = null
    )
}

fun PROFILE_NORMAL_MENU_DTO.toDomain(): ProfileMenuItemModel {
    return ProfileMenuItemModel(
        text = this.TEXT ?: "",
        imageUrl = this.IMAGE?.toVodovozUrl() ?: "",
        id = this.ID ?: "",
        description = this.OPISANIE ?: "",
        popupWindow = this.TEXT_OKNO?.toDomain()
    )
}


fun PROFILE_MENO_OKNO_DTO.toDomain(): ProfileChatsPopupWindowModel {
    return ProfileChatsPopupWindowModel(
        title = this.ID?.TITLE ?: "",
        description = this.ID?.OPISANIE ?: "",
        menu = this.ID?.MENU?.mapNotNull { it?.toDomain() } ?: emptyList()
    )
}


fun CHAT_MENU_DTO.toDomain(): ProfileChatItemModel {
    return ProfileChatItemModel(
        name = this.TEXT ?: "",
        imageUrl = VodovozWebConfig.VODOVOZ_URL + (this.IMAGE ?: ""),
        transitionData = this.CHATDAN ?: "",
        id = this.ID ?: ""
    )
}


fun BonusesPopupWindowDTO.toDomain(): BonusesPopupWindowModel {
    val buttonId = KNOPKA?.ID
    return BonusesPopupWindowModel(
        title = TITLE ?: "",
        coupon = COUPON ?: "",
        description = OPISANIE ?: "",
        warmAboutExpiration = SGORANIE?.KLYCH == "1",
        messageAboutExpiration = SGORANIE?.TITLE ?: "",
        button = KNOPKA?.toDomain() ?: ColorfulButtonModel.Empty,
        url = buttonId?.SSILKA?.toVodovozUrl() ?: "",
        browser = !VodovozBoolean.from(buttonId?.IFRAME).boolean
    )
}

fun KNOPKA_BONUSES_DTO.toDomain(): ColorfulButtonModel {
    return ColorfulButtonModel(
        name = TITLE ?: "",
        backgroundColor = BACKGROUND ?: "",
        textColor = TEXTCOLOR ?: ""
    )
}