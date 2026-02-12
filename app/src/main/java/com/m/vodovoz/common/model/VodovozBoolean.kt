package com.m.vodovoz.common.model

import com.m.vodovoz.common.model.VodovozBoolean.False
import com.m.vodovoz.common.model.VodovozBoolean.True

enum class VodovozBoolean(val value: String) {
    True("Y"), False("N");

    companion object
}


val VodovozBoolean.boolean: Boolean
    get() {
        return when (this) {
            True -> true
            False -> false
        }
    }

fun VodovozBoolean.toBoolean(): Boolean {
    return when (this) {
        True -> true
        False -> false
    }
}

fun String.toVodovozBoolean(): VodovozBoolean? {
    return VodovozBoolean.entries.firstOrNull { it.value == this }
}

fun VodovozBoolean.Companion.from(value: String?): VodovozBoolean {
    return when (value) {
        True.value -> {
            True
        }

        else -> {
            False
        }
    }
}

fun VodovozBoolean.Companion.from(value: Boolean?): VodovozBoolean {
    return when (value) {
        true -> {
            True
        }

        else -> {
            False
        }
    }
}

fun String?.toBoleanByVodovoz(): Boolean {
    return VodovozBoolean.from(this).boolean
}

fun Boolean?.toStringByVodovoz(): String {
    return VodovozBoolean.from(this).value
}

infix fun VodovozBoolean.equalsTo(boolean: Boolean?): Boolean = toBoolean() == boolean

infix fun VodovozBoolean.equalsTo(string: String?): Boolean = VodovozBoolean.from(string) == this