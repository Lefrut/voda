package com.vodovoz.app.common.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaManager @Inject constructor() {

    private val avatarImageListener = MutableStateFlow<File?>(null)

    fun observeAvatarImage(): StateFlow<File?> = avatarImageListener.asStateFlow()

    fun saveAvatarImage(imageFile: File) {
        avatarImageListener.value = imageFile
    }

    fun removeAvatarImage() {
        avatarImageListener.value = null
    }
}
