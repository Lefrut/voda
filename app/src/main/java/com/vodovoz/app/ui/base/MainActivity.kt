package com.vodovoz.app.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.firebase.messaging.RemoteMessage
import com.vodovoz.app.R
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.block_app_signal.BlockAppSignalProvider
import com.vodovoz.app.databinding.ActivityMainBinding
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.setSystemBarIconColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BlockAppSignalProvider {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var reloadAppSignalInject: BlockAppSignal

    @get:Inject
    override val appSignal get() = reloadAppSignalInject

    @Inject
    lateinit var siteStateManager: SiteStateManager

    private val viewModel: MainActivityViewModel by viewModels()
    private val splashFileViewModel: SplashFileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition { viewModel.androidSplash.value }
        window.setSystemBarIconColors()
        supportActionBar?.hide()
        splashFileViewModel.downloadSplashFile()

        viewModel.checkAppState()

        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fcvMainContainer.id) as? NavHostFragment
        val navController = navHostFragment?.navController
        navController?.setGraph(R.navigation.nav_graph)
        navController?.navigate(
            resId = R.id.splashFragment,
            args = null,
            navOptions = navOptions {
                launchSingleTop = true
            }
        )

        processIntent(intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        debugLog { "onNewIntent: $intent" }
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        handleIntent(intent)
        handlePushIntent(intent)
    }

    private fun handlePushIntent(intent: Intent) {
        val data = intent.extras ?: return
        val remoteMessage = RemoteMessage(data)
        val params = remoteMessage.data.toMap()

        val jsonData = if (params.isNotEmpty()) {
            JSONObject(params)
        }
        else null

        lifecycleScope.launch {
            if (jsonData != null) siteStateManager.savePushData(jsonData)
        }

    }

    private fun handleIntent(intent: Intent) = lifecycleScope.launch {
        val appLinkData: Uri? = intent.data
        val path = appLinkData?.lastPathSegment
        siteStateManager.saveDeepLinkPath(path)
    }
}