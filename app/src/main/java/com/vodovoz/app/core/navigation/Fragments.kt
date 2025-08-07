package com.vodovoz.app.core.navigation

import androidx.fragment.app.Fragment
import com.vodovoz.app.R


val Fragment.mainFragment: Fragment?
    get() {
        return activity?.supportFragmentManager?.findFragmentById(
            R.id.fcvMainContainer
        )?.childFragmentManager?.fragments?.lastOrNull()
    }