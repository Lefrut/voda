package com.m.vodovoz.feature.search.qrcode

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.m.vodovoz.domain.general.model.exceptions.EmptyResultException
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
@Stable
class QrCodeViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : MviViewModel<QrCodeViewModel.QrCodeState, QrCodeViewModel.QrCodeEvents>(QrCodeState()) {

    private val mutex = Mutex()

    fun searchByBarCode(barCode: String) = viewModelScope.launch {
        if (!mutex.tryLock()) return@launch

        updateState { s ->
            s.copy(barCode = barCode)
        }

        val barCodeProductsResult =
            vodovozServiceRepository.getBarCodeProducts(barCode).singleResult()

        barCodeProductsResult.onSuccess { products ->
            updateState { s ->
                s.copy(uiState = QrCodeUiState.Scanner)
            }

            val product = products.firstOrNull()
            if (products.size == 1 && product != null) {
                sendEvent(QrCodeEvents.GoToProductDetails(product.id))
            } else if (products.size > 1) {
                sendEvent(QrCodeEvents.GoToSearchProducts(barCode))
            }
        }.onFailure { t ->

            if (t is EmptyResultException && t.placeholder != null) {
                val errorModel = t.placeholder

                updateState { s ->
                    s.copy(
                        uiState = QrCodeUiState.EmptyResult(
                            errorModel.headerHtml,
                            errorModel.descriptionHtml,
                            errorModel.imageUrl
                        )
                    )
                }
            }

        }
    }.invokeOnCompletion {
        if (mutex.isLocked) {
            mutex.unlock()
        }
    }

    fun switchFlashOn() = viewModelScope.launch {
        updateState { s ->
            s.copy(
                flashOn = !s.flashOn
            )
        }
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(QrCodeEvents.GoBack)
    }

    fun setScannerState() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = QrCodeUiState.Scanner)
        }
    }


    @Immutable
    data class QrCodeState(
        val flashOn: Boolean = false,
        val barCode: String = "",
        val uiState: QrCodeUiState = QrCodeUiState.Scanner,
    ) : State

    @Stable
    sealed interface QrCodeUiState {

        data object Scanner : QrCodeUiState
        data class EmptyResult(
            val title: String,
            val description: String,
            val imageUrl: String,
        ) : QrCodeUiState

    }

    sealed class QrCodeEvents : Event {
        data class Success(val id: String) : QrCodeEvents()
        data class GoToProductDetails(val id: Long) : QrCodeEvents()
        data class GoToSearchProducts(val barCode: String) : QrCodeEvents()
        data object GoBack : QrCodeEvents()
    }
}