package com.vodovoz.app.feature.write_comment.model


sealed class WriteCommentEvent() {

    data object GoBack: WriteCommentEvent()

    data object OpenImagePicker: WriteCommentEvent()

}