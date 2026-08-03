package com.m.vodovoz.feature.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.domain.general.model.promotion.FloatingPromoButtonModel
import com.m.vodovoz.domain.general.respository.VodovozServiceRepository
import com.m.vodovoz.util.extensions.singleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val vodovozServiceRepository: VodovozServiceRepository,
) : ViewModel() {

    var isBottomBarInitialized = false

    private val _floatingPromoState = MutableStateFlow(FloatingPromoState())
    val floatingPromoState = _floatingPromoState.asStateFlow()

    init {
        fetchFloatingPromoButton()
    }

    fun fetchFloatingPromoButton() = viewModelScope.launch {
        _floatingPromoState.update { it.copy(isLoading = true, error = null) }

        vodovozServiceRepository.getFloatingPromoButtons().singleResult()
            .onSuccess { buttons ->
                val button = buttons.firstOrNull()
                _floatingPromoState.update {
                    it.copy(
                        button = button,
                        isLoading = false,
                        error = null,
                        rightScreenNames = button?.rightScreenNames.orEmpty(),
                    )
                }
            }
            .onFailure { throwable ->
                _floatingPromoState.update {
                    it.copy(
                        button = null,
                        isLoading = false,
                        error = throwable,
                        rightScreenNames = emptySet(),
                    )
                }
            }
    }

    data class FloatingPromoState(
        val button: FloatingPromoButtonModel? = null,
        val isLoading: Boolean = true,
        val error: Throwable? = null,
        val rightScreenNames: Set<String> = emptySet(),
    )
}
