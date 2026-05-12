package com.m.vodovoz.feature.write_comment.composables

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter

import com.m.vodovoz.R
import com.m.vodovoz.design_system.composables.button.VodovozButton
import com.m.vodovoz.design_system.composables.decoration.VodovozRatingBar
import com.m.vodovoz.design_system.composables.snackbar.VodovozSnackbarHost
import com.m.vodovoz.design_system.composables.text_fields.VodovozTextField
import com.m.vodovoz.design_system.model.widgets.FieldUi

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun WriteCommentBody(
    modifier: Modifier = Modifier,
    productName: String,
    productImage: String,
    commentField: FieldUi,
    rating: Int,
    havePhotos: Boolean,
    images: List<String>,
    buttonIsLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onRatingChange: (Float) -> Unit,
    onCommentChange: (FieldUi) -> Unit,
    onCommentSend: () -> Unit,
    onImagePickerOpen: () -> Unit,
    onImageRemove: (String) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = productImage,
                contentDescription = null,
                modifier = Modifier.size(76.dp),
                contentScale = ContentScale.FillBounds
            )

            Text(
                modifier = Modifier.weight(1f),
                text = productName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp)
            )
        }

        Box(Modifier.padding(top = 24.dp), contentAlignment = Alignment.Center) {
            VodovozRatingBar(
                rating = rating.toFloat(),
                startSize = 24.dp,
                spaceBetween = 8.dp,
                onRatingChange = onRatingChange
            )
        }

        VodovozTextField(
            modifier = Modifier.padding(top = 32.dp, start = 16.dp, end = 16.dp),
            field = commentField,
            onFieldChange = { _, field -> onCommentChange(field) },
            minLines = 2,
            maxLines = 3
        )

        if (havePhotos) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                val maxItemsInRow = 4
                val space = 8.dp
                val imageWidth = (maxWidth + space - 2.dp) / maxItemsInRow - space

                val flowRowItemModifier = Modifier
                    .width(imageWidth)
                    .aspectRatio(0.8f)

                FlowRow(
                    modifier = Modifier,
                    maxItemsInEachRow = maxItemsInRow,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (images.size < 5) {
                        PickPhotoItem(modifier = flowRowItemModifier, onClick = onImagePickerOpen)
                    }

                    images.forEach { image ->
                        key(image) {
                            RemovingImage(
                                modifier = flowRowItemModifier,
                                image = image,
                                onRemove = onImageRemove
                            )
                        }
                    }
                }

            }
        }

        Spacer(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
        )

        VodovozSnackbarHost(hostState = snackbarHostState)

        VodovozButton(
            modifier = Modifier.padding(
                start = 16.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            text = stringResource(id = R.string.leave_rate),
            isLoading = buttonIsLoading,
            onClick = onCommentSend
        )
    }
}

@Composable
private fun PickPhotoItem(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.svg_cam_plus),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            contentScale = ContentScale.Fit
        )

        val bodyMedium = MaterialTheme.typography.bodyMedium
        Text(
            text = stringResource(R.string.photo),
            color = MaterialTheme.colorScheme.primary,
            style = bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.15.sp,
                lineHeight = bodyMedium.fontSize
            )
        )

        Text(
            text = stringResource(R.string.up_to_5_pieces),
            color = MaterialTheme.colorScheme.surfaceTint,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.15.sp)
        )

    }
}

@Composable
private fun RemovingImage(
    modifier: Modifier = Modifier,
    image: String,
    onRemove: (String) -> Unit,
) {
    Box(modifier = modifier.clip(MaterialTheme.shapes.large)) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = rememberAsyncImagePainter(model = image, contentScale = ContentScale.Crop),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_close_circle),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .clickable { onRemove(image) },
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}