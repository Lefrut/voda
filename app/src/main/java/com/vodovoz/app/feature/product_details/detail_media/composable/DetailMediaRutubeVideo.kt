package com.vodovoz.app.feature.product_details.detail_media.composable

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.gapps.library.api.VideoService
import com.vodovoz.app.core.network.ApiConfig
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DetailMediaRutubeVideo(
    modifier: Modifier = Modifier,
    videoCode: String,
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
        WebView(context).apply {
            setBackgroundColor(Color.Transparent.hashCode())
        }
    }
    val coroutineScope = rememberCoroutineScope()


    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = {
                webView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )


                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean = false

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }



                    webChromeClient = object : WebChromeClient() {

                        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                            onLandscape()
                        }

                        override fun onHideCustomView() {
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
            },
            update = { view ->
                if(videoCode.isEmpty()) return@AndroidView

                videoService.loadVideoPreview(
                    url = ApiConfig.RUTUBE_URL + videoCode,
                    onSuccess = { model ->
                        val linkToPlay = model.linkToPlay ?: return@loadVideoPreview
                        view.updateWebViewSize(
                            false,
                            model.height,
                            model.width
                        )
                        coroutineScope.launch {
                            webView.loadUrl(linkToPlay)
                        }
                    }
                )
            }
        )
    }
}


private fun View.updateWebViewSize(
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
            windowWidth * videoHeight / videoWidth
        }
    }
}
