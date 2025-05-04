package com.vodovoz.app.feature.about_app.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.vodovoz.app.R

enum class AboutAppOption(
    @DrawableRes val iconId: Int,
    @StringRes val textId: Int
) {
    ContactDevelopers(R.drawable.ic_contact_developers, R.string.contact_developers),
    RateApp(R.drawable.ic_rate_app, R.string.rate_app),
    PrivacyPolicy(R.drawable.ic_privacy_policy, R.string.privacy_policy),
    TermsOfUse(R.drawable.ic_terms_of_use, R.string.public_offer)
}