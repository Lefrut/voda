package com.m.vodovoz.core.navigation

import androidx.compose.runtime.compositionLocalOf
import com.m.vodovoz.feature.main.Navigator

val LocalNavigator = compositionLocalOf<Navigator> { error("Can't find navigator") }
