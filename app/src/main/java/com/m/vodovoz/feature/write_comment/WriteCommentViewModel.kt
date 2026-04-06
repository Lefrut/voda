package com.m.vodovoz.feature.write_comment

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.common.resources.ContentProvider
import com.m.vodovoz.common.resources.ResourcesProvider
import com.m.vodovoz.design_system.model.toUi
import com.m.vodovoz.design_system.model.widgets.FieldTypeUi
import com.m.vodovoz.design_system.model.widgets.FieldUi
import com.m.vodovoz.design_system.model.widgets.FieldValidationResult
import com.m.vodovoz.design_system.model.widgets.FieldValidator
import com.m.vodovoz.design_system.model.widgets.NoRequiredValidator
import com.m.vodovoz.design_system.model.widgets.checkFields
import com.m.vodovoz.design_system.model.widgets.getErrorText
import com.m.vodovoz.design_system.model.widgets.resetError
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.feature.write_comment.api.WriteCommentNavKey
import com.m.vodovoz.feature.write_comment.model.WriteCommentEvent
import com.m.vodovoz.feature.write_comment.model.WriteCommentState
import com.m.vodovoz.feature.write_comment.model.WriteCommentUiState
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.util.extensions.compressAsJPEG
import com.m.vodovoz.util.extensions.resizeBitmap
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@HiltViewModel(assistedFactory = WriteCommentViewModel.Factory::class)
@Stable
class WriteCommentViewModel @AssistedInject constructor(
    val tabManager: TabManager,
    private val siteStateManager: SiteStateManager,
    private val resourcesProvider: ResourcesProvider,
    private val contentProvider: ContentProvider,
    private val vodovozServiceRepository: VodovozServiceRepository,
    @Assisted private val navKey: WriteCommentNavKey,
) : MviViewModel<WriteCommentState, WriteCommentEvent>(WriteCommentState()) {

    private val productId: Long = navKey.product_id
    private val productName: String = navKey.product_name
    private val productImage: String = navKey.product_image
    private val rating: Int = navKey.rating

    private val commentField = FieldUi(
        id = "comment",
        label = resourcesProvider.getString(R.string.comment),
        value = "",
        keyboardType = KeyboardType.Text,
        isRequired = true,
        isError = false,
        readOnly = false,
        supportingText = resourcesProvider.getString(R.string.minimal_count_15),
        hint = resourcesProvider.getString(R.string.enter_comment),
        type = FieldTypeUi.Text,
        isValueVisible = true
    )
    private val commentValidator = FieldValidator {
        if (it.value.length in 15..1000) {
            FieldValidationResult.VALID
        } else { FieldValidationResult.INVALID }
    }


    init {
        updateState { s ->
            s.copy(
                productImage = productImage,
                rating = rating,
                productName = productName,
                field = commentField
            )
        }


        listenTakePhotos()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun listenTakePhotos() = siteStateManager.siteStateFlow.mapLatest { siteState ->
        updateState { s ->
            s.copy(takePhotos = siteState?.takePhotos ?: false)
        }
    }.launchIn(viewModelScope)

    fun navigateBack() = viewModelScope.launch {
        sendEvent(WriteCommentEvent.GoBack)
    }

    fun changeRating(rating: Float) = viewModelScope.launch {
        updateState { s ->
            s.copy(rating = rating.roundToInt())
        }
    }

    fun writeComment() = viewModelScope.launch {
        val isValid = listOf(stateSnapshot.field).checkFields(
            putErrors = true,
            validators = listOf(NoRequiredValidator, commentValidator),
            getSupportingText = { field ->
                field.getErrorText { resId -> resourcesProvider.getString(resId) }
            }
        ) { updatedFields, _ ->
            val updateField = updatedFields.firstOrNull() ?: return@launch
            updateState { s ->
                s.copy(field = updateField)
            }
        }

        if (!isValid || stateSnapshot.rating == 0) return@launch

        updateState { s ->
            s.copy(buttonIsLoading = true)
        }


        val imageBytesArray = stateSnapshot.imagesUri.mapNotNull { uri ->
            val byteArray = contentProvider.getBytesArray(uri.toUri()) ?: return@mapNotNull null
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            bitmap.resizeBitmap(1080).compressAsJPEG(150_000)
        }

        vodovozServiceRepository.sendComment(
            productId = productId,
            rating = stateSnapshot.rating,
            message = stateSnapshot.field.value,
            imageBytesArray = imageBytesArray
        ).singleResult().onSuccess { placeholder ->
            sendEvent(WriteCommentEvent.SetRatedProductResult(productId))
            updateState { s ->
                s.copy(
                    uiState = WriteCommentUiState.Success(
                        placeholder.toUi()
                    )
                )
            }
        }.onFailure {
            sendEvent(
                WriteCommentEvent.ShowSnackbar(
                    resourcesProvider.getString(R.string.send_cooment_error)
                )
            )
        }

        updateState { s ->
            s.copy(buttonIsLoading = false)
        }

    }

    fun changeComment(field: FieldUi) = viewModelScope.launch {
        updateState { s -> s.copy(field = field.resetError()) }
    }

    fun openImagePicker() = viewModelScope.launch {
        sendEvent(WriteCommentEvent.OpenImagePicker)
    }

    fun addUri(uri: List<@JvmSuppressWildcards Uri>) = viewModelScope.launch {
        updateState { s ->
            val imagesSet = uri.map { it.toString() }.toSet() + s.imagesUri.reversed()
            s.copy(
                imagesUri = imagesSet.take(5)
            )
        }
    }

    fun removeImage(image: String) = viewModelScope.launch {
        updateState { s ->
            s.copy(
                imagesUri = s.imagesUri - image
            )
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(navKey: WriteCommentNavKey): WriteCommentViewModel
    }

}
