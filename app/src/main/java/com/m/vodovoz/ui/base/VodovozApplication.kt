package com.m.vodovoz.ui.base

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.m.vodovoz.BuildConfig
import com.m.vodovoz.common.notification.NotificationChannels
import com.m.vodovoz.core.network.VodovozWebConfig
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.HiltAndroidApp
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class VodovozApplication : Application(), Configuration.Provider, SingletonImageLoader.Factory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()

        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY)
        MapKitFactory.initialize(this)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        initAppMetrica()
        NotificationChannels.create(this)
    }

    private fun initAppMetrica() {
        if (!BuildConfig.DEBUG) {
            val config: AppMetricaConfig = AppMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_METRICA_KEY)
                .withNativeCrashReporting(false)
                .withLocationTracking(false)
                .withAppVersion(BuildConfig.VERSION_NAME)
                .withUserProfileID(VodovozWebConfig.VODOVOZ_URL)
                .withLogs()
                .build()

            AppMetrica.activate(applicationContext, config)
            AppMetrica.enableActivityAutoTracking(this)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(SvgDecoder.Factory())
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()
    }


}