package com.vodovoz.app.feature.profile.user_data

import com.vodovoz.app.design_system.composables.date_picker.VodovozCalendarDialog
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.vodovoz.app.R
import com.vodovoz.app.design_system.composables.dialogs.VodovozDialog
import com.vodovoz.app.design_system.composables.snackbar.VodovozSnackbarHost
import com.vodovoz.app.design_system.composables.top_bar.VodovozTopBar
import com.vodovoz.app.feature.profile.user_data.composables.UserDataBody
import com.vodovoz.app.util.formatters.VodovozDateFormatters
import java.time.LocalDate

@Composable
fun UserDataScreen(
    viewModel: UserDataFlowViewModel,
    viewState: UserDataFlowViewModel.UserDataState,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            VodovozTopBar(
                onBack = { viewModel.navigateBack() },
                title = viewState.title,
                actionIconId = R.drawable.ic_logout,
                onActionClick = {
                    viewModel.showLogoutDialog()
                }
            )
        },
        snackbarHost = {
            VodovozSnackbarHost(hostState = snackbarHostState)
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        UserDataBody(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            fields = viewState.fields,
            photoTitle = viewState.photoTitle,
            photo = viewState.photo,
            photoDescription = viewState.photoDescription,
            buttonEnabled = viewState.buttonEnabled,
            onSaveDataClick = {
                viewModel.updateUserData()
            },
            onDeleteAccountClick = {
                viewModel.showDeleteAccountDialog()
            },
            onAvatarClick = {
                viewModel.chooseImage()
            },
            onFieldClick = { field ->
                viewModel.checkBirthdayField(field)
            },
            onFieldChange = { field, updatedField ->
                viewModel.changeField(field, updatedField)
            }
        )
    }


    if (viewState.showLogoutDialog) {
        VodovozDialog(
            title = stringResource(id = R.string.exit),
            description = stringResource(id = R.string.exit_confirmation),
            acceptButtonText = stringResource(id = R.string.exit).uppercase(),
            cancelButtonText = stringResource(id = R.string.cancel).uppercase(),
            onDismiss = {
                viewModel.closeLogoutDialog()
            },
            onAccept = {
                viewModel.logout()
            }
        )
    }

    if (viewState.showDeleteAccountDialog) {
        VodovozDialog(
            title = stringResource(id = R.string.delete_account),
            description = stringResource(id = R.string.delete_account_confirmation),
            acceptButtonText = stringResource(id = R.string.delete).uppercase(),
            cancelButtonText = stringResource(id = R.string.cancel).uppercase(),
            onDismiss = {
                viewModel.closeDeleteAccountDialog()
            },
            onAccept = {
                viewModel.deleteAccount()
            }
        )
    }

    if (viewState.showDatePicker) {
        val dateString = viewState.fields.firstOrNull { it.id == "data" }?.value ?: ""

        val currentDay = rememberSaveable(dateString) {
            val localDate = runCatching {
                LocalDate.parse(dateString, VodovozDateFormatters.DMY)
            }.getOrNull() ?: LocalDate.now().minusYears(30)

            return@rememberSaveable CalendarDay(localDate, DayPosition.MonthDate)
        }

        val today = remember { LocalDate.now() }

        VodovozCalendarDialog(
            initialDate = currentDay,
            isSelectableDate = { currentDate ->
                currentDate < today
            },
            onDateSelect = { selectedDate ->
                viewModel.changeDate(selectedDate)
            },
            onDismiss = {
                viewModel.closeDatePicker()
            }
        )
    }
}
