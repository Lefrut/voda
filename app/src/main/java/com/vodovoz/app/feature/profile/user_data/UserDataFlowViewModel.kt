package com.vodovoz.app.feature.profile.user_data

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.R
import com.vodovoz.app.common.account.LogoutManager
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.media.MediaManager
import com.vodovoz.app.common.resources.ResourcesProvider
import com.vodovoz.app.design_system.model.widgets.FieldUi
import com.vodovoz.app.design_system.model.widgets.checkFields
import com.vodovoz.app.design_system.model.widgets.mapToDomain
import com.vodovoz.app.design_system.model.widgets.mapToUi
import com.vodovoz.app.design_system.model.widgets.updateFieldAndResetError
import com.vodovoz.app.domain.general.model.exceptions.UserNotLoginException
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
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
) : MviViewModel<UserDataFlowViewModel.UserDataState, UserDataFlowViewModel.UserDataEvents>(
    UserDataState()
) {


    init {
        viewModelScope.launch {
            mediaManager
                .observeAvatarImage()
                .collect { imageFile ->
                    updateUserAvatar(imageFile ?: return@collect)
                    mediaManager.removeAvatarImage()
                }
        }
        fetchUserData()
    }

    fun fetchUserData() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = UserDataUiState.Loading)
        }
        val userDataResult = vodovozServiceRepository.getUserData().singleResult()

        userDataResult.onSuccess { userData ->
            _state.update { s ->
                val photoModel = userData.photo
                s.copy(
                    title = userData.title,
                    fields = userData.fields.mapToUi(),
                    photo = photoModel.imageUrl,
                    photoDescription = photoModel.description,
                    photoTitle = photoModel.title,
                    uiState = UserDataUiState.Success,
                    deleteText = userData.deleteText,
                    deleteTextPrefix = userData.deleteTextPrefix
                )
            }

        }.onFailure { t ->
            if (t is UserNotLoginException && t.placeholder != null) {
                sendEvent(UserDataEvents.RefreshAllAndGoBack)
            } else {
                _state.update { s ->
                    s.copy(uiState = UserDataUiState.Error)
                }
            }
        }
    }


    fun navigateBack() = viewModelScope.launch {
        sendEvent(UserDataEvents.GoBack)
    }

    private fun updateUserAvatar(imageFile: File) = viewModelScope.launch {
        val updateUserAvatarResult =
            vodovozServiceRepository.updateUserAvatar(imageFile).singleResult()
        updateUserAvatarResult.onSuccess { message ->
            _state.update { s ->
                s.copy(photo = imageFile.path)
            }
            sendEvent(UserDataEvents.UpdateProfile)
            sendEvent(UserDataEvents.ShowSnackbar(message))
        }.onFailure {
            val message = it.message ?: return@onFailure
            sendEvent(UserDataEvents.ShowSnackbar(message))
        }
    }

    fun updateUserData() = viewModelScope.launch {
        _state.update { s ->
            s.copy(buttonLoading = true)
        }

        val updateUserDataResult =
            vodovozServiceRepository.updateUserData(stateSnapshot.fields.mapToDomain()).singleResult()
        updateUserDataResult.onSuccess { message ->
            sendEvent(UserDataEvents.UpdateProfile)
            sendEvent(UserDataEvents.ShowSnackbar(message))

        }.onFailure {
            sendEvent(
                UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.update_user_data_error))
            )
        }

        _state.update { s ->
            s.copy(
                buttonEnabled = false,
                buttonLoading = false
            )
        }
    }

    fun logout() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showLogoutDialog = false,
                uiState = UserDataUiState.Loading
            )
        }
        logoutManager.logout().singleResult().onSuccess {
            sendEvent(UserDataEvents.RefreshAllAndGoBack)
        }.onFailure {
            _state.update { s ->
                s.copy(showLogoutDialog = false, uiState = UserDataUiState.Success)
            }
            sendEvent(
                UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.logout_error))
            )
        }
    }

    fun deleteAccount() = viewModelScope.launch {
        _state.update { s ->
            s.copy(
                showDeleteAccountDialog = false,
                uiState = UserDataUiState.Loading
            )
        }

        val deleteAccountResult = vodovozServiceRepository.deleteAccount().singleResult()

        deleteAccountResult.onSuccess {
            logoutManager.logout().singleResult().onSuccess {
                sendEvent(UserDataEvents.RefreshAllAndGoBack)
            }.onFailure {
                sendEvent(UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.logout_after_delete_error)))
            }
        }.onFailure {
            sendEvent(UserDataEvents.ShowSnackbar(resourcesProvider.getString(R.string.delete_account_error)))
        }




        _state.update { s ->
            s.copy(uiState = UserDataUiState.Success)
        }

    }

    fun chooseImage() = viewModelScope.launch {
        sendEvent(UserDataEvents.OpenImagePicker)
    }


    fun showDeleteAccountDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showDeleteAccountDialog = true)
        }
    }

    fun showLogoutDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showLogoutDialog = true)
        }
    }

    fun closeLogoutDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showLogoutDialog = false)
        }

    }

    fun closeDeleteAccountDialog() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showDeleteAccountDialog = false)
        }
    }

    fun checkBirthdayField(field: FieldUi) = viewModelScope.launch {
        if (field.id != "data") return@launch

        _state.update { s ->
            s.copy(showDatePicker = true)
        }
    }

    fun closeDatePicker() = viewModelScope.launch {
        _state.update { s ->
            s.copy(showDatePicker = false)
        }
    }

    fun changeDate(date: LocalDate) = viewModelScope.launch {
        val dateField = stateSnapshot.fields.firstOrNull { it.id == "data" } ?: return@launch
        _state.update { s ->
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

    fun changeField(field: FieldUi, updatedField: FieldUi) = viewModelScope.launch {
        val updatedFields = stateSnapshot.fields.updateFieldAndResetError(field, updatedField)

        updatedFields.checkFields(false) { fields, _ ->
            _state.update { s ->
                s.copy(
                    fields = fields,
                    buttonEnabled = fields.checkFields()
                )
            }
        }
    }


    sealed class UserDataEvents : Event {
        data class ShowSnackbar(val message: String) : UserDataEvents()

        data object UpdateProfile : UserDataEvents()
        data object RefreshAllAndGoBack : UserDataEvents()
        data object GoBack : UserDataEvents()
        data object OpenImagePicker : UserDataEvents()
    }

    @Stable
    sealed interface UserDataUiState {
        data object Loading : UserDataUiState
        data object Error : UserDataUiState
        data object Success : UserDataUiState
    }

    @Immutable
    data class UserDataState(
        val fields: List<FieldUi> = emptyList(),
        val title: String = "",
        val photo: String = "",
        val photoTitle: String = "",
        val photoDescription: String = "",
        val uiState: UserDataUiState = UserDataUiState.Loading,
        val buttonEnabled: Boolean = false,
        val buttonLoading: Boolean = false,
        val showLogoutDialog: Boolean = false,
        val showDeleteAccountDialog: Boolean = false,
        val showDatePicker: Boolean = false,
        val deleteTextPrefix: String = "",
        val deleteText: String = "",
    ) : State

}