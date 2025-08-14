package com.vodovoz.app.feature.stories_fragment

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.vodovoz.app.ui.mvi.Event
import com.vodovoz.app.ui.mvi.MviViewModel
import com.vodovoz.app.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.vodovoz.app.common.model.VodovozAction
import com.vodovoz.app.design_system.model.StoryUi
import com.vodovoz.app.domain.general.respository.UserPreferencesRepository
import com.vodovoz.app.util.extensions.indexOfOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Stable
class StoriesViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val userPreferencesRepository: UserPreferencesRepository,
) : MviViewModel<StoriesViewModel.HistoriesSliderState, StoriesViewModel.StoriesEvents>(
    HistoriesSliderState()
) {

    private val startStoryId = savedState.get<Long>("storyId") ?: 0L
    private val stories: List<StoryUi>? = savedState.get<List<StoryUi>>("stories")

    init {
        fetchStories()
    }

    private fun fetchStories() = viewModelScope.launch {
        _state.update { s ->
            s.copy(uiState = StoriesUiState.Loading)
        }

        if (stories != null) {
            val storyIndex = stories.indexOfOrNull(
                stories.firstOrNull { story -> story.id == startStoryId }
            ) ?: 0


            _state.update { s ->
                s.copy(
                    stories = stories,
                    currentStoryIndex = storyIndex,
                    currentPageIndex = 0,
                    uiState = StoriesUiState.Success,
                    timePassed = 0
                )
            }
            startStory()
        } else {
            delay(500L)
            navigateBack()
        }

        userPreferencesRepository.addViewedStoryId(startStoryId)
    }

    private fun startStory() = viewModelScope.launch {
        fun systemMilliseconds(): Long {
            return System.nanoTime() / 1_000_000
        }

        _state.update { s ->
            s.copy(storyIsPlay = true)
        }

        while (stateSnapshot.storyIsPlay) {
            val startTime = systemMilliseconds()
            delay(35L)
            _state.update { s ->
                s.copy(
                    timePassed = s.timePassed + (systemMilliseconds() - startTime)
                )
            }

            if (stateSnapshot.timePassed >= stateSnapshot.currentStoryPage.durationMillis) {
                goNextStoryPage()
            }
        }
    }

    fun stopStory() = viewModelScope.launch {
        _state.update { s ->
            s.copy(storyIsPlay = false)
        }
    }

    fun resumeStory() = viewModelScope.launch {
        startStory()
    }

    fun navigateBack() = viewModelScope.launch {
        sendEvent(StoriesEvents.GoBack)
    }

    fun changeStoryIndex(currentStoryPage: Int) = viewModelScope.launch {

        if (currentStoryPage == stateSnapshot.currentStoryIndex) return@launch

        _state.update { s ->
            s.copy(
                currentStoryIndex = currentStoryPage,
                currentPageIndex = 0,
                timePassed = 0L,
            )
        }

        startStory()

        stateSnapshot.stories.getOrNull(currentStoryPage)?.let { story ->
            userPreferencesRepository.addViewedStoryId(story.id)
        }
    }

    fun goPreviousStoryPage() = viewModelScope.launch {
        val isFirstPage = stateSnapshot.currentPageIndex == 0
        val isFirstStory = stateSnapshot.currentStoryIndex == 0

        when {
            isFirstPage && isFirstStory -> {
                sendEvent(StoriesEvents.GoBack)
            }

            isFirstPage -> {
                val prevStoryIndex = stateSnapshot.currentStoryIndex - 1
                sendEvent(StoriesEvents.ChangePagerIndex(prevStoryIndex))
            }

            else -> {
                _state.update { state ->
                    state.copy(
                        currentPageIndex = stateSnapshot.currentPageIndex - 1,
                        timePassed = 0L
                    )
                }
            }
        }
    }

    fun goNextStoryPage() = viewModelScope.launch {
        val maxStoryPageIndex = stateSnapshot.currentStory.pages.size - 1
        val nextPageIndex = stateSnapshot.currentPageIndex + 1
        val isLastPage = nextPageIndex > maxStoryPageIndex
        val isLastStory = stateSnapshot.currentStoryIndex >= stateSnapshot.stories.lastIndex

        when {
            isLastPage && isLastStory -> {
                _state.update { state ->
                    state.copy(storyIsPlay = false)
                }
                sendEvent(StoriesEvents.GoBack)
            }

            isLastPage -> {
                sendEvent(
                    StoriesEvents.ChangePagerIndex(stateSnapshot.currentStoryIndex + 1)
                )
            }

            else -> {
                _state.update { state ->
                    state.copy(currentPageIndex = nextPageIndex, timePassed = 0L)
                }
            }
        }
    }

    fun activateButtonAction(action: VodovozAction) {
        viewModelScope.launch {
            sendEvent(StoriesEvents.ActivateAction(action))
        }
    }


    sealed class StoriesEvents : Event {
        data object GoBack : StoriesEvents()
        data class ChangePagerIndex(val newStoryIndex: Int) : StoriesEvents()
        data class ActivateAction(val action: VodovozAction) : StoriesEvents()
    }

    @Stable
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