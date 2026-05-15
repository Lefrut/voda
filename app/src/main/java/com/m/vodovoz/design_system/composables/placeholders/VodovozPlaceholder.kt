package com.m.vodovoz.design_system.composables.placeholders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.m.vodovoz.R
import com.m.vodovoz.common.model.ButtonAction
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.grid.ProductSectionGrid
import com.m.vodovoz.design_system.model.ProductUi
import com.m.vodovoz.design_system.model.VodovozPlaceholderUi
import com.m.vodovoz.feature.home.composables.ProductSectionRow


@Composable
fun VodovozPlaceholder(
    modifier: Modifier = Modifier,
    data: VodovozPlaceholderUi,
    topContent: @Composable ColumnScope.() -> Unit = {
        Spacer(modifier = Modifier.weight(1f))
    },
    bottomContent: @Composable ColumnScope.() -> Unit = {
        Spacer(modifier = Modifier.weight(1.2f))
    },
    onButtonClick: () -> Unit = {},
    onProductClick: (ProductUi) -> Unit = {},
    onProductLike: (ProductUi) -> Unit = {},
    onIncrementToCart: (ProductUi) -> Unit = {},
    onDecrementToCart: (ProductUi) -> Unit = {},
    onAnalogsClick: (ProductUi) -> Unit = {},
) {
    val productsSection = data.productsSection
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        topContent()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(data.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.FillBounds
            )
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = AnnotatedString.fromHtml(data.headerHtml),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall.copy(textAlign = TextAlign.Center)
            )

            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = AnnotatedString.fromHtml(
                    data.descriptionHtml.replace(
                        "\n",
                        stringResource(R.string.html_br)
                    )
                ),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )

        }


        data.button?.let { button ->
            VodovozButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 32.dp),
                text = button.name,
                onClick = onButtonClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    contentColor = button.textColor,
                    containerColor = button.backgroundColor
                ),
            )
        }

        bottomContent()

        if (productsSection != null && productsSection.items.isNotEmpty()) {
            ProductSectionRow(
                modifier = Modifier.padding(bottom = 16.dp, top = 24.dp),
                sectionProducts = productsSection.copy(button = null),
                onProductLike = onProductLike,
                onProductClick = onProductClick,
                onIncrementToCart = onIncrementToCart,
                onAnalogsClick = onAnalogsClick,
                onShowAllClick = {},
                onDecrementToCart = onDecrementToCart
            )
        }
    }
}
