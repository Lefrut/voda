package com.vodovoz.app.ui.base

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.vodovoz.app.BuildConfig
import com.vodovoz.app.common.notification.NotificationChannels
import com.vodovoz.app.core.network.ApiConfig
import com.vodovoz.app.common.constants.AppKeys
import com.vodovoz.app.common.constants.AppKeys.YANDEX_METRICA_KEY
import com.yandex.mapkit.MapKitFactory
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class VodovozApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))
        //initYandexMetrica() //todo релиз
        MapKitFactory.setApiKey(AppKeys.MAPKIT_API_KEY)
        Timber.plant(Timber.DebugTree())
        NotificationChannels.create(this)
    }

    //todo - need review
    private fun initYandexMetrica() {
        if(!BuildConfig.DEBUG) {
            val config: YandexMetricaConfig = YandexMetricaConfig.newConfigBuilder(YANDEX_METRICA_KEY)
                    .withNativeCrashReporting(false)
                    .withLocationTracking(false)
                    .withAppVersion(BuildConfig.VERSION_NAME)
                    .withUserProfileID(ApiConfig.VODOVOZ_URL)
                    .withLogs()
                    .build()
            YandexMetrica.activate(this, config)
            YandexMetrica.enableActivityAutoTracking(this)
        }
    }


}