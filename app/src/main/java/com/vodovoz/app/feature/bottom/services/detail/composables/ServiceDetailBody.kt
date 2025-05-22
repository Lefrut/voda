package com.vodovoz.app.feature.bottom.services.detail.composables

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebSettings.LOAD_CACHE_ELSE_NETWORK
import android.webkit.WebSettings.LOAD_CACHE_ONLY
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.core.network.VodovozWebConfig
import com.vodovoz.app.design_system.composables.button.VodovozButtonsColumn
import com.vodovoz.app.design_system.composables.card.GridProductCard
import com.vodovoz.app.design_system.model.ColorfulButtonUi
import com.vodovoz.app.design_system.model.ProductUi
import com.vodovoz.app.feature.bottom.services.detail.model.ServiceProductsUi
import com.vodovoz.app.util.extensions.prepareServiceHtml

@SuppressLint("SetJavaScriptEnabled")
@Suppress("NonSkippableComposable")
@Composable
fun ServiceDetailBody(
    modifier: Modifier = Modifier,
    image: String,
    html: String,
    productsSection: ServiceProductsUi?,
    button: ColorfulButtonUi?,
    onButtonClick: (ColorfulButtonUi) -> Unit,
    onProductLike: (ProductUi) -> Unit,
    onProductClick: (ProductUi) -> Unit,
    onIncrementProductToCart: (ProductUi) -> Unit,
    onDecrementProductToCart: (ProductUi) -> Unit,
    onAnalogsClick: (ProductUi) -> Unit,
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
            if (image.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(model = image, contentScale = ContentScale.Crop),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                            cacheMode = LOAD_CACHE_ONLY
                        }

                    }

                },
                update = { webView ->
                    webView.loadDataWithBaseURL(
                        ApiConfig.VODOVOZ_URL,
                        html.prepareServiceHtml(),
                        "text/html",
                        "utf-8",
                        null
                    )
                },
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
    }
}