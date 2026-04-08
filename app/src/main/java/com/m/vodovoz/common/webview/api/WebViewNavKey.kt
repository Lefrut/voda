package com.m.vodovoz.common.webview.api

import com.m.vodovoz.core.navigation.VodovozNavKey

data class WebViewNavKey(
    val url: String,
    val title: String = "",
) : VodovozNavKey {
    companion object {
        const val NAV_NAME: String = "common/webview/WebView"
    }
}
