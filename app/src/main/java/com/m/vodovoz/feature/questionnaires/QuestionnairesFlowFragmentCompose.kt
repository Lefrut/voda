package com.m.vodovoz.feature.questionnaires

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.m.vodovoz.ui.mvi.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.core.network.VodovozWebConfig
import com.m.vodovoz.core.network.interceptor.BaseUrlInterceptor
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.dialogs.VodovozDialog
import com.m.vodovoz.design_system.composables.placeholders.LoadingPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.composables.placeholders.VodovozLongPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.feature.sitestate.SiteStateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuestionnairesFlowFragment : Fragment() {

    private val viewModel: QuestionnairesFlowViewModel by viewModels()

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
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.collectAsState()
                    
                    val scrollState = rememberScrollState()
                    val snackbarHostState = remember { SnackbarHostState() }

                    when (val uiState = viewState.uiState) {
                        QuestionnairesFlowViewModel.QuestionnairesUiState.Body -> {
                            QuestionnairesScreen(
                                viewModel = viewModel,
                                viewState = viewState,
                                scrollState = scrollState,
                                snackbarHostState = snackbarHostState
                            )
                        }

                        QuestionnairesFlowViewModel.QuestionnairesUiState.Loading -> {
                            LoadingPlaceholder()
                        }

                        is QuestionnairesFlowViewModel.QuestionnairesUiState.Success -> {
                            VodovozLongPlaceholder(
                                data = uiState.placeholder,
                                onCloseClick = {
                                    viewModel.navigateBack()
                                },
                                onButtonClick = {
                                    viewModel.navigateBack()
                                }
                            )
                        }

                        is QuestionnairesFlowViewModel.QuestionnairesUiState.Welcome -> {
                            WelcomeQuestionnairesScreen(
                                uiState = uiState,
                                onWelcomeButtonClick = { btn ->
                                    viewModel.activateWelcomeButton(btn)
                                },
                                onBackClick = {
                                    viewModel.navigateBack()
                                }
                            )
                        }

                        QuestionnairesFlowViewModel.QuestionnairesUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchWelcomeDetails() }
                        }
                    }


                    if (viewState.showCancelDialog) {
                        VodovozDialog(
                            title = stringResource(R.string.questionnaire_cancel_title),
                            description = stringResource(R.string.questionnaire_cancel_description),
                            acceptButtonText = stringResource(R.string.exit),
                            cancelButtonText = stringResource(R.string.cancel),
                            onDismiss = {
                                viewModel.closeCancelDialog()
                            },
                            onAccept = {
                                viewModel.closeCancelDialog()
                                viewModel.fetchWelcomeDetails()
                            }
                        )
                    }


                    LifecycleEffect(snackbarHostState) {
                        viewModel.events.collect { event ->
                            when (event) {
                                QuestionnairesFlowViewModel.QuestionnaireEvents.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                is QuestionnairesFlowViewModel.QuestionnaireEvents.GoToWebView -> {
                                    findNavController().navigateToWebView(
                                        event.url, requireContext().getString(R.string.space)
                                    )
                                }

                                QuestionnairesFlowViewModel.QuestionnaireEvents.ScrollToTop -> {
                                    launch { scrollState.animateScrollTo(0) }
                                }

                                is QuestionnairesFlowViewModel.QuestionnaireEvents.ShowToast -> {
                                    launch {
                                        snackbarHostState.showSnackbar(event.message)
                                    }
                                }
                            }
                        }
                    }

                    BackHandler {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }
}