package com.vodovoz.app.feature.write_comment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.write_comment.composables.WriteCommentBody
import com.vodovoz.app.feature.write_comment.model.WriteCommentState

@Composable
fun WriteCommentScreen(viewModel: WriteCommentViewModel, viewState: WriteCommentState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        VodovozTopBar(
            onBack = { viewModel.navigateBack() },
            title = stringResource(R.string.new_comment)
        )

        WriteCommentBody(
            productName = viewState.productName,
            productImage = viewState.productImage,
            rating = viewState.rating,
            buttonIsLoading = viewState.buttonIsLoading,
            commentField = viewState.field,
            havePhotos = viewState.havePhotos,
            images = viewState.imagesUri,
            onRatingChange = { rating ->
                viewModel.changeRating(rating)
            },
            onCommentChange = { field ->
                viewModel.changeComment(field)
            },
            onRateSend = {
                viewModel.writeComment()
            },
            onImagePickerOpen = {
                viewModel.openImagePicker()
            },
            onImageRemove = { image ->
                viewModel.removeImage(image)
            }
        )
    }
}