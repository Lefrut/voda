package com.m.vodovoz.feature.write_comment.model


sealed class WriteCommentEvent {

    data object GoBack: WriteCommentEvent()

    data object OpenImagePicker: WriteCommentEvent()
    data class SetRatedProductResult(val productId: Long): WriteCommentEvent()
    data class ShowSnackbar(val message: String): WriteCommentEvent()

}