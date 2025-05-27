package com.vodovoz.app.feature.stories_fragment

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.common.content.Event
import com.vodovoz.app.common.content.PagingContractViewModel
import com.vodovoz.app.common.content.State
import com.vodovoz.app.common.content.updateData
import com.vodovoz.app.data.MainRepository
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.design_system.model.mapToUi
import com.vodovoz.app.domain.general.respository.VodovozServiceRepository
import com.vodovoz.app.ui.model.HistoryUI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoriesViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: MainRepository,
    private val vodovozServiceRepository: VodovozServiceRepository,
) : PagingContractViewModel<StoriesViewModel.HistoriesSliderState, StoriesViewModel.HistoriesSliderEvents>(
    HistoriesSliderState()
) {

    private val startStoryId = savedState.get<Long>("startHistoryId") ?: 0L

    init { fetchStories() }

    private fun fetchStories() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(uiState = StoriesUiState.Loading)
        }

        vodovozServiceRepository.getStories().onEach { storiesResult ->
            val stories = storiesResult.getOrNull()
            if (stories != null) {

                val storyIndex =
                    stories.indexOf(
                        stories.firstOrNull { it.id == startStoryId } ?: run {
                            navigateBack()
                            return@onEach
                        }
                    )

                delay(150)

                uiStateListener.updateData { s ->
                    s.copy(
                        stories = stories.mapToUi(),
                        currentStoryIndex = storyIndex,
                        currentPageIndex = 0,
                        uiState = StoriesUiState.Success,
                        timePassed = 0
                    )
                }
                startStory()
            } else {
                navigateBack()
            }

        }.collect()
    }

    private fun startStory() = viewModelScope.launch {
        fun systemMilliseconds(): Long {
            return System.nanoTime() / 1_000_000
        }

        uiStateListener.updateData { s ->
            s.copy(storyIsPlay = true)
        }

        while (state.data.storyIsPlay) {
            val startTime = systemMilliseconds()
            delay(35L)
            uiStateListener.updateData { s ->
                s.copy(
                    timePassed = s.timePassed + (systemMilliseconds() - startTime)
                )
            }

            val viewState = state.data

            if (viewState.timePassed >= viewState.currentStoryPage.durationMillis) {
                goNextStoryPage()
            }
        }
    }

    fun stopStory() = viewModelScope.launch {
        uiStateListener.updateData { s ->
            s.copy(
                storyIsPlay = false
            )
        }
    }

    fun resumeStory() = viewModelScope.launch {
        startStory()
    }

    fun navigateBack() = viewModelScope.launch {
        eventListener.emit(HistoriesSliderEvents.GoBack)
    }

    fun changeStoryIndex(currentStoryPage: Int) = viewModelScope.launch {
        if (currentStoryPage == state.data.currentStoryIndex) return@launch

        uiStateListener.updateData { s ->
            s.copy(
                currentStoryIndex = currentStoryPage,
                currentPageIndex = 0,
                timePassed = 0L
            )
        }
    }

    fun goPreviousStoryPage() = viewModelScope.launch {
        val isFirstPage = dataState.currentPageIndex == 0
        val isFirstStory = dataState.currentStoryIndex == 0

        when {
            isFirstPage && isFirstStory -> {
                eventListener.emit(HistoriesSliderEvents.GoBack)
            }
            isFirstPage -> {
                val prevStoryIndex = dataState.currentStoryIndex - 1
                eventListener.emit(HistoriesSliderEvents.ChangePagerIndex(prevStoryIndex))
            }
            else -> {
                uiStateListener.updateData { state ->
                    state.copy(
                        currentPageIndex = dataState.currentPageIndex - 1,
                        timePassed = 0L
                    )
                }
            }
        }
    }

    fun goNextStoryPage() = viewModelScope.launch {
        val maxStoryPageIndex = dataState.currentStory.pages.size - 1
        val nextPageIndex = dataState.currentPageIndex + 1
        val isLastPage = nextPageIndex > maxStoryPageIndex
        val isLastStory = dataState.currentStoryIndex >= dataState.stories.lastIndex

        when {
            isLastPage && isLastStory -> {
                uiStateListener.updateData { state ->
                    state.copy(storyIsPlay = false)
                }
                eventListener.emit(HistoriesSliderEvents.GoBack)
            }
            isLastPage -> {
                eventListener.emit(
                    HistoriesSliderEvents.ChangePagerIndex(dataState.currentStoryIndex + 1)
                )
            }
            else -> {
                uiStateListener.updateData { state ->
                    state.copy(currentPageIndex = nextPageIndex, timePassed = 0L)
                }
            }
        }
    }


    sealed class HistoriesSliderEvents : Event {
        data object GoToProfile : HistoriesSliderEvents()

        data object GoBack : HistoriesSliderEvents()

        data class ChangePagerIndex(val newStoryIndex: Int) : HistoriesSliderEvents()
    }

    @Immutable
    sealed class StoriesUiState {
        data object Loading : StoriesUiState()
        data object Success : StoriesUiState()
    }

    @Immutable
    data class HistoriesSliderState(
        val stories: List<StoryUi> = listOf(),
        val currentStoryIndex: Int = 0,
        val currentPageIndex: Int = 0,
        val uiState: StoriesUiState = StoriesUiState.Loading,
        val timePassed: Long = 0L,
        val storyIsPlay: Boolean = false,
    ) : State {

        val currentStory get() = stories[currentStoryIndex]
        val currentStoryPage get() = currentStory.pages[currentPageIndex]

    }
}