package com.vodovoz.app.feature.profile.notification_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationSettingsFragment : Fragment() {

    private val viewModel: NotificationSettingsViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onStart() {
        super.onStart()
        tabManager.changeTabVisibility(false)
    }

    override fun onStop() {
        super.onStop()
        tabManager.changeTabVisibility(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)

            setContent {
                VodovozTheme {
                    val pagingState by viewModel.observeUiState().collectAsStateWithLifecycle()
                    val viewState by rememberUpdatedState(newValue = pagingState.data)

                    NotificationSettingsScreen(viewModel = viewModel, viewState = viewState)

                    LifecycleEffect {
                        viewModel.observeEvent().collect { event ->
                            when(event){
                                is NotificationSettingsViewModel.NotSettingsEvents.Failure -> {

                                }
                                is NotificationSettingsViewModel.NotSettingsEvents.Success -> {

                                }
                                NotificationSettingsViewModel.NotSettingsEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}