package com.vodovoz.app.feature.bottom.services.detail.composables

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import coil3.compose.AsyncImage
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.card.GridProductCard
import com.vodovoz.app.design_system.composables.placeholders.LoadingPlaceholder
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceProductsUi
import com.vodovoz.app.util.extensions.prepareServiceHtml
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ServiceDetailBody(
    modifier: Modifier = Modifier,
    image: String,
    html: String,
    productsSection: ServiceProductsUi?,
    button: ColorfulButtonUi?,
    webViewIsLoading: Boolean,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onAnalogsClick: (ProductUi) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            button?.let {
                VodovozButtonsColumn(
                    modifier = Modifier.padding(16.dp),
                    buttons = listOf(button),
                    onButtonClick = onButtonClick
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            AsyncImage(
                model = image,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
            )

            ServiceDetailsWebView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                html = html,
                onLoadingChange = onLoadingChange
            )

            productsSection?.let {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = productsSection.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall
                )

                FlowRow(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    productsSection.products.forEach { product ->
                        GridProductCard(
                            modifier = Modifier.weight(1f),
                            product = product,
                            onClick = onProductClick,
                            onLike = onProductLike,
                            onAnalogsClick = onAnalogsClick,
                            onIncrementToCart = onIncrementProductToCart,
                            onDecrementToCart = onDecrementProductToCart
                        )
                    }
                    if (productsSection.products.size % 2 == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        if (webViewIsLoading) {
            LoadingPlaceholder(modifier = Modifier.zIndex(1f))
        }
    }


}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ServiceDetailsWebView(
    modifier: Modifier = Modifier,
    html: String,
    onLoadingChange: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val webViewBundle: Bundle = rememberSaveable { bundleOf() }
    var lastProgress by remember { mutableIntStateOf(100) }
    var errorOccurred by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.apply {
                        javaScriptEnabled = true
                        blockNetworkImage = false
                        loadsImagesAutomatically = true
                        useWideViewPort = true
                        loadWithOverviewMode = true
                        layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
                    }

                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            if (errorOccurred) return

                            if (newProgress < 100 && lastProgress == 100) {
                                onLoadingChange(true)
                            } else if (newProgress == 100) {
                                onLoadingChange(false)
                            }
                            lastProgress = newProgress
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            errorOccurred = true
                            onLoadingChange(false)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            if (errorOccurred) {
                                errorOccurred = false
                            }
                        }
                    }
                }
            },
            update = { webView ->
                if (webViewBundle.isEmpty) {
                    onLoadingChange(true)
                    coroutineScope.launch {
                        webView.loadDataWithBaseURL(
                            VodovozWebConfig.VODOVOZ_URL,
                            html.prepareServiceHtml(),
                            "text/html",
                            "utf-8",
                            null
                        )
                    }
                } else {
                    onLoadingChange(true)
                    webView.restoreState(webViewBundle)
                }
            },
            onRelease = { webView ->
                webView.saveState(webViewBundle)
            }
        )
    }
}