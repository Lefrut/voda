package com.vodovoz.app.feature.profile.userdata

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.common.logout.LogoutManager
import com.vodovoz.app.common.media.MediaManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.domain.general.model.UserNotLoginException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.feature.preorder.model.FieldUi
import com.vodovoz.app.feature.preorder.model.checkFields
import com.vodovoz.app.feature.preorder.model.mapToDomain
import com.vodovoz.app.feature.preorder.model.toUi
import com.vodovoz.app.feature.preorder.model.updateFieldAndResetError
import com.vodovoz.app.feature.preorder.model.updateFieldValueAndResetError
import com.vodovoz.app.ui.model.UserDataUI
import com.vodovoz.app.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Stable
@HiltViewModel
class UserDataFlowViewModel @Inject constructor(
    private val mediaManager: MediaManager,
    private val vodovozServiceRepository: VodovozServiceRepository,
    private val resourcesProvider: ResourcesProvider,
    private val logoutManager: LogoutManager,
) : PagingContractViewModel<UserDataFlowViewModel.UserDataState, UserDataFlowViewModel.UserDataEvents>(
    UserDataState()
) {


    init {
        viewModelScope.launch {
            mediaManager
                .observeAvatarImage()
                .collect { imageFile ->
                    imageFile ?: return@collect

                    updateUserAvatar(imageFile)
                    mediaManager.removeAvatarImage()
                }
        }
    }

    fun fetchUserData() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = UserDataUiState.Loading)
        }
        val userDataResult = vodovozServiceRepository.getUserData().singleResult()

        userDataResult.onSuccess { userData ->
            uiStateListener.updateData { s ->
                val photoModel = userData.photo
                s.copy(
                    title = userData.title,
                    fields = userData.fields.map { field -> field.toUi() },
                    photo = photoModel.imageUrl,
                    photoDescription = photoModel.description,
                    photoTitle = photoModel.title,
                    uiState = UserDataUiState.Success
                )
            }

        }.onFailure { t ->
            if (t is UserNotLoginException && t.placeholder != null) {
                eventListener.emit(UserDataEvents.GoBack)
            } else {
                uiStateListener.updateData { s ->
                    s.copy(uiState = UserDataUiState.Error)
                }
            }
        }
    }


    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(UserDataEvents.GoBack)
    }

    fun changeFieldValue(field: FieldUi, newValue: String) = viewModelScope.launch {
        val updatedFields = dataState.fields.updateFieldValueAndResetError(field, newValue)

        updatedFields.checkFields(false) { fields, isValid ->
            uiStateListener.updateData { s ->
                s.copy(
                    fields = fields,
                    buttonEnabled = fields.checkFields()
                )
            }
        }
    }

    private fun updateUserAvatar(imageFile: File) = viewModelScope.launch {
        val updateUserAvatarResult =
            vodovozServiceRepository.updateUserAvatar(imageFile).singleResult()
        updateUserAvatarResult.onSuccess { message ->
            uiStateListener.updateData { s ->
                s.copy(photo = imageFile.path)
            }
            eventListener.emit(UserDataEvents.UpdateProfile)
            eventListener.emit(UserDataEvents.ShowSnackbar(message))
        }.onFailure {
            val message = it.message ?: return@onFailure
            eventListener.emit(UserDataEvents.ShowSnackbar(message))
        }
    }

    fun updateUserData() = viewModelScope.launch {
        val updateUserDataResult =
            vodovozServiceRepository.updateUserData(dataState.fields.mapToDomain()).singleResult()
        updateUserDataResult.onSuccess { message ->
            eventListener.emit(UserDataEvents.UpdateProfile)
            eventListener.emit(UserDataEvents.ShowSnackbar(message))
            uiStateListener.updateData { s ->
                s.copy(buttonEnabled = false)
            }
        }.onFailure {
            eventListener.emit(
                UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.update_user_data_error))
            )
            uiStateListener.updateData { s ->
                s.copy(buttonEnabled = false)
            }
        }
    }

    fun logout() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                showLogoutDialog = false,
                uiState = UserDataUiState.Loading
            )
        }
        logoutManager.logout().singleResult().onSuccess {
            eventListener.emit(UserDataEvents.RefreshAllAndGoBack)
        }.onFailure {
            eventListener.emit(
                UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.logout_error))
            )
        }

        uiStateListener.updateData { s ->
            s.copy(
                showLogoutDialog = false,
                uiState = UserDataUiState.Success
            )
        }

    }

    fun deleteAccount() = viewModelScope.launch {
        //todo - make delete
    }

    fun chooseImage() = viewModelScope.launch {
        eventListener.emit(UserDataEvents.OpenImagePicker)
    }


    fun showDeleteAccountDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showDeleteAccountDialog = true)
        }
    }

    fun showLogoutDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showLogoutDialog = true)
        }
    }

    fun closeLogoutDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showLogoutDialog = false)
        }

    }

    fun closeDeleteAccountDialog() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showDeleteAccountDialog = false)
        }
    }

    fun checkBirthdayField(field: FieldUi) = viewModelScope.launch {
        if (field.id != "data") return@launch

        uiStateListener.updateData { s ->
            s.copy(showDatePicker = true)
        }
    }

    fun closeDatePicker() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(showDatePicker = false)
        }
    }

    fun changeDate(date: LocalDate) = viewModelScope.launch {
        val dateField = dataState.fields.firstOrNull { it.id == "data" } ?: return@launch
        uiStateListener.updateData { s ->
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val formattedDate = date.format(formatter)
            val updatedFields = s.fields.updateFieldAndResetError(
                dateField,
                dateField.copy(value = formattedDate)
            )

            s.copy(
                fields = updatedFields,
                showDatePicker = false,
                buttonEnabled = updatedFields.checkFields()
            )
        }
    }


    sealed class UserDataEvents : Event {
        data class ShowSnackbar(val message: String) : UserDataEvents()

        data object UpdateProfile : UserDataEvents()
        data object RefreshAllAndGoBack : UserDataEvents()
        data object GoBack : UserDataEvents()
        data object OpenImagePicker : UserDataEvents()
    }

    sealed interface UserDataUiState {
        data object Loading : UserDataUiState
        data object Error : UserDataUiState
        data object Success : UserDataUiState
    }

    @Immutable
    data class UserDataState(
        val item: UserDataUI? = null,
        val canChangeBirthDay: Boolean = true,
        val showPassword: Boolean = false,

        val fields: List<FieldUi> = emptyList(),
        val title: String = "",
        val photo: String = "",
        val photoTitle: String = "",
        val photoDescription: String = "",
        val uiState: UserDataUiState = UserDataUiState.Loading,
        val buttonEnabled: Boolean = false,
        val showLogoutDialog: Boolean = false,
        val showDeleteAccountDialog: Boolean = false,
        val showDatePicker: Boolean = false,
    ) : State

}