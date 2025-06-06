package com.vodovoz.app.util.formatters

import java.time.format.DateTimeFormatter

data object VodovozDateFormatters {
    val DMY: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
}