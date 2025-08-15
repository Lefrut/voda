package com.vodovoz.app.feature.search.qrcode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.vodovoz.app.ui.mvi.collectAsState
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil3.compose.rememberAsyncImagePainter
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToProductDetails
import com.vodovoz.app.core.navigation.navigateToSearchProductList
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.composables.placeholders.EmptyResultPlaceholder
import com.vodovoz.app.design_system.composables.placeholders.EmptyResultPlaceholderItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QrCodeFragment : Fragment() {


    private val viewModel: QrCodeViewModel by viewModels()


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
                    val viewState by viewModel.collectAsState()


                    when (val uiState = viewState.uiState) {
                        is QrCodeViewModel.QrCodeUiState.EmptyResult -> {
                            EmptyResultPlaceholder(
                                title = uiState.title,
                                description = uiState.description,
                                item = EmptyResultPlaceholderItem.Cross,
                                imagePainter = rememberAsyncImagePainter(
                                    model = uiState.imageUrl,
                                    error = painterResource(id = R.drawable.pic_search)
                                ),
                                onItemClick = {
                                    viewModel.setScannerState()
                                }
                            )
                        }

                        QrCodeViewModel.QrCodeUiState.Scanner -> {
                            ScannerScreen(
                                viewState = viewState,
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeEvents()
    }

    private fun observeEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events
                    .collect { qrCodeEvents ->
                        when (qrCodeEvents) {
                            is QrCodeViewModel.QrCodeEvents.Success -> {
                                findNavController().navigateToProductDetails(
                                    qrCodeEvents.id.toLong()
                                )
                            }

                            QrCodeViewModel.QrCodeEvents.GoBack -> {
                                findNavController().popBackStack()
                            }

                            is QrCodeViewModel.QrCodeEvents.GoToProductDetails -> {
                                findNavController().navigateToProductDetails(qrCodeEvents.id)
                            }

                            is QrCodeViewModel.QrCodeEvents.GoToSearchProducts -> {
                                findNavController().navigateToSearchProductList(qrCodeEvents.barCode)
                            }
                        }
                    }
            }

        }
    }

}