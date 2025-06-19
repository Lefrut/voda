package com.vodovoz.app.ui.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.messaging.RemoteMessage
import com.vodovoz.app.R
import com.vodovoz.app.databinding.ActivityMainBinding
import com.vodovoz.app.feature.sitestate.SiteStateManager
import com.vodovoz.app.util.extensions.debugLog
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var siteStateManager: SiteStateManager

    private val viewModel: MainActivityViewModel by viewModels()
    private val splashFileViewModel: SplashFileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition {
            viewModel.androidSplash.value
        }
        supportActionBar?.hide()
        splashFileViewModel.downloadSplashFile()

        viewModel.checkAppState()

        MapKitFactory.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater).apply { setContentView(root) }
        supportFragmentManager.commit {
            val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
            replace(R.id.fcvMainContainer, navHostFragment)
            setPrimaryNavigationFragment(navHostFragment)
        }

        processIntent(intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        debugLog { "onNewIntent: $intent" }
        processIntent(intent)
    }

    private fun processIntent(intent: Intent) {
        debugLog { "proccessIntent: ${intent.data}" }
        handleIntent(intent)
        handlePushIntent(intent)
    }

    private fun handlePushIntent(intent: Intent) {
        val data = intent.extras ?: return
        val remoteMessage = RemoteMessage(data)

        val jsonData = if (remoteMessage.data.isNotEmpty()) {
            JSONObject(remoteMessage.data.toString())
        } else {
            null
        }

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