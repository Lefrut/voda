package com.m.vodovoz.common.media

import android.graphics.Bitmap
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.common.media.model.ImagePickerEvent
import com.m.vodovoz.common.media.model.ImagePickerState
import com.m.vodovoz.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImagePickerViewModel @Inject constructor(
    private val mediaManager: MediaManager,
) : MviViewModel<ImagePickerState, ImagePickerEvent>(ImagePickerState()) {

    fun setImageUri(uri: String) = viewModelScope.launch {
        updateState { s -> s.copy(imageUri = uri) }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(ImagePickerEvent.GoBack)
    }

    fun getBitmapFromImageUri() = viewModelScope.launch {
        sendEvent(ImagePickerEvent.GetBitmap(stateSnapshot.imageUri))
    }

    fun saveBitmapToFile(
        sourceBitmap: Bitmap,
        lensSize: Float,
        containerWidth: Float,
        containerHeight: Float,
        scale: Float,
        offsetX: Float,
        offsetY: Float,
    ) = viewModelScope.launch {
        runCatching {
            val imageWidth = sourceBitmap.width.toFloat()
            val imageHeight = sourceBitmap.height.toFloat()

            if (containerWidth == 0f || imageWidth == 0f) return@launch

            val scaleToFit = containerWidth / imageWidth
            val displayedImageHeight = containerWidth * (imageHeight / imageWidth)
            val verticalPadding = (containerHeight - displayedImageHeight) / 2f

            val cropWidthOnScreen = lensSize / scale
            val cropHeightOnScreen = lensSize / scale

            val cropCenterXOnScreen = containerWidth / 2f - offsetX / scale
            val cropCenterYOnScreen = containerHeight / 2f - offsetY / scale

            val cropLeftOnScreen = cropCenterXOnScreen - cropWidthOnScreen / 2f
            val cropTopOnScreen = cropCenterYOnScreen - cropHeightOnScreen / 2f

            val cropLeftInBitmap = cropLeftOnScreen / scaleToFit
            val cropTopInBitmap = (cropTopOnScreen - verticalPadding) / scaleToFit
            val cropWidthInBitmap = cropWidthOnScreen / scaleToFit
            val cropHeightInBitmap = cropHeightOnScreen / scaleToFit

            val croppedBitmap = Bitmap.createBitmap(
                sourceBitmap,
                cropLeftInBitmap.toInt().coerceIn(0, sourceBitmap.width - 1),
                cropTopInBitmap.toInt().coerceIn(0, sourceBitmap.height - 1),
                cropWidthInBitmap.toInt()
                    .coerceAtMost(sourceBitmap.width - cropLeftInBitmap.toInt()),
                cropHeightInBitmap.toInt()
                    .coerceAtMost(sourceBitmap.height - cropTopInBitmap.toInt())
            )

            sendEvent(ImagePickerEvent.SaveInFile(croppedBitmap))
        }
    }

    fun saveImageFile(file: File) {
        mediaManager.saveAvatarImage(file)
        navigateBack()
    }

}
