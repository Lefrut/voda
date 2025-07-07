package com.vodovoz.app.feature.wait_feedback_products.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import coil3.compose.AsyncImage

import coil3.request.crossfade
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.StepSize
import com.vodovoz.app.R
import com.vodovoz.app.feature.wait_feedback_products.model.WaitFeedbackProductUi
import kotlin.math.roundToInt

@Suppress("NonSkippableComposable")
@Composable
fun WaitFeedbackProductsBody(
    modifier: Modifier = Modifier,
    products: List<WaitFeedbackProductUi>,
    loadStates: CombinedLoadStates,
    onProductClick: (WaitFeedbackProductUi) -> Unit,
    onProductRatingChange: (WaitFeedbackProductUi, Int) -> Unit,
    onProductSee: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
    ) {
        itemsIndexed(
            items = products,
            key = { _, item ->
                item.id
            }
        ) { index, item ->
            Column{
                WaitFeedbackProductUi(
                    waitFeedbackProduct = item,
                    onClick = onProductClick,
                    onRatingChange = onProductRatingChange
                )

                if (index != products.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            SideEffect {
                onProductSee(index)
            }
        }

        item {
            if (loadStates.append is LoadState.Loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp),
                    trackColor = Color.Transparent,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun WaitFeedbackProductUi(
    modifier: Modifier = Modifier,
    waitFeedbackProduct: WaitFeedbackProductUi,
    onClick: (WaitFeedbackProductUi) -> Unit,
    onRatingChange: (WaitFeedbackProductUi, Int) -> Unit,
) {
    Row(
        modifier = modifier.clickable(
            onClick = { onClick(waitFeedbackProduct) },
            interactionSource = null,
            indication = null
        )
    ) {
        AsyncImage(
            model = waitFeedbackProduct.image,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(76.dp)
        )

        Column {
            Text(
                text = waitFeedbackProduct.name,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall
            )


            var rating by remember {
                mutableFloatStateOf(0f)
            }

            RatingBar(
                modifier = Modifier.padding(top = 16.dp),
                value = rating,
                painterEmpty = painterResource(id = R.drawable.ic_star_inactive),
                painterFilled = painterResource(id = R.drawable.ic_star_active),
                size = 24.dp,
                stepSize = StepSize.ONE,
                onValueChange = { newRating ->
                    rating = newRating
                },
                onRatingChanged = { newRating ->
                    onRatingChange(waitFeedbackProduct, newRating.roundToInt())
                }
            )
        }
    }
}