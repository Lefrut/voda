package com.m.vodovoz.util.formatters

import java.time.format.DateTimeFormatter

data object VodovozDateFormatters {
    val DMY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val DMY_HMS: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
}