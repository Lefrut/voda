package com.vodovoz.app.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import java.net.URL

object VodovozSplashFile {

    private const val FILE_NAME = "splash.json"
    private const val FILE_LINK = "https://vodovoz.ru/images/zastavka/zastavkamobil.json"

    fun getSplashFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    suspend fun downloadSplashFile(
        context: Context,
        link: String = FILE_LINK,
    ): Result<Unit> = runCatching {

        val file = getSplashFile(context)
        if (file.exists()) return@runCatching

        withTimeout(5000L) {
            file.writeToFile(link)
        }
    }

}

suspend fun File.writeToFile(url: String) {
    withContext(Dispatchers.IO) {
        URL(url).openStream()
    }.use { input ->
        outputStream().use { output ->
            input.copyTo(output)
        }
    }
}