package com.m.vodovoz.common.webview.api

import androidx.navigation3.runtime.NavKey

data class WebViewNavKey(
    val url: String,
    val title: String = "",
) : NavKey {
    companion object {
        const val NAV_NAME: String = "common/webview/WebView"
    }
}
