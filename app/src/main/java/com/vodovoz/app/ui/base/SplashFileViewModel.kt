package com.vodovoz.app.ui.base

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.base.model.SplashFileState
import com.vodovoz.app.util.SplashFileConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashFileViewModel @Inject constructor(
    private val appContext: Application,
) : ViewModel() {


    private val _fileState = MutableStateFlow<SplashFileState>(SplashFileState.Loading)
    val fileState = _fileState.asStateFlow()


    fun downloadSplashFile() = viewModelScope.launch {
        _fileState.update { SplashFileState.Loading }
        SplashFileConfig.downloadSplashFile(appContext).onSuccess {
            _fileState.update { SplashFileState.Success }
        }.onFailure {
            println(it)
            _fileState.update { SplashFileState.Error }
        }
    }
}