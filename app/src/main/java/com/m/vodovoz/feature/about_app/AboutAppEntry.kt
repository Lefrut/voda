package com.m.vodovoz.feature.about_app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.R
import com.m.vodovoz.core.navigation.NavigationEntry
import com.m.vodovoz.core.navigation.navigateToHome
import com.m.vodovoz.core.navigation.navigateToWebView
import com.m.vodovoz.design_system.effects.LifecycleEffect
import com.m.vodovoz.feature.about_app.composables.DeveloperBottomSheet
import com.m.vodovoz.feature.about_app.model.AboutAppEvent
import com.m.vodovoz.ui.mvi.collectAsState
import com.m.vodovoz.util.extensions.isTablet

@Composable
fun AboutAppEntry(
    onRefreshApp: () -> Unit,
) = NavigationEntry<AboutAppViewModel> {
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
                    navigator.goBack()
                }

                AboutAppEvent.Share -> {
                    context.shareApp()
                }

                AboutAppEvent.RateApp -> {
                    context.rateApp()
                }

                is AboutAppEvent.GoToWebView -> {
                    navigator.navigateToWebView(event.url, event.title)
                }

                is AboutAppEvent.WriteToDevelopers -> {
                    context.writeToDevelopers(event.userId)
                }

                AboutAppEvent.RefreshApp -> {
                    onRefreshApp()
                    navigator.navigateToHome()
                }
            }
        }
    }
}

private fun android.content.Context.shareApp() {
    val shareText = "https://play.google.com/store/apps/details?id=${packageName}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
    }

    try {
        startActivity(Intent.createChooser(intent, getString(R.string.share_app_text)))
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.error_action_failed), Toast.LENGTH_SHORT).show()
    }
}

private fun android.content.Context.rateApp() {
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
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.error_action_failed), Toast.LENGTH_SHORT).show()
    }
}

private fun android.content.Context.writeToDevelopers(userId: Long) {
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
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, getString(R.string.no_email_client), Toast.LENGTH_SHORT).show()
    }
}
