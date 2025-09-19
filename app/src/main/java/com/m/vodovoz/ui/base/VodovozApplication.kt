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
import com.m.vodovoz.common.constants.AppKeys
import com.m.vodovoz.common.constants.AppKeys.YANDEX_METRICA_KEY
import com.m.vodovoz.common.notification.NotificationChannels
import com.m.vodovoz.core.network.VodovozWebConfig
import com.yandex.mapkit.MapKitFactory
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp
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

        //initYandexMetrica() //todo релиз
        Timber.plant(Timber.DebugTree())
        NotificationChannels.create(this)
    }

    private fun initYandexMetrica() {
        if (!BuildConfig.DEBUG) {
            val config: YandexMetricaConfig =
                YandexMetricaConfig.newConfigBuilder(YANDEX_METRICA_KEY)
                    .withNativeCrashReporting(false)
                    .withLocationTracking(false)
                    .withAppVersion(BuildConfig.VERSION_NAME)
                    .withUserProfileID(VodovozWebConfig.VODOVOZ_URL)
                    .withLogs()
                    .build()
            YandexMetrica.activate(this, config)
            YandexMetrica.enableActivityAutoTracking(this)
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