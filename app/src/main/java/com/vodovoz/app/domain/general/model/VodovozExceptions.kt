package com.vodovoz.app.domain.general.model


open class RequestException(
    message: String = "",
    cause: Throwable? = null,
    val placeholder: VodovozPlaceholderModel? = null,
) : IllegalStateException(
    message, cause
)

class WebsiteErrorException(
    message: String = "",
    cause: Throwable? = null,
) : RequestException(
    message, cause
)

class ValidationException(
    message: String = "",
    cause: Throwable? = null,
) : IllegalStateException(
    message, cause
)

class EmptyResultException(
    message: String = "",
    placeholder: VodovozPlaceholderModel? = null,
    cause: Throwable? = null,
) : RequestException(
    message, cause, placeholder
)

class UserNotLoginException(
    message: String = "",
    cause: Throwable? = null,
    placeholder: VodovozPlaceholderModel? = null,
) : RequestException(
    message, cause, placeholder
)

class TooManyRequestsException(
    message: String = "",
    cause: Throwable? = null,
    placeholder: VodovozPlaceholderModel? = null,
    val remainingSeconds: Int = 0,
) : RequestException(
    message, cause, placeholder
)


data class VodovozPlaceholderModel(
    val title: String,
    val headerHtml: String,
    val descriptionHtml: String,
    val imageUrl: String,
    val button: ColorfulButtonModel? = null,
) {
    companion object {
        val Empty = VodovozPlaceholderModel(
            "", "", "", ""
        )
    }
}