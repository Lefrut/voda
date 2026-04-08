package com.m.vodovoz.feature.auth.login_by_email.api

import androidx.compose.runtime.Immutable
import com.m.vodovoz.core.navigation.VodovozNavKey
import com.m.vodovoz.core.navigation.viewmodel.SharedViewModelStoreNavKey

@Immutable
data class LoginByEmailNavKey(
    val accountTypeId: String? = null,
    val source: Source = Source.None,
    override val parentContentKey: String? = null,
) : VodovozNavKey, SharedViewModelStoreNavKey {
    override fun toString(): String = parentContentKey
        ?: "LoginByEmailNavKey(accountTypeId=$accountTypeId, source=$source)"

    enum class Source {
        None,
        Login,
    }

    companion object {
        const val NAV_NAME: String = "feature/auth/login_by_email/LoginByEmail"
    }
}
