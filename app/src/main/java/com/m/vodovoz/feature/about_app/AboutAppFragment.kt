package com.m.vodovoz.feature.about_app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.R
import com.m.vodovoz.common.tab.TabManager
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.VodovozTheme
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.about_app.composables.DeveloperBottomSheet
import com.m.vodovoz.feature.about_app.model.AboutAppEvent
import com.m.vodovoz.feature.cart.CartFlowViewModel
import com.m.vodovoz.feature.catalog.CatalogFlowViewModel
import com.m.vodovoz.feature.favorite.FavoriteFlowViewModel
import com.m.vodovoz.feature.home.HomeFlowViewModel
import com.m.vodovoz.feature.profile.ProfileFlowViewModel
import com.m.vodovoz.ui.base.MainActivityViewModel
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.isTablet
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AboutAppFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager

    private val viewModel: AboutAppViewModel by viewModels()
    private val homeViewModel: HomeFlowViewModel by activityViewModels()
    private val cartFlowViewModel: CartFlowViewModel by activityViewModels()
    private val favoriteViewModel: FavoriteFlowViewModel by activityViewModels()
    private val catalogFlowViewModel: CatalogFlowViewModel by activityViewModels()
    private val profileViewModel: ProfileFlowViewModel by activityViewModels()

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
                    val context = LocalContext.current

                    AboutAppScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )

                    if (viewState.showDeveloperBS) {
                        DeveloperBottomSheet(
                            onDismissClick = {
                                viewModel.hideDeveloperBottomSheet()
                            },
                            onModeClick = { appMode ->
                                viewModel.changeMode(appMode)
                            }
                        )
                    }

                    LifecycleEffect {
                        viewModel.events.collect { event ->
                            when (event) {
                                AboutAppEvent.GoBack -> {
                                    findNavController().popBackStack()
                                }

                                AboutAppEvent.Share -> {
                                    context.shareApp()
                                }

                                AboutAppEvent.RateApp -> {
                                    context.rateApp()
                                }

                                is AboutAppEvent.GoToWebView -> {
                                    findNavController().navigateToWebView(event.url, event.title)
                                }

                                is AboutAppEvent.WriteToDevelopers -> {
                                    context.writeToDevelopers(event.userId)
                                }

                                AboutAppEvent.RefreshApp -> {
                                    homeViewModel.fetchHomeDetails()
                                    cartFlowViewModel.fetchCartDetails()
                                    favoriteViewModel.fetchFavoriteProducts()
                                    catalogFlowViewModel.fetchCatalogDetails()
                                    profileViewModel.fetchProfileDetails()
                                    tabManager.selectTab(R.id.graph_home)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Context.shareApp() {
        val shareText = "https://play.google.com/store/apps/details?id=${packageName}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.share_app_text)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_action_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.rateApp() {
        val uri = "market://details?id=${packageName}".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        try {
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=${packageName}".toUri()
                )
                startActivity(webIntent)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.error_action_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun Context.writeToDevelopers(userId: Long) {
        val deviceType = if (isTablet()) getString(R.string.tablet) else getString(R.string.phone)
        val deviceInfo =
            getString(R.string.device_info, Build.MODEL, Build.VERSION.RELEASE, deviceType)

        val emailBody = getString(
            R.string.email_body_template,
            deviceInfo,
            BuildConfig.VERSION_NAME,
            userId
        )

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf("android@vodovoz.ru"))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            putExtra(Intent.EXTRA_TEXT, emailBody)
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show()
        }
    }
}