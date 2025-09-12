package com.vodovoz.app.util

import CoroutineTestBase
import android.content.Context
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File


class VodovozSplashFileTest : CoroutineTestBase() {

    private val context: Context = mockk(relaxed = true)
    private val vodovozSplashFile = VodovozSplashFile

    private class FakeFile(private val exists: Boolean) : File("file.txt") {

        override fun exists(): Boolean = exists

    }

    @Test
    fun downloadSplashFile() = runTest {
        val fakeFiles = listOf(FakeFile(true), FakeFile(false))

        mockkObject(vodovozSplashFile)
        mockkStatic(File::writeToFile)

        fakeFiles.forEach { coEvery { it.writeToFile(any()) } just Runs }
        every { vodovozSplashFile.getSplashFile(context) } returnsMany fakeFiles

        assertTrue(vodovozSplashFile.downloadSplashFile(context).isSuccess)
        assertTrue(vodovozSplashFile.downloadSplashFile(context).isSuccess)
    }

}