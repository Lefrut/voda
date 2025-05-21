package com.vodovoz.app.feature.full_screen_history_slider

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.vodovoz.app.R
import com.vodovoz.app.common.account.data.AccountManager
import com.vodovoz.app.common.content.BaseFragment
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.data.model.common.ActionEntity
import com.vodovoz.app.databinding.FragmentFullscreenHistorySliderBinding
import com.vodovoz.app.feature.all.promotions.AllPromotionsFragment
import com.vodovoz.app.feature.full_screen_history_slider.adapter.HistoriesDetailStateAdapter
import com.vodovoz.app.feature.productlistnofilter.ProductCatalogFragment
import com.vodovoz.app.ui.interfaces.IOnChangeHistory
import com.vodovoz.app.ui.interfaces.IOnInvokeAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenHistoriesSliderFlowFragment1 : BaseFragment(),
    IOnChangeHistory, IOnInvokeAction {

    override fun layout() = R.layout.fragment_fullscreen_history_slider

    private val binding: FragmentFullscreenHistorySliderBinding by viewBinding {
        FragmentFullscreenHistorySliderBinding.bind(
            contentView
        )
    }

    private val viewModel: FullScreenHistoriesSliderFlowViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    @Inject
    lateinit var accountManager: AccountManager


    private var startHistoryId: Long = 0
    private var lastIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.FullScreenDialogWithoutStatusBar)
        viewModel.updateData()
        getArgs()
    }

    private fun getArgs() {
        FullScreenHistoriesSliderFlowFragmentArgs.fromBundle(requireArguments()).let { args ->
            startHistoryId = args.startHistoryId
        }
    }

    override fun initView() {
        observeViewModel()
        observeViewModelEvents()
    }

    private fun observeViewModelEvents() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeEvent()
                    .collect {
                        when (it) {
                            is FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.GoToProfile -> {
                                tabManager.setAuthRedirect(findNavController().graph.id)
                                tabManager.selectTab(R.id.graph_profile)
                            }

                            FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.GoBack -> {
                                findNavController().popBackStack()
                            }

                            is FullScreenHistoriesSliderFlowViewModel.HistoriesSliderEvents.ChangePagerIndex -> {

                            }
                        }
                    }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.observeUiState().collect { state ->
                if (state.loadingPage) {
                    showLoaderWithBg(true)
                } else {
                    showLoaderWithBg(false)
                }

                val historyUIList = state.data.historyUIList
                if (historyUIList.isNotEmpty()) {
                    binding.vpHistories.adapter = HistoriesDetailStateAdapter(
                        fragment = this@FullScreenHistoriesSliderFlowFragment1,
                        historyUIList = historyUIList
                    )
                    binding.vpHistories.currentItem =
                        historyUIList.indexOfFirst { it.id == startHistoryId }
                    lastIndex = historyUIList.indices.last
                }

                showError(state.error)
            }
        }
    }

    override fun nextHistory() {
        if (binding.vpHistories.currentItem == lastIndex) {
            findNavController().popBackStack()
        } else {
            binding.vpHistories.currentItem += 1
        }
    }

    override fun previousHistory() {
        binding.vpHistories.currentItem -= 1
    }

    override fun close() {
        findNavController().popBackStack()
    }


    override fun update() {
        viewModel.updateData()
    }

    override fun onInvokeAction(actionEntity: ActionEntity) {

    }

}