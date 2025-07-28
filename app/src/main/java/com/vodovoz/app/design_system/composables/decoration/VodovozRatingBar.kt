package com.vodovoz.app.design_system.composables.decoration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.gowtham.ratingbar.RatingBar
import com.vodovoz.app.R

@Composable
fun VodovozRatingBar(
    modifier: Modifier = Modifier,
    rating: Float,
    startSize: Dp,
    spaceBetween: Dp,
    onRatingChange: (Float) -> Unit,
) {

    var localRating by rememberSaveable(rating) {
        mutableFloatStateOf(rating)
    }

    RatingBar(
        value = localRating,
        modifier = modifier,
        painterEmpty = painterResource(id = R.drawable.ic_star_inactive),
        painterFilled = painterResource(id = R.drawable.ic_star_active),
        size = startSize,
        spaceBetween = spaceBetween,
        onValueChange = { newRating ->
            localRating = newRating
        },
        onRatingChanged = { newRating ->
            onRatingChange(newRating)
        }
    )
}