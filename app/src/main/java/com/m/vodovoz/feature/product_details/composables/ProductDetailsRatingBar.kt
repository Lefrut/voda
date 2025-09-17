package com.m.vodovoz.feature.product_details.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.m.vodovoz.R
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.util.extensions.formatRating

@Composable
fun ProductDetailsRatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    numberOfReviews: Int,
    articleNumber: String,
    onCopyClick: () -> Unit,
    onReviewsClick: () -> Unit,
    onZeroReviewsClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatRating(rating),
            color = if (rating <= 0f) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            modifier = Modifier.padding(horizontal = 4.dp),
            text = stringResource(R.string.middle_dot),
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.titleMedium.copy(
                lineHeightStyle = LineHeightStyle(
                    LineHeightStyle.Alignment.Top,
                    LineHeightStyle.Trim.FirstLineTop
                )
            )
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_star_active),
            contentDescription = null,
            tint = if (rating <= 0f) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier
                .size(16.dp),
        )

        val hasReviews = numberOfReviews > 0

        Text(
            modifier = Modifier
                .padding(start = 16.dp)
                .clickable(
                    null,
                    null,
                    onClick = {
                        if (hasReviews) onReviewsClick()
                        else onZeroReviewsClick()
                    }
                ),
            text = if (!hasReviews) stringResource(R.string.leave_feedback) else pluralStringResource(
                id = R.plurals.reviews_count,
                count = numberOfReviews,
                numberOfReviews
            ),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .clickable(onClick = onCopyClick)
        ) {
            Text(
                text = stringResource(R.string.article_number, articleNumber),
                color = MaterialTheme.colorScheme.surfaceTint,
                style = MaterialTheme.typography.labelSmall
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_copy),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp),
            )
        }


    }
}

@Preview
@Composable
private fun ProductDetailsRatingBarPreview() {
    VodovozTheme {
        ProductDetailsRatingBar(
            rating = 4.3f,
            numberOfReviews = 0,
            articleNumber = "123456789",
            onReviewsClick = {},
            onCopyClick = {},
            onZeroReviewsClick = {}
        )
    }
}