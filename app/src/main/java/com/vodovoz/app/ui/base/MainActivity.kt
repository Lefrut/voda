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
import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.RemoteMessage
import com.vodovoz.app.R
import com.vodovoz.app.common.block_app_signal.BlockAppSignal
import com.vodovoz.app.common.block_app_signal.BlockAppSignalProvider
import com.vodovoz.app.common.cache.HttpErrorCache
import com.vodovoz.app.common.cache.HttpErrorCacheProvider
import com.vodovoz.app.common.cache.emptyHttpErrorCache
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.ui.base.model.SplashFileState
import com.vodovoz.app.util.VodovozSplashFile
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


val Activity.blockAppSignal: BlockAppSignal?
    get() = (this as? BlockAppSignalProvider)?.blockAppSignal

val Activity?.httpErrorCache: HttpErrorCache
    get() = ((this as? HttpErrorCacheProvider)?.httpErrorCache) ?: emptyHttpErrorCache

@AndroidEntryPoint
class MainActivity :
    AppCompatActivity(),
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.androidSplash.value
        }
        setupUi()
        downloadSplashFile()
        viewModel.checkAppState()
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
        //todo - when will be animation
//        viewModel.setFileState(SplashFileState.Loading)
//        VodovozSplashFile.downloadSplashFile(applicationContext).onSuccess {
//            viewModel.setFileState(SplashFileState.Success)
//        }.onFailure {
//            viewModel.setFileState(SplashFileState.Error)
//        }
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
        val remoteMessage = RemoteMessage(data)
        val params = remoteMessage.data.toMap()

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

