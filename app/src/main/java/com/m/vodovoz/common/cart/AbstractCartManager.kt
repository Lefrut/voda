package com.m.vodovoz.common.cart

import kotlinx.coroutines.sync.Mutex

abstract class AbstractCartManager {

    protected open val sendMutex = Mutex()

}