package com.m.vodovoz.common.media.model

import android.graphics.Bitmap

sealed interface ImagePickerEvent {
    data class GetBitmap(val imageUri: String) : ImagePickerEvent

    data class SaveInFile(val bitmap: Bitmap): ImagePickerEvent

    data object GoBack: ImagePickerEvent

}