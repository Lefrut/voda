package com.m.vodovoz.core.navigation

import android.os.Bundle
import androidx.navigation.NavOptions
import androidx.navigation3.runtime.NavKey

data class LegacyDestinationNavKey(
    val destinationId: Int,
    val args: Bundle? = null,
    val navOptions: NavOptions? = null,
) : NavKey
