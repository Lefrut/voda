package com.m.vodovoz.core.navigation

import androidx.fragment.app.Fragment
import com.m.vodovoz.R


val Fragment.mainFragment: Fragment?
    get() {
        return activity?.supportFragmentManager?.findFragmentById(
            R.id.fcvMainContainer
        )?.childFragmentManager?.fragments?.lastOrNull()
    }