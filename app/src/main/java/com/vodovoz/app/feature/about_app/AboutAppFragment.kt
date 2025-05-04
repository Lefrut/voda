package com.vodovoz.app.feature.about_app

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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.vodovoz.app.common.tab.TabManager
import com.vodovoz.app.design_system.VodovozTheme
import com.vodovoz.app.design_system.effects.LifecycleEffect
import com.vodovoz.app.feature.about_app.model.AboutAppEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.ui.extensions.ContextExtensions.isTablet
import com.vodovoz.app.R
import com.vodovoz.app.core.navigation.navigateToWebView
import com.vodovoz.app.util.extensions.fromHtml

@AndroidEntryPoint
class AboutAppFragment : Fragment() {

    @Inject
    lateinit var tabManager: TabManager
    private val viewModel: AboutAppViewModel by viewModels()

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
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                VodovozTheme {
                    val viewState by viewModel.state.collectAsStateWithLifecycle()
                    val context = LocalContext.current

                    AboutAppScreen(
                        viewModel = viewModel,
                        viewState = viewState
                    )

                    LifecycleEffect(arg2 = context) {
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
            putExtra(Intent.EXTRA_TEXT, (StringBuilder()
                .append("<br><br><br><br><font size=\"2\">${deviceInfo}</font>")
                .append("<br>Версия приложения: ${BuildConfig.VERSION_NAME}")
                .append("<br>User id:" + viewModel.fetchUserId())
                .toString()).fromHtml()
            )
        }

        try {
            startActivity(Intent.createChooser(intent, getString(R.string.choose_email_client)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show()
        }
    }
}