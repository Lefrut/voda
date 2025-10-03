package com.m.vodovoz.feature.sub_categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.activate
import com.m.vodovoz.core.navigation.navigateToCategoryProductList
import com.m.vodovoz.core.navigation.navigateToQrCode
import com.m.vodovoz.core.navigation.navigateToSearch
import com.m.vodovoz.core.navigation.navigateToSpeechDialog
import com.m.vodovoz.core.navigation.navigateToSubCategories
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.sub_categories.model.SubCategoriesEvent
import com.m.vodovoz.ui.mvi.collectAsState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SubCategoriesFragment : Fragment() {

    val viewModel by viewModels<SubCategoriesViewModel>()

    @Inject
    lateinit var tabManager: TabManager

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

                    SubCategoriesScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                is SubCategoriesEvent.GoToProductList -> {
                                    findNavController().navigateToCategoryProductList(event.categoryId)
                                }

                                is SubCategoriesEvent.GoToSubCategories -> {
                                    findNavController().navigateToSubCategories(event.category)
                                }

                                SubCategoriesEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                SubCategoriesEvent.GoToSearch -> {
                                    findNavController().navigateToSearch()
                                }

                                is SubCategoriesEvent.ActivateDataAllAction -> {
                                    event.action.activate(
                                        navController = findNavController(),
                                        tabManager = tabManager
                                    )
                                }

                                SubCategoriesEvent.GoToScanner -> {
                                    findNavController().navigateToQrCode()
                                }

                                SubCategoriesEvent.GoToSpeechRecognizer -> {
                                    findNavController().navigateToSpeechDialog()
                                }
                            }

                        }
                    }
                }
            }

        }
    }

}