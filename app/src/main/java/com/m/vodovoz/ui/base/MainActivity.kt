package com.m.vodovoz.ui.base

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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.RemoteMessage
import com.m.vodovoz.R
import com.m.vodovoz.common.block_app_signal.BlockAppSignal
import com.m.vodovoz.common.block_app_signal.BlockAppSignalProvider
import com.m.vodovoz.common.cache.HttpErrorCache
import com.m.vodovoz.common.cache.HttpErrorCacheProvider
import com.m.vodovoz.common.cache.emptyHttpErrorCache
import com.m.vodovoz.feature.sitestate.SiteStateManager
import com.m.vodovoz.ui.base.model.AppState
import com.m.vodovoz.ui.base.model.SplashFileState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


val Activity.blockAppSignal: BlockAppSignal?
    get() = (this as? BlockAppSignalProvider)?.blockAppSignal

val Activity?.httpErrorCache: HttpErrorCache
    get() = ((this as? HttpErrorCacheProvider)?.httpErrorCache) ?: emptyHttpErrorCache

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    BlockAppSignalProvider,
    HttpErrorCacheProvider {

    @Inject
    lateinit var blockAppSignalInject: BlockAppSignal
    override val blockAppSignal get() = blockAppSignalInject

    @Inject
    lateinit var httpErrorCacheInject: HttpErrorCache
    override val httpErrorCache: HttpErrorCache get() = httpErrorCacheInject

    @Inject
    lateinit var siteStateManager: SiteStateManager

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onResume() {
        super.onResume()
        viewModel.updateCookieIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.androidSplash.value
        }

        setupUi()
        downloadSplashFile()
        viewModel.fetchAppConfig()
        handleIntent(intent)
    }

    private fun setupUi() {
        enableEdgeToEdge(
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
    }

    private fun downloadSplashFile() = lifecycleScope.launch {
        viewModel.setFileState(SplashFileState.Success)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        handleDeepLinkIntent(intent)
        handlePushIntent(intent)
    }


    private fun handlePushIntent(intent: Intent) {
        val data = intent.extras ?: return
        val params = RemoteMessage(data).data.toMap().ifEmpty {
            return
        }

        val jsonData = if (params.isNotEmpty()) {
            JSONObject(params)
        } else null

        lifecycleScope.launch {
            if (jsonData != null) siteStateManager.savePushData(jsonData)
        }

    }

    private fun handleDeepLinkIntent(intent: Intent) = lifecycleScope.launch {
        val appLinkData: Uri? = intent.data
        val path = appLinkData?.lastPathSegment
        siteStateManager.saveDeepLinkPath(path)
    }
}

