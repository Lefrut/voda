package com.m.vodovoz.common.webview.model

sealed interface WebViewEvents {

    data object GoBack: WebViewEvents
}