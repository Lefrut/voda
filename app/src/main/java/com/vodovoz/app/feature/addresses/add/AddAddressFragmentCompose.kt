package com.vodovoz.app.feature.addresses.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.R
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.core.navigation.navigateToMap
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.addresses.add.composables.AddAddressScreen
import com.vodovoz.app.feature.addresses.add.model.AddAddressEvent
import com.vodovoz.app.feature.addresses.model.AddressUi
import com.vodovoz.app.feature.map.MapFlowViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onSubscription
import javax.inject.Inject

@AndroidEntryPoint
class AddAddressFragment : Fragment() {

    private val viewModel: AddAddressViewModel by viewModels()

    @Inject
    lateinit var tabManager: TabManager

    override fun onResume() {
        super.onResume()
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
            setContent {
                VodovozTheme {
                    val viewState by viewModel.state.collectAsStateWithLifecycle()

                    AddAddressScreen(viewModel = viewModel, viewState = viewState)

                    LifecycleEffect {
                        observeEvents()
                    }

                    BackHandler {
                        viewModel.navigateBack()
                    }
                }
            }
        }
    }

    private suspend fun observeEvents() {
        viewModel.events.onSubscription {
            findNavController().currentBackStackEntry?.savedStateHandle?.remove<String>("addressName")?.let { name ->
                viewModel.changeAddressName(name)
            }
        }.collect { event ->
            when (event) {
                AddAddressEvent.GoBack -> {
                    findNavController().popBackStack(
                        R.id.savedAddressesDialogFragment,
                        false
                    )
                }

                is AddAddressEvent.GoToMap -> {
                    findNavController().navigateToMap(
                        AddressUi(
                            id = event.addressId,
                            address = event.addressName,
                            personTypeId = -1,
                            description = ""
                        )
                    )
                }
            }
        }
    }
}
