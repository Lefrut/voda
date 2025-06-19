package com.vodovoz.app.data.parser.common

import org.json.JSONObject



fun JSONObject.safeString(name: String): String =  when(has(name)) {
    true -> when(isNull(name)) {
        true -> ""
        false -> getString(name)
    }
    false -> ""
}






fun JSONObject.safeLong(name: String) = when(has(name)) {
    true -> when(isNull(name)) {
        true -> 0L
        false -> getLong(name)
    }
    false -> 0L
}
