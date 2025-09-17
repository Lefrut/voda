package com.m.vodovoz.data.parser.common

import org.json.JSONObject



fun JSONObject.safeString(name: String): String =  when(has(name)) {
    true -> when(isNull(name)) {
        true -> ""
        false -> getString(name)
    }
    false -> ""
}