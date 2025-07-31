package com.vodovoz.app.feature.product_details.detail_media.composable

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gapps.library.api.VideoService
import com.vodovoz.app.core.network.WebConfig
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DetailMediaVideo(
    modifier: Modifier = Modifier,
    videoCode: String,
    isRutube: Boolean,
    onLandscape: () -> Unit,
    onPortrait: () -> Unit,
) {
    val context = LocalContext.current

    val videoService = remember {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        VideoService.build {
            with(context)
            httpClient(okHttpClient)
            enableCache(true)
            enableLog(true)
        }
    }
    val webView = remember {
        WebView(context)
    }

    var customView by remember { mutableStateOf<View?>(null) }

    val coroutineScope = rememberCoroutineScope()


    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            modifier = Modifier.background(MaterialTheme.colorScheme.onBackground),
            factory = {
                webView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = true
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            customView = view
                            onLandscape()
                        }

                        override fun onHideCustomView() {
                            customView = null
                            onPortrait()
                        }
                    }

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        mediaPlaybackRequiresUserGesture = false
                        cacheMode = WebSettings.LOAD_DEFAULT
                    }

                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
                webView
            },
            update = {
                videoService.loadVideoPreview(
                    url = (if (isRutube) WebConfig.RUTUBE_URL else WebConfig.YOUTUBE_URL) + videoCode,
                    onSuccess = { model ->

                        val linkToPlay = model.linkToPlay ?: return@loadVideoPreview

                        webView.updateVideoWebView(
                            landscapeOrientation = false,
                            videoHeight = model.height,
                            videoWidth = model.width
                        )

                        if (webView.url == linkToPlay) return@loadVideoPreview

                        coroutineScope.launch {
                            webView.loadUrl(linkToPlay)
                        }
                    }
                )

            }
        )

        customView?.let { view ->
            AndroidView(
                factory = { view },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


private fun View.updateVideoWebView(
    landscapeOrientation: Boolean,
    videoHeight: Int,
    videoWidth: Int,
) {
    val windowWidth = resources.displayMetrics.widthPixels
    val windowHeight = resources.displayMetrics.heightPixels

    layoutParams = layoutParams.apply {
        width = windowWidth
        height = if (landscapeOrientation) {
            windowHeight
        } else {
            windowWidth * videoHeight / videoWidth.coerceAtLeast(1)
        }
    }
}
