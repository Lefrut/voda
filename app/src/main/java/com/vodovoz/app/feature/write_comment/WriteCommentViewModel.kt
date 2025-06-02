package com.vodovoz.app.feature.write_comment

import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.resetError
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.feature.write_comment.model.WriteCommentEvent
import com.vodovoz.app.feature.write_comment.model.WriteCommentState
import com.vodovoz.app.ui.mvi.MviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
@Stable
class WriteCommentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val siteStateManager: SiteStateManager,
    private val resourcesProvider: ResourcesProvider,
) : MviViewModel<WriteCommentState, WriteCommentEvent>(WriteCommentState()) {

    private val productId: Long = savedStateHandle["product_id"] ?: navigateBack().run { -1L }
    private val productName: String = savedStateHandle["product_name"] ?: ""
    private val productImage: String = savedStateHandle["product_image"] ?: ""
    private val rating: Int = savedStateHandle["rating"] ?: 0

    private val commentField = FieldUi(
        id = "",
        label = resourcesProvider.getString(R.string.comment),
        value = "",
        keyboardType = KeyboardType.Text,
        isRequired = false,
        isError = false,
        readOnly = false,
        supportingText = resourcesProvider.getString(R.string.minimal_count_15),
        hint = resourcesProvider.getString(R.string.enter_comment)
    )


    init {
        _state.update { s ->
            s.copy(
                productImage = productImage,
                rating = rating,
                productName = productName,
                field = commentField
            )
        }


        listenHavePhotos()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenHavePhotos() = siteStateManager.siteStateFlow.mapLatest { siteState ->
        _state.update { s ->
            s.copy(havePhotos = siteState?.showComments ?: false)
        }
    }.launchIn(viewModelScope)

    fun navigateBack() = viewModelScope.launch {
        _events.emit(WriteCommentEvent.GoBack)
    }

    fun changeRating(rating: Float) = viewModelScope.launch {
        _state.update { s ->
            s.copy(rating = rating.roundToInt())
        }
    }

    fun writeComment() = viewModelScope.launch {

    }

    fun changeComment(field: FieldUi) = viewModelScope.launch {
        _state.update { s -> s.copy(field = field.resetError()) }
    }

    fun openImagePicker() = viewModelScope.launch {
        _events.emit(WriteCommentEvent.OpenImagePicker)
    }

    fun addUri(uri: List<@JvmSuppressWildcards Uri>) = viewModelScope.launch {
        _state.update { s ->
            val imagesSet = uri.map { it.toString() }.toSet() + s.imagesUri.reversed()
            s.copy(
                imagesUri = imagesSet.take(5)
            )
        }
    }

    fun removeImage(image: String) = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                imagesUri = s.imagesUri - image
            )
        }
    }

//    fun prepareImagesForUpload(context: Context, uris: List<Uri>): List<MultipartBody.Part> {
//        return uris.mapIndexedNotNull { index, uri ->
//            val inputStream = context.contentResolver.openInputStream(uri) ?: return@mapIndexedNotNull null
//            val fileBytes = inputStream.readBytes()
//            val requestFile = fileBytes.toRequestBody("image/*".toMediaTypeOrNull())
//            MultipartBody.Part.createFormData(
//                name = "images[$index]",
//                filename = "photo_$index.jpg",
//                body = requestFile
//            )
//        }
//    }

}