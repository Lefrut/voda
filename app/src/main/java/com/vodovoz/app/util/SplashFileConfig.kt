package com.vodovoz.app.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.URL

object SplashFileConfig {

    private const val FILE_NAME = "splash.json"
    const val DEFAULT_LINK = "https://vodovoz.ru/images/zastavka/zastavkamobil.json"

    fun getSplashFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    suspend fun downloadSplashFile(
        context: Context,
        link: String = DEFAULT_LINK,
    ): Result<Unit> = runCatching {
        withContext(Dispatchers.IO){
            withTimeout(5000L) {
                val url = URL(link)
                url.openStream().use { input ->
                    val file = getSplashFile(context)
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }

}