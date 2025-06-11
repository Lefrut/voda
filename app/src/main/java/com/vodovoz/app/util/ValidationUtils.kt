package com.vodovoz.app.util

object ValidationUtils {

    val PASSWORD_LENGTH = 2..30
    val PHONE_REGEX = Regex("\\+7\\(([0-9]{3})\\)([0-9]{3})-([0-9]{2})-([0-9]{2})")
    val EMAIL_REGEX = Regex("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")

}