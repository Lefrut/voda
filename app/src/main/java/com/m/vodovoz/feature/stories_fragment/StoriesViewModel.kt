package com.m.vodovoz.feature.stories_fragment

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.m.vodovoz.ui.mvi.Event
import com.m.vodovoz.ui.mvi.MviViewModel
import com.m.vodovoz.ui.mvi.State
import kotlinx.coroutines.flow.update
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.model.VodovozAction
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.design_system.model.StoryUi
import com.m.vodovoz.domain.general.respository.UserPreferencesRepository
import com.m.vodovoz.feature.stories_fragment.api.StoriesNavKey
import com.m.vodovoz.ui.insets.InsetsVisibilityState
import com.m.vodovoz.util.extensions.indexOfOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = StoriesViewModel.Factory::class)
@Stable
class StoriesViewModel @AssistedInject constructor(
    savedState: SavedStateHandle,
    val tabManager: TabManager,
    val cookieManager: CookieManager,
    val insetsVisibilityState: InsetsVisibilityState,
    private val userPreferencesRepository: UserPreferencesRepository,
    @Assisted private val navKey: StoriesNavKey?,
) : MviViewModel<StoriesViewModel.HistoriesSliderState, StoriesViewModel.StoriesEvents>(
    HistoriesSliderState()
) {

    private val startStoryId = navKey?.storyId ?: savedState.get<Long>("storyId") ?: 0L
    private val stories: List<StoryUi>? = navKey?.stories ?: savedState.get<List<StoryUi>>("stories")

    init {
        fetchStories()
    }

    private fun fetchStories() = viewModelScope.launch {
        updateState { s ->
            s.copy(uiState = StoriesUiState.Loading)
        }

        if (stories != null) {
            val storyIndex = stories.indexOfOrNull(
                stories.firstOrNull { story -> story.id == startStoryId }
            ) ?: 0


            updateState { s ->
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

        updateState { s ->
            s.copy(storyIsPlay = true)
        }

        while (stateSnapshot.storyIsPlay) {
            val startTime = systemMilliseconds()
            delay(35L)
            updateState { s ->
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
        updateState { s ->
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

        updateState { s ->
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
                updateState { state ->
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
                updateState { state ->
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
                updateState { state ->
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

    @AssistedFactory
    interface Factory {
        fun create(navKey: StoriesNavKey?): StoriesViewModel
    }
}
