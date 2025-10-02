package com.m.vodovoz.feature.about_app.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.m.vodovoz.R
import com.m.vodovoz.common.model.GlobalAppLinks


@Immutable
enum class AboutAppOptionUi(
    @DrawableRes val iconId: Int,
) {
    ContactDevelopers(R.drawable.ic_contact_developers) {
        @Composable
        override fun text(): String = stringResource(id = R.string.contact_developers)
    },
    RateApp(R.drawable.ic_rate_app) {
        @Composable
        override fun text(): String = stringResource(id = R.string.rate_app)

    },
    PrivacyPolicy(R.drawable.ic_document) {
        @Composable
        override fun text(): String = GlobalAppLinks.policy.title

    },
    TermsOfUse(R.drawable.ic_document) {
        @Composable
        override fun text(): String = GlobalAppLinks.termsOfUse.title
    },
    PersonalData(R.drawable.ic_document) {
        @Composable
        override fun text(): String = GlobalAppLinks.personal.title
    };


    @Composable
    open fun text(): String {
        return ""
    }
}