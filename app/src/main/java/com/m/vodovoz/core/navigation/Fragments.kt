package com.m.vodovoz.core.navigation

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.m.vodovoz.R


val Fragment.mainFragment: Fragment?
    get() {
        return activity?.supportFragmentManager?.findFragmentById(
            R.id.fcvMainContainer
        )?.childFragmentManager?.fragments?.lastOrNull()
    }

val Activity.mainFragment: Fragment?
    get() {
        return (this as? AppCompatActivity)?.supportFragmentManager?.findFragmentById(
            R.id.fcvMainContainer
        )?.childFragmentManager?.fragments?.lastOrNull()
    }