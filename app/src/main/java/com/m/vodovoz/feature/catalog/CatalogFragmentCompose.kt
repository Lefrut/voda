package com.m.vodovoz.feature.catalog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.R
import com.m.vodovoz.common.account.AccountManager
import com.m.vodovoz.common.cookie.CookieManager
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.ContentSearchNavigator
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToSubCategories
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.composables.placeholders.NetworkErrorPlaceholder
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CatalogFragment : Fragment() {

    private val viewModel: CatalogFlowViewModel by activityViewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var navigatorFactory: ContentSearchNavigator.Factory

    private lateinit var searchNavigator: ContentSearchNavigator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        searchNavigator = navigatorFactory.create(
            findNavController(), this
        )
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


                    when (viewState.uiState) {
                        CatalogFlowViewModel.CatalogUiState.Error -> {
                            NetworkErrorPlaceholder { viewModel.fetchCatalogDetails() }
                        }

                        else -> {
                            CatalogScreen(viewModel = viewModel, viewState = viewState)
                        }
                    }

                    LifecycleEffect {
                        observeTabReselect()
                    }

                    LifecycleEffect {
                        observeEvents()
                    }
                }
            }
        }
    }


    private suspend fun observeEvents() {
        viewModel.events
            .collect { event ->
                when (event) {
                    is CatalogFlowViewModel.CatalogEvents.GoToProfile -> {
                        tabManager.setAuthRedirect(findNavController().graph.id)
                        tabManager.selectTab(R.id.graph_profile)
                    }

                    CatalogFlowViewModel.CatalogEvents.GoToSearch -> {
                        findNavController().navigate(R.id.searchFragment)
                    }

                    is CatalogFlowViewModel.CatalogEvents.GoToSubCategories -> {
                        findNavController().navigateToSubCategories(event.catalogCategory)
                    }

                    is CatalogFlowViewModel.CatalogEvents.GoToProductList -> {
                        findNavController().navigateToCategoryProductList(event.catalogCategory.id)
                    }

                    CatalogFlowViewModel.CatalogEvents.GoToScanner -> {
                        searchNavigator.navigateToImageSearch()
                    }

                    CatalogFlowViewModel.CatalogEvents.ShowSpeechRecognizer -> {
                        searchNavigator.navigateToVoiceSearch()
                    }

                    is CatalogFlowViewModel.CatalogEvents.ActivateAction -> {
                        event.action.activate(
                            navController = findNavController(),
                            context = requireActivity(),
                            cookie = cookieManager.fetchCookieSessionId() ?: "",
                            tabManager = tabManager
                        )
                    }
                }
            }
    }


    private suspend fun observeTabReselect() {
        tabManager.observeTabReselect().collect {
            if (it != TabManager.DEFAULT_STATE && it == R.id.catalogFragment) {
                tabManager.setDefaultState()
            }
        }

    }
}