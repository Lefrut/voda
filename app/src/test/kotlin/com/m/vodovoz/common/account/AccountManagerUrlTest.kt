package com.m.vodovoz.common.account

import com.m.vodovoz.common.datastore.DataStorePrefs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AccountManagerUrlTest {

    private lateinit var dataStorePrefs: DataStorePrefs
    private lateinit var accountManager: AccountManager

    @Before
    fun setUp() {
        dataStorePrefs = mockk(relaxed = true)
        accountManager = AccountManager(
            dataStorePrefs = dataStorePrefs,
        )
    }

    @Test
    fun `updateUserUrl persists supplied account url`() {
        val businessUrl = "https://business.vodovoz.test/"

        accountManager.updateUserUrl(businessUrl)

        verify { dataStorePrefs.putString("user_url", businessUrl) }
    }

    @Test
    fun `getUserUrl returns stored value without applying web config rules`() = runTest {
        every { dataStorePrefs.getStringFlow("user_url") } returns flowOf("   ")

        val actualUrl = accountManager.getUserUrl()

        assertEquals("   ", actualUrl)
    }
}
