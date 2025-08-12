package com.vodovoz.app.ui.base

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.google.firebase.messaging.RemoteMessage
import com.vodovoz.app.R
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.block_app_signal.BlockAppSignalProvider
import com.vodovoz.app.common.cache.HttpErrorCache
import com.vodovoz.app.common.cache.HttpErrorCacheProvider
import com.vodovoz.app.databinding.ActivityMainBinding
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.debugLog
import com.vodovoz.app.util.extensions.setSystemBarColors
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


val Activity.blockAppSignal: BlockAppSignal?
    get() = (this as? BlockAppSignalProvider)?.blockAppSignal

val Activity.httpErrorCache: HttpErrorCache?
    get() = (this as? HttpErrorCacheProvider)?.httpErrorCache

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), BlockAppSignalProvider, HttpErrorCacheProvider {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var blockAppSignalInject: BlockAppSignal
    override val blockAppSignal get() = blockAppSignalInject

    @Inject
    lateinit var httpErrorCacheInject: HttpErrorCache
    override val httpErrorCache: HttpErrorCache get() = httpErrorCacheInject

    @Inject
    lateinit var siteStateManager: SiteStateManager

    private val viewModel: MainActivityViewModel by viewModels()
    private val splashFileViewModel: SplashFileViewModel by viewModels()

    override fun onStart() {
        super.onStart()

        window.setSystemBarColors(
            statusColor = Color.TRANSPARENT,
            navigationColor = Color.TRANSPARENT,
            lightNavigationBarIcons = false,
            lightStatusBarIcons = false
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.androidSplash.value
        }

        enableEdgeToEdge(
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        supportActionBar?.hide()
        splashFileViewModel.downloadSplashFile()
        viewModel.checkAppState()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fcvMainContainer.setSplashScreen()

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
        } else null

        lifecycleScope.launch {
            if (jsonData != null) siteStateManager.savePushData(jsonData)
        }

    }

    private fun handleIntent(intent: Intent) = lifecycleScope.launch {
        val appLinkData: Uri? = intent.data
        val path = appLinkData?.lastPathSegment
        siteStateManager.saveDeepLinkPath(path)
    }

    private fun FragmentContainerView.setSplashScreen() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(id) as? NavHostFragment
        val navController = navHostFragment?.navController
        navController?.setGraph(R.navigation.nav_graph)
        navController?.navigate(
            resId = R.id.splashFragment,
            args = null,
            navOptions = navOptions {
                launchSingleTop = true
            }
        )
    }


}